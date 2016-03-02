/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
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


