/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef BASE64_H_
#define BASE64_H_

#include <string>
#include <vector>
#include "RefBase.h"

::std::string base64_encode(const uint8_t *src, const size_t src_len);
::std::string base64_encode(::std::vector<uint8_t>src);
std::vector<uint8_t> base64_decode(const ::std::string &ascdata);

namespace android {

struct ABuffer;
struct AString;

sp<ABuffer> decodeBase64(const AString &s);
void encodeBase64(const void *data, size_t size, AString *out);

}  // namespace android


#endif /* BASE64_H_ */
