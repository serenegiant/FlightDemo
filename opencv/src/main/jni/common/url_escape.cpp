/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
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

#include <sstream>
#include <algorithm>

#include "url_escape.h"

static const char CHARS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

//static inline bool is_safe_char(char c) {  return isalnum(c) || (c == '.') || (c =='-') || (c == '_') || (c == '*'); }

// urlで使用可能な文字
static const std::string url_letters("-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~");

/**
 * 指定したstd::string文字列をurlエスケープして返す
 * FIXME 複数回通すと複数回エスケープされてしまう(フラグを設ける)
 */
std::string url_encode(const std::string &_src) {
    std::stringstream ss;

    const char *p = _src.c_str();
    const int n = _src.size();
//	for (std::string::const_iterator p = src.begin(); p != src.end(); ++p) {
    for (int i = 0; i < n; i++, p++) {
		if (std::binary_search(url_letters.begin(), url_letters.end(), *p))
        	ss << *p;
        else
        	ss << "%" << CHARS[(*p & 0xf0) >> 4] << CHARS[*p & 0xf];
    }

    return ss.str();
}

#define H2B(a) (a <= '9' ? a - '0' : (a <= 'Z' ? a - 'A' + 0x0a : a <= 'z' ? a - 'a' + 0x0a : 0))

std::string url_decode(const std::string &_src) {
    std::stringstream ss;

    const char *src = _src.c_str();
    const int n = strlen(src);
    const char *end = src + n;
    uint8_t a, b;
    for ( ; src < end ; ) {
    	switch (*src) {
    	case '%':
    		if (src + 2 < end) {
    			a = *(src + 1);
    			b = *(src + 2);
    			ss << (char)(H2B(a) * 16 + H2B(b));
    			src += 2;
    		} else if (src + 1 < end) {
    			ss << (char)(H2B(*(src+1)));
    			src++;
    		}
    		break;
    	case '+':
        	ss << ' ';
        	break;
    	default:
        	ss << *src;
        	break;
    	}
    	src++;
    }

    return ss.str();
}


