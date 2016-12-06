/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#include <cassert>
#include <limits>
#include <stdexcept>
#include <cctype>
#include <iostream>
#include <iomanip>
#include <sstream>

#include "base64.h"
#include "utilbase.h"
#include "ABuffer.h"
#include "AString.h"

static const char b64_table[65] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static const char reverse_table[128] = { 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
		64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
		64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 62, 64, 64,
		64, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 64, 64, 64, 64, 64, 64,
		64, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
		19, 20, 21, 22, 23, 24, 25, 64, 64, 64, 64, 64, 64, 26, 27, 28, 29, 30,
		31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
		49, 50, 51, 64, 64, 64, 64, 64 };

::std::string base64_encode(::std::vector<uint8_t>src) {
	return base64_encode(&src[0], src.size());
}

std::string base64_encode(const uint8_t *src, const size_t src_len) {
	ENTER();

	std::stringstream ss;

    int i = 0;
	int j = 0;
	size_t in_len = src_len;
	unsigned char char_array_3[3];
	unsigned char char_array_4[4];

	while (in_len--) {
		char_array_3[i++] = *(src++);
		if (i == 3) {
			char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
			char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
			char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);
			char_array_4[3] = char_array_3[2] & 0x3f;

			for(i = 0; (i <4) ; i++)
				ss << b64_table[char_array_4[i]];
			i = 0;
		}
	}

	if (i) {
		for(j = i; j < 3; j++)
			char_array_3[j] = '\0';

		char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
		char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
		char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);
		char_array_4[3] = char_array_3[2] & 0x3f;

		for (j = 0; (j < i + 1); j++)
			ss << b64_table[char_array_4[j]];

		while((i++ < 3))
			ss << '=';

	}

	RET(ss.str());
}

std::vector<uint8_t> base64_decode(const std::string &ascdata) {
	ENTER();

	std::ostringstream os;
	const std::string::const_iterator last = ascdata.end();
	int bits_collected = 0;
	unsigned int accumulator = 0;

	for (std::string::const_iterator i = ascdata.begin(); i != last; ++i) {
		const int c = *i;
		if (std::isspace(c) || c == '=') {
			// Skip whitespace and padding. Be liberal in what you accept.
			continue;
		}
		if ((c > 127) || (c < 0) || (reverse_table[c] > 63)) {
			LOGE("This contains characters not legal in a base64 encoded string.");
			break;
		}
		accumulator = (accumulator << 6) | reverse_table[c];
		bits_collected += 6;
		if (bits_collected >= 8) {
			bits_collected -= 8;
			os << (char) ((accumulator >> bits_collected) & 0xffu);
		}
	}
	std::vector<uint8_t> buf(os.str().size());   		// ストリームのサイズ分の領域を確保
	memcpy(&buf[0], os.str().data(), os.str().size());	// バッファにコピー
	RET(buf);
}

namespace android {

sp<ABuffer> decodeBase64(const AString &s) {
    if ((s.size() % 4) != 0) {
        return NULL;
    }

    size_t n = s.size();
    size_t padding = 0;
    if (n >= 1 && s.c_str()[n - 1] == '=') {
        padding = 1;

        if (n >= 2 && s.c_str()[n - 2] == '=') {
            padding = 2;

            if (n >= 3 && s.c_str()[n - 3] == '=') {
                padding = 3;
            }
        }
    }

    size_t outLen = 3 * s.size() / 4 - padding;

    sp<ABuffer> buffer = new ABuffer(outLen);

    uint8_t *out = buffer->data();
    size_t j = 0;
    uint32_t accum = 0;
    for (size_t i = 0; i < n; ++i) {
        char c = s.c_str()[i];
        unsigned value;
        if (c >= 'A' && c <= 'Z') {
            value = c - 'A';
        } else if (c >= 'a' && c <= 'z') {
            value = 26 + c - 'a';
        } else if (c >= '0' && c <= '9') {
            value = 52 + c - '0';
        } else if (c == '+') {
            value = 62;
        } else if (c == '/') {
            value = 63;
        } else if (c != '=') {
            return NULL;
        } else {
            if (i < n - padding) {
                return NULL;
            }

            value = 0;
        }

        accum = (accum << 6) | value;

        if (((i + 1) % 4) == 0) {
            out[j++] = (accum >> 16);

            if (j < outLen) { out[j++] = (accum >> 8) & 0xff; }
            if (j < outLen) { out[j++] = accum & 0xff; }

            accum = 0;
        }
    }

    return buffer;
}

static char encode6Bit(unsigned x) {
    if (x <= 25) {
        return 'A' + x;
    } else if (x <= 51) {
        return 'a' + x - 26;
    } else if (x <= 61) {
        return '0' + x - 52;
    } else if (x == 62) {
        return '+';
    } else {
        return '/';
    }
}

void encodeBase64(
        const void *_data, size_t size, AString *out) {
    out->clear();

    const uint8_t *data = (const uint8_t *)_data;

    size_t i;
    for (i = 0; i < (size / 3) * 3; i += 3) {
        uint8_t x1 = data[i];
        uint8_t x2 = data[i + 1];
        uint8_t x3 = data[i + 2];

        out->append(encode6Bit(x1 >> 2));
        out->append(encode6Bit((x1 << 4 | x2 >> 4) & 0x3f));
        out->append(encode6Bit((x2 << 2 | x3 >> 6) & 0x3f));
        out->append(encode6Bit(x3 & 0x3f));
    }
    switch (size % 3) {
        case 0:
            break;
        case 2:
        {
            uint8_t x1 = data[i];
            uint8_t x2 = data[i + 1];
            out->append(encode6Bit(x1 >> 2));
            out->append(encode6Bit((x1 << 4 | x2 >> 4) & 0x3f));
            out->append(encode6Bit((x2 << 2) & 0x3f));
            out->append('=');
            break;
        }
        default:
        {
            uint8_t x1 = data[i];
            out->append(encode6Bit(x1 >> 2));
            out->append(encode6Bit((x1 << 4) & 0x3f));
            out->append("==");
            break;
        }
    }
}

}  // namespace android

