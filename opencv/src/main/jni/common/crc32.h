/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef CRC32_H_
#define CRC32_H_

#include <string>
#include <vector>

uint32_t crc32(const std::string &s);
uint32_t crc32(const std::vector<uint8_t> &p);

#endif /* CRC32_H_ */
