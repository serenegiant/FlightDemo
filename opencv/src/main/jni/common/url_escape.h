/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef URL_ESCAPE_H_
#define URL_ESCAPE_H_

#include <string>

#define H2B(a) (a <= '9' ? a - '0' : (a <= 'Z' ? a - 'A' + 0x0a : a <= 'z' ? a - 'a' + 0x0a : 0))

::std::string url_encode(const ::std::string& src);
::std::string url_decode(const ::std::string &_src);


#endif /* URL_ESCAPE_H_ */
