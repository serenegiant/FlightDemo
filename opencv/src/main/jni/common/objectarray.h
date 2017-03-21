/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2014-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

#ifndef OBJECTARRAY_H_
#define OBJECTARRAY_H_

#include "utilbase.h"

template <class T>
class ObjectArray {
private:
	T *m_elements;
	const int min_size;
	int m_max_size;
	int m_size;
public:
	ObjectArray(int initial_capacity = 2)
		: m_elements(new T[initial_capacity]),
		  m_max_size(initial_capacity),
		  m_size(0),
		  min_size(initial_capacity) {
	}

	~ObjectArray() { SAFE_DELETE_ARRAY(m_elements); }
	void size(int new_size) {
		if (new_size != capacity()) {
			T *new_elements = new T[new_size];
			LOG_ASSERT(new_elements, "out of memory:size=%d,capacity=%d", new_size, m_max_size);
			const int n = (new_size < capacity()) ? new_size : capacity();
			for (int i = 0; i < n; i++) {
				new_elements[i] = m_elements[i];
			}
			SAFE_DELETE_ARRAY(m_elements);
			m_elements = new_elements;
			m_max_size = new_size;
			m_size = (m_size < new_size) ? m_size : new_size;
		}
	}

	inline int size() const { return m_size; }
	inline bool isEmpty() const { return (m_size < 1); }
	inline int capacity() const { return m_max_size; }
	inline T &operator[](int index) { return m_elements[index]; }
	inline const T &operator[](int index) const { return m_elements[index]; }
	int put(T object) {
		if LIKELY(object) {
			if UNLIKELY(size() >= capacity()) {
				size(capacity() ? capacity() * 2 : 2);
			}
			m_elements[m_size++] = object;
		}
		return m_size;
	}
	/**
	 * remove T which posisioned on index
	 */
	T remove(int index) {
		T obj = m_elements[index];
		for (int i = index; i < m_size - 1; i++) {
			m_elements[i] = m_elements[i+1];
		}
		m_size--;
		return obj;
	}
	/**
	 * search the T object and remove if exist
	 */
	void removeObject(T object) {
		for (int i = 0; i < size(); i++) {
			if (m_elements[i] == object) {
				remove(i);
				break;
			}
		}
	}
	/**
	 * get last T and remove from this array ¥
	 * this is faster than remove(size()-1)
	 */
	inline T last() {
		if LIKELY(m_size > 0)
			return m_elements[--m_size];
		else
			return NULL;
	}
	/**
	 * search the T object and return it's index
	 * if the T is not in this array, return -1
	 */
	int getIndex(const T object) {
		int result = -1;
		for (int i = 0; i < size(); i++) {
			if (m_elements[i] == object) {
				result = i;
				break;
			}
		}
		return result;
	}

	/**
	 * clear the T array but never delete actual T instance
	 */
	inline void clear() {
		size(min_size);
		m_size = 0;
	}
};

#endif	// OBJECTARRAY_H_
