/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#define LOG_TAG "frame_conv"
#ifndef LOG_NDEBUG
#define LOG_NDEBUG			// LOGV/LOGDを出力しない時
#endif
#undef USE_LOGALL			// 指定したLOGxだけを出力

#include "utilbase.h"
#include "frame_conv.h"

#define RGB_Y(rgba) \
	(uint8_t)(((66 * (rgba)[0] + 129 * (rgba)[1] + 25 * (rgba)[2]) + 128) >> 8) + 16
#define RGB_U(rgba) \
	(uint8_t)(((-38 * (rgba)[0] - 74 * (rgba)[1] + 112 * (rgba)[2]) + 128) >> 8) + 128;
#define RGB_V(rgba) \
	(uint8_t)((( 112 * (rgba)[0] - 94 * (rgba)[1] - 18 * (rgba)[2]) + 128) >> 8) + 128;

//********************************************************************************
/**
 * yuyvからyuv420Planarに変換(y→v→u)
 * rgba/rgba/rgba/rgba => yyyy/vv/uu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels;
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels * 5 / 4;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y++) = RGB_Y(&rgba[0]);	// y
			*(y++) = RGB_Y(&rgba[4]);	// y'
			*(y++) = RGB_Y(&rgba[8]);	// y''
			*(y++) = RGB_Y(&rgba[12]);	// y'''
			if ((h & 1) == 1) {
				*(u++) = RGB_U(&rgba[0]);	// u 少し横着
				*(u++) = RGB_U(&rgba[8]);	// u 少し横着
			} else {
				*(v++) = RGB_V(&rgba[0]);	// v 少し横着
				*(v++) = RGB_U(&rgba[8]);	// v 少し横着
			}
			rgba += 16;	// (1ピクセル=4バイト)✕4ピクセル=16バイト
		}
	}
	RETURN(0, int);
}
/**
 * rgbaからyuv420Planarに変換(y→v→u)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vv/uu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels * 5 / 4;
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y++) = RGB_Y(&rgba[0]);	// y
			*(y++) = RGB_Y(&rgba[4]);	// y'
			*(y++) = RGB_Y(&rgba[8]);	// y''
			*(y++) = RGB_Y(&rgba[12]);	// y'''
			if ((h & 1) == 0) {
				*(u++) = RGB_U(&rgba[0]);	// u 少し横着
				*(u++) = RGB_U(&rgba[8]);	// u 少し横着
			} else {
				*(v++) = RGB_V(&rgba[0]);	// v 少し横着
				*(v++) = RGB_U(&rgba[8]);	// v 少し横着
			}
			rgba += 16;	// (1ピクセル=4バイト)✕4ピクセル=16バイト
		}
	}
	RETURN(0, int);
}

/**
 * yuyvからyuv420Planarに変換(y→v→u)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vv/uu
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels;
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels * 5 / 4;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y++) = yuv[0];	// y
			*(y++) = yuv[2];	// y'
			*(y++) = yuv[4];	// y''
			*(y++) = yuv[6];	// y'''
			if ((h & 1) == 1) {
				*(u++) = yuv[3];	// u
				*(u++) = yuv[7];	// u
			} else {
				*(v++) = yuv[1];	// v
				*(v++) = yuv[5];	// v
			}
			yuv += 8;	// (1ピクセル=2バイト)✕4ピクセル=8バイト
		}
	}
	RETURN(0, int);
}

//--------------------------------------------------------------------------------
/**
 * yuyvからiyuv420Planarに変換(yuyv_yuv420Planarとuとvの順が逆, y→u→v)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/uu/vv
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels * 5 / 4;	// uとvが逆
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y++) = yuv[0];	// y
			*(y++) = yuv[2];	// y'
			*(y++) = yuv[4];	// y''
			*(y++) = yuv[6];	// y'''
			if ((h & 1) == 1) {
				*(u++) = yuv[3];	// u
				*(u++) = yuv[7];	// u
			} else {
				*(v++) = yuv[1];	// v
				*(v++) = yuv[5];	// v
			}
			yuv += 8;	// (1ピクセル=2バイト)✕4ピクセル=8バイト
		}
	}
	RETURN(0, int);
}

//================================================================================
/**
 * rgbaからyuv420SemiPlanarに変換(y→vu)
 * rgba/rgba/rgba/rgba => yyyyyyyy/vuvu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh - 1; h += 2) {			// 縦方向に2ピクセルずつループ
		uint8_t *y0 = dest + dest_width_pixels * h;
		uint8_t *y1 = y0 + dest_width_pixels;
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y0++) = RGB_Y(&rgba[0]);	// y
			*(y0++) = RGB_Y(&rgba[4]);	// y'
			*(y0++) = RGB_Y(&rgba[8]);	// y''
			*(y0++) = RGB_Y(&rgba[12]);	// y'''
			*(uv++) = RGB_U(&rgba[0]);	// u
			*(uv++) = RGB_V(&rgba[0]);	// v
			*(uv++) = RGB_U(&rgba[8]);	// u
			*(uv++) = RGB_V(&rgba[8]);	// v
			*(y1++) = RGB_Y(&rgba[src_stride+0]);	// 1つ下の行のy
			*(y1++) = RGB_Y(&rgba[src_stride+4]);	// 1つ下の行のy'
			*(y1++) = RGB_Y(&rgba[src_stride+8]);	// 1つ下の行のy''
			*(y1++) = RGB_Y(&rgba[src_stride+12]);	// 1つ下の行のy'''
			rgba += 16;	// (1ピクセル=4バイト)✕4ピクセル=16バイト
		}
	}
	RETURN(0, int);
}
/**
 * rgbaからyuv420SemiPlanarに変換(y→vu)
 * rgba/rgba/rgba/rgba => yyyyyyyy/vuvu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh - 1; h += 2) {			// 縦方向に2ピクセルずつループ
		uint8_t *y0 = dest + dest_width_pixels * h;
		uint8_t *y1 = y0 + dest_width_pixels;
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y0++) = RGB_Y(rgba);		// y
			*(y0++) = RGB_Y(&rgba[4]);	// y'
			*(y0++) = RGB_Y(&rgba[8]);	// y''
			*(y0++) = RGB_Y(&rgba[12]);	// y'''
			*(uv++) = RGB_V(&rgba[0]);	// v
			*(uv++) = RGB_U(&rgba[0]);	// u
			*(uv++) = RGB_V(&rgba[8]);	// v
			*(uv++) = RGB_U(&rgba[8]);	// u
			*(y1++) = RGB_Y(&rgba[src_stride+0]);	// 1つ下の行のy
			*(y1++) = RGB_Y(&rgba[src_stride+4]);	// 1つ下の行のy'
			*(y1++) = RGB_Y(&rgba[src_stride+8]);	// 1つ下の行のy''
			*(y1++) = RGB_Y(&rgba[src_stride+12]);	// 1つ下の行のy'''
			rgba += 16;	// (1ピクセル=4バイト)✕4ピクセル=16バイト
		}
	}
	RETURN(0, int);
}
/**
 * yuyvからyuv420SemiPlanarに変換(y→vu)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vuvu
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh - 1; h += 2) {			// 縦方向に2ピクセルずつループ
		uint8_t *y0 = dest + dest_width_pixels * h;
		uint8_t *y1 = y0 + dest_width_pixels;
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y0++) = yuv[0];	// y
			*(y0++) = yuv[2];	// y'
			*(y0++) = yuv[4];	// y''
			*(y0++) = yuv[6];	// y'''
			*(uv++) = yuv[1];	// u
			*(uv++) = yuv[3];	// v
			*(uv++) = yuv[5];	// u
			*(uv++) = yuv[7];	// v
			*(y1++) = yuv[src_stride+0];	// 1つ下の行のy
			*(y1++) = yuv[src_stride+2];	// 1つ下の行のy'
			*(y1++) = yuv[src_stride+4];	// 1つ下の行のy''
			*(y1++) = yuv[src_stride+6];	// 1つ下の行のy'''
			yuv += 8;	// (1ピクセル=2バイト)✕4ピクセル=8バイト
		}
	}
	RETURN(0, int);
}

//--------------------------------------------------------------------------------
/**
 * yuyvからiyuv420SemiPlanarに変換(y→uv)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/uvuv
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh - 1; h += 2) {			// 縦方向に2ピクセルずつループ
		uint8_t *y0 = dest + dest_width_pixels * h;
		uint8_t *y1 = y0 + dest_width_pixels;
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 4) {	// 横方向に4ピクセルずつループ
			*(y0++) = yuv[0];	// y
			*(y0++) = yuv[2];	// y'
			*(y0++) = yuv[4];	// y''
			*(y0++) = yuv[6];	// y'''
			*(uv++) = yuv[3];	// v
			*(uv++) = yuv[1];	// u
			*(uv++) = yuv[7];	// v
			*(uv++) = yuv[5];	// u
			*(y1++) = yuv[src_stride+0];	// 1つ下の行のy
			*(y1++) = yuv[src_stride+2];	// 1つ下の行のy'
			*(y1++) = yuv[src_stride+4];	// 1つ下の行のy''
			*(y1++) = yuv[src_stride+6];	// 1つ下の行のy'''
			yuv += 8;	// (1ピクセル=2バイト)✕4ピクセル=8バイト
		}
	}
	RETURN(0, int);
}
//********************************************************************************
/**
 * rgbaからyuv422Planarに変換(y→v→u)
 * rgba/rgba/rgba/rgba => yyyyyyyy/vvvv/uuuu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels;
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels * 3 / 2;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = RGB_Y(&rgba[0]);	// y
			*(y++) = RGB_Y(&rgba[4]);	// y'
			*(u++) = RGB_U(&rgba[0]);	// u
			*(v++) = RGB_V(&rgba[0]);	// v
			rgba += 8;	// (1ピクセル=4バイト)✕2ピクセル=8バイト
		}
	}
	RETURN(0, int);
}
/**
 * rgbaからyuv422Planarに変換(y→v→u)
 * rgba/rgba/rgba/rgba => yyyyyyyy/vvvv/uuuu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels;
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels * 3 / 2;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = RGB_Y(&rgba[0]);	// y
			*(y++) = RGB_Y(&rgba[4]);	// y'
			*(u++) = RGB_V(&rgba[0]);	// u
			*(v++) = RGB_U(&rgba[0]);	// v
			rgba += 8;	// (1ピクセル=4バイト)✕2ピクセル=8バイト
		}
	}
	RETURN(0, int);
}
/**
 * yuyvからyuv422Planarに変換(y→v→u)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vvvv/uuuu
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels;
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels * 3 / 2;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = yuv[0];	// y
			*(y++) = yuv[2];	// y'
			*(u++) = yuv[1];	// u
			*(v++) = yuv[3];	// v
			yuv += 4;	// (1ピクセル=2バイト)✕2ピクセル=4バイト
		}
	}
	RETURN(0, int);
}

//--------------------------------------------------------------------------------
/**
 * yuyvからiyuv422Planarに変換(yuyv_yuv422Planarとuとvの順が逆, y→u→v)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/uuuu/vvvv
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	ENTER();
	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *u = dest + dest_width_pixels * dest_height_pixels;	// uとvが逆
	uint8_t *v = dest + dest_width_pixels * dest_height_pixels * 3 / 2;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = yuv[0];	// y
			*(y++) = yuv[2];	// y'
			*(u++) = yuv[1];	// u
			*(v++) = yuv[3];	// v
			yuv += 4;	// (1ピクセル=2バイト)✕2ピクセル=4バイト
		}
	}
	RETURN(0, int);
}

//================================================================================
/**
 * yuyvからyuv422SemiPlanarに変換(y→vu)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vuvuvuvu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = RGB_Y(&rgba[0]);	// y
			*(y++) = RGB_Y(&rgba[4]);	// y'
			*(uv++) = RGB_V(&rgba[0]);	// v	少し横着
			*(uv++) = RGB_U(&rgba[0]);	// u	少し横着
			rgba += 8;	// (1ピクセル=4バイト)✕2ピクセル=8バイト
		}
	}
	RETURN(0, int);
}
/**
 * yuyvからiyuv422SemiPlanarに変換(y→uv)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/uvuvuvuv
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *rgba = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = RGB_Y(&rgba[0]);	// y
			*(y++) = RGB_Y(&rgba[4]);	// y'
			*(uv++) = RGB_U(&rgba[0]);	// u	少し横着
			*(uv++) = RGB_V(&rgba[0]);	// v	少し横着
			rgba += 8;	// (1ピクセル=2バイト)✕2ピクセル=4バイト
		}
	}
	RETURN(0, int);
}
/**
 * yuyvからyuv422SemiPlanarに変換(y→vu)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vuvuvuvu
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = yuv[0];	// y
			*(y++) = yuv[2];	// y'
			*(uv++) = yuv[3];	// v
			*(uv++) = yuv[1];	// u
			yuv += 4;	// (1ピクセル=2バイト)✕2ピクセル=4バイト
		}
	}
	RETURN(0, int);
}

//--------------------------------------------------------------------------------
/**
 * yuyvからiyuv422SemiPlanarに変換(y→uv)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/uvuvuvuv
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t _src_stride,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY) {

	const uint32_t hh = src_height_pixels < dest_height_pixels ? src_height_pixels : dest_height_pixels;
	uint8_t *y = dest;
	uint8_t *uv = dest + dest_width_pixels * dest_height_pixels;
	int32_t src_stride = _src_stride;
	if (flipY) {
		src += (hh - 1) * src_stride;
		src_stride = -src_stride;
	}
	for (int h = 0; h < hh; h++) {			// 縦方向に1ピクセルずつループ
		const uint8_t *yuv = src + src_stride * h;
		for (int w = 0; w < src_width_pixels; w += 2) {	// 横方向に2ピクセルずつループ
			*(y++) = yuv[0];	// y
			*(y++) = yuv[2];	// y'
			*(uv++) = yuv[1];	// u
			*(uv++) = yuv[3];	// v
			yuv += 4;	// (1ピクセル=2バイト)✕2ピクセル=4バイト
		}
	}
	RETURN(0, int);
}
//********************************************************************************
