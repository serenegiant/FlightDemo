/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
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

#ifndef ENDIAN_UNALIGNED_H_
#define ENDIAN_UNALIGNED_H_

#include <endian.h>

// 64ビットアクセスしたいけど先頭アドレスが8バイト境界にアライメントしてない時は
// バイトアクセスして自前で計算しないとダメかもしれないので自前で準備
inline uint64_t letoh64_unaligned(const uint64_t *_p) {
	const uint8_t *p = (const uint8_t *)_p;
	return (uint64_t)((p)[0]
	 | ((uint64_t)(p)[1] << 8)
	 | ((uint64_t)(p)[2] << 16)
	 | ((uint64_t)(p)[3] << 24)
	 | ((uint64_t)(p)[4] << 32)
	 | ((uint64_t)(p)[5] << 40)
	 | ((uint64_t)(p)[6] << 48)
	 | ((uint64_t)(p)[7] << 56));
}

inline uint64_t letoh64_unaligned(const uint8_t *p) {
	return (uint64_t)((p)[0]
	 | ((uint64_t)(p)[1] << 8)
	 | ((uint64_t)(p)[2] << 16)
	 | ((uint64_t)(p)[3] << 24)
	 | ((uint64_t)(p)[4] << 32)
	 | ((uint64_t)(p)[5] << 40)
	 | ((uint64_t)(p)[6] << 48)
	 | ((uint64_t)(p)[7] << 56));
}

// 64ビットアクセスしたいけど先頭アドレスが8バイト境界にアライメントしてない時は
// バイトアクセスして自前で計算しないとダメかもしれないので自前で準備
inline void htole64_unaligned(const uint64_t &i, uint64_t *_p) {
	uint8_t *p = (uint8_t *)_p;
	(p)[0] = (i);
    (p)[1] = (i) >> 8;
    (p)[2] = (i) >> 16;
    (p)[3] = (i) >> 24;
    (p)[4] = (i) >> 32;
    (p)[5] = (i) >> 40;
    (p)[6] = (i) >> 48;
    (p)[7] = (i) >> 56;
}

inline void htole64_unaligned(const uint64_t &i, uint8_t *p) {
	(p)[0] = (i);
    (p)[1] = (i) >> 8;
    (p)[2] = (i) >> 16;
    (p)[3] = (i) >> 24;
    (p)[4] = (i) >> 32;
    (p)[5] = (i) >> 40;
    (p)[6] = (i) >> 48;
    (p)[7] = (i) >> 56;
}

// 32ビットアクセスしたいけど先頭アドレスが4バイト境界にアライメントしてない時は
// バイトアクセスして自前で計算しないとダメかもしれないので自前で準備
inline uint32_t letoh32_unaligned(const uint32_t *_p) {
	const uint8_t *p = (const uint8_t *)_p;
	return ((uint32_t)(p)[0]
		| ((uint32_t)(p)[1] << 8)
		| ((uint32_t)(p)[2] << 16)
		| ((uint32_t)(p)[3] << 24));
}

inline uint32_t letoh32_unaligned(const uint8_t *p) {
	return ((uint32_t)(p)[0]
		| ((uint32_t)(p)[1] << 8)
		| ((uint32_t)(p)[2] << 16)
		| ((uint32_t)(p)[3] << 24));
}

// 32ビットアクセスしたいけど先頭アドレスが4バイト境界にアライメントしてない時は
// バイトアクセスして自前で計算しないとダメかもしれないので自前で準備
inline void htole32_unaligned(const uint32_t &i, uint32_t *_p) {
	uint8_t *p = (uint8_t *)_p;
	(p)[0] = (i);
    (p)[1] = (i) >> 8;
    (p)[2] = (i) >> 16;
    (p)[3] = (i) >> 24;
}

inline void htole32_unaligned(const uint32_t &i, uint8_t *p) {
	(p)[0] = (i);
    (p)[1] = (i) >> 8;
    (p)[2] = (i) >> 16;
    (p)[3] = (i) >> 24;
}

// 16ビットアクセスしたいけど先頭アドレスが2バイト境界にアライメントしてない時は
// バイトアクセスして自前で計算しないとダメかもしれないので自前で準備
inline uint16_t letoh16_unaligned(const uint16_t *_p) {
	const uint8_t *p = (const uint8_t *)_p;
	return ((uint16_t)(p)[0] | ((uint16_t)(p)[1] << 8));
}

inline uint16_t letoh16_unaligned(const uint8_t *p) {
	return ((uint16_t)(p)[0] | ((uint16_t)(p)[1] << 8));
}

// 16ビットアクセスしたいけど先頭アドレスが2バイト境界にアライメントしてない時は
// バイトアクセスして自前で計算しないとダメかもしれないので自前で準備
inline void htole16_unaligned(const uint16_t &s, uint16_t *_p) {
	uint8_t *p = (uint8_t *)_p;
	(p)[0] = (s);
    (p)[1] = (s) >> 8;
}

inline void htole16_unaligned(const uint16_t &s, uint8_t *p) {
	(p)[0] = (s);
    (p)[1] = (s) >> 8;
}

#endif /* ENDIAN_UNALIGNED_H_ */
