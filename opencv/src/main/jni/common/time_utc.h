/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef TIME_UTC_H_
#define TIME_UTC_H_

#include <time.h>

#if !defined(__LP64__)
#include <time64.h>
typedef time64_t TIME_T;

#else

typedef time_t TIME_T;

#endif

TIME_T get_current_utc();
TIME_T w3ctime_time(const char *w3c_time);
TIME_T get_timestamp_utc(const char *path);
void dump_utc(const char *tag, TIME_T utc);

#endif /* TIME_UTC_H_ */
