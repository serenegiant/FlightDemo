/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef USBWEBCAMERAPROJ_FRAME_POOL_H
#define USBWEBCAMERAPROJ_FRAME_POOL_H

#include <vector>
#include "utilbase.h"

#define DEFAULT_INIT_FRAME_POOL_SZ 2
#define DEFAULT_MAX_FRAME_NUM 8
#define DEFAULT_FRAME_SZ 1024

// メモリアロケーションの回数を減らしてスピードアップ・・・実測で5〜30%ぐらい速くなるみたい
template <class T>
class FramePool {
private:
	const int MAX_FRAME_NUM;				// 最大フレーム数
	const bool CREATE_IF_EMPTY;				// プールが空の時に自動生成するかどうか
	const bool BLOCK_IF_EMPTY;				// プールが空の時にブロックしてrecycleされるまで待機するかどうか
	size_t default_frame_sz;				// フレーム生成時の初期サイズ
	volatile uint32_t total_frame_num;		// 生成されたフレームの個数
	volatile bool cleared;					// フレームバッファがクリアされたかどうか
	mutable Mutex pool_mutex;				// ミューテックス
	Condition pool_sync;					// 同期オブジェクト
	std::vector<T *> mFramePool;			// フレームバッファプール
protected:
	FramePool(const uint32_t &max_frame_num = DEFAULT_MAX_FRAME_NUM, const uint32_t &init_frame_num = DEFAULT_INIT_FRAME_POOL_SZ,
		const size_t &_default_frame_sz = DEFAULT_FRAME_SZ,
		const bool &create_if_empty = false, const bool &block_if_empty = false)
	:	MAX_FRAME_NUM(max_frame_num),
		CREATE_IF_EMPTY(create_if_empty),
		BLOCK_IF_EMPTY(block_if_empty),
		default_frame_sz(_default_frame_sz),
		total_frame_num(0),
		cleared(true) {
		ENTER();

		init_pool(init_frame_num, _default_frame_sz);

		EXIT();
	}

	virtual ~FramePool() {
		ENTER();

		clear_pool();

		EXIT();
	}

	virtual T *createFrame(const size_t &data_bytes) {
		return new T(data_bytes);
	}

	void init_pool(const uint32_t &init_num, const size_t &data_bytes = 0) {
		ENTER();

		clear_pool();
		if (LIKELY(init_num)) {
			pool_mutex.lock();
			{
				if (data_bytes) {
					default_frame_sz = data_bytes;
				}
				for (uint32_t i = 0; i < init_num; i++) {
					mFramePool.push_back(createFrame(default_frame_sz));
					total_frame_num++;
				}
				cleared = false;
				pool_sync.broadcast();
			}
			pool_mutex.unlock();
		}

		EXIT();
	}

	void clear_pool() {
		ENTER();

		Mutex::Autolock autolock(pool_mutex);
		cleared = true;
		pool_sync.broadcast();
		for (auto iter = mFramePool.begin(); iter != mFramePool.end(); iter++) {
			SAFE_DELETE(*iter);
		}
		mFramePool.clear();
		total_frame_num = 0;

		EXIT();
	}

	T *obtain_frame() {
		T *frame = NULL;
		pool_mutex.lock();
		{
			if (UNLIKELY(mFramePool.empty() && (total_frame_num < MAX_FRAME_NUM))) {
				// 頻繁にプールサイズを拡張しないように前回の2倍になるように試みる
				uint32_t n = total_frame_num ? total_frame_num * 2 : 2;
				if (n > MAX_FRAME_NUM) {
					// 上限を超えないように制限する
					n = MAX_FRAME_NUM;
				}
				if (LIKELY(n > total_frame_num)) {
					// 新規追加
					n -= total_frame_num;
					for (uint32_t i = 0; i < n; i++) {
						mFramePool.push_back(createFrame(default_frame_sz));
						total_frame_num++;
					}
					LOGW("allocate new frame(s):total=%d", total_frame_num);
				} else {
					LOGW("number of allocated frame exceeds limit");
				}
			}
			if (UNLIKELY(mFramePool.empty())) {
				if (BLOCK_IF_EMPTY) {
					for (; !cleared && mFramePool.empty() ; ) {
						pool_sync.wait(pool_mutex);
					}
				} else if (CREATE_IF_EMPTY) {
					mFramePool.push_back(createFrame(default_frame_sz));
					total_frame_num++;
					LOGW("allocate new frame:total=%d", total_frame_num);
				}
			}
			if (!mFramePool.empty()) {
				frame = mFramePool.back();
				mFramePool.pop_back();
			}
		}
		pool_mutex.unlock();

		return frame;
	}

	void recycle_frame(T *frame) {
		if (LIKELY(frame)) {
			pool_mutex.lock();
			{
				if (LIKELY(mFramePool.size() < MAX_FRAME_NUM)) {
					mFramePool.push_back(frame);
					frame = NULL;
				}
				if (UNLIKELY(frame)) { // frameプールに戻せなかった時
					SAFE_DELETE(frame);
					total_frame_num--;
				}
				pool_sync.signal();
			}
			pool_mutex.unlock();
		}
	}
};

#endif //USBWEBCAMERAPROJ_FRAME_POOL_H
