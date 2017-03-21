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

#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include "utilbase.h"
#include "time_utc.h"

void dump_utc(const char *tag, TIME_T utc) {
	#if !defined(LOG_NDEBUG) && !defined(NDEBUG)
	struct tm tm;
	char buf[50];

	memset(&tm, 0, sizeof(struct tm));
#if !defined(__LP64__)
	localtime64_r(&utc, &tm);	// 指定したtime64_tがUTCを示しているとみなしてtmに変換する
	strftime(buf, 50, "%Y-%m-%dT%H:%M:%S(UTC)", &tm);	// log出力用に変換する
	LOGV("%s{%lld}{%s}", tag, utc, buf);
#else
	localtime_r(&utc, &tm);	// 指定したtime_tがUTCを示しているとみなしてtmに変換する
	strftime(buf, 50, "%Y-%m-%dT%H:%M:%S(UTC)", &tm);	// log出力用に変換する
	LOGV("%s{%ld}{%s}", tag, utc, buf);
#endif

#endif
}


TIME_T get_current_utc() {
	// 現在のローカル時間とタイムゾーンを取得
	struct timeval tv;
	struct timezone timezone;
	gettimeofday(&tv, &timezone);
	TIME_T now = tv.tv_sec;	// 秒未満は無視
	// ローカル時間をUTC[秒]に換算
	now += timezone.tz_minuteswest * 60;
	dump_utc("現在日時(UTC)", now);
	return now;
}

/**
 * W3C/RFC3339形式の時刻文字列をUTCのtime64_t形式に変換して返す
 * 「2014-10-06T17:30:00+0900…」または「2014-10-06T17:30:00+09:00…」
 */
TIME_T w3ctime_time(const char *w3c_time) {
	ENTER();
	struct tm tm;
	TIME_T result;
	char *tmp = strdup(w3c_time);
	const int n = tmp ? strlen(tmp) : 0;
	int tz = 0;
	if (n >= 25) {	// タイムゾーン付き(コロン付きかも)
		tz = ((tmp[20] - '0') * 10 + (tmp[21] - '0')) * 60;
		if (tmp[22] == ':') {
			tz += (tmp[23] - '0') * 10 + (tmp[24] - '0');
		} else {
			tz += (tmp[22] - '0') * 10 + (tmp[23] - '0');
		}
	} else if (n >= 24) {
		tz = ((tmp[20] - '0') * 10 + (tmp[21] - '0')) * 60;
		tz += (tmp[22] - '0') * 10 + (tmp[23] - '0');
	}
	if (n > 20) {
		switch (tmp[19]) {
		case '-':	tz = -tz; break;
		case '+':	break;
		default:	tz = 0;
		}
	}
	LOGV("TZ:{%d}", tz);
	if (n > 19)
		tmp[19] = 0;
	if (n > 18) {
		memset(&tm, 0, sizeof(struct tm));
		strptime(tmp, "%Y-%m-%dT%H:%M:%S", &tm);
#if !defined(__LP64__)
		result = mktime64(&tm);
#else
		result = mktime(&tm);
#endif
		result -= tz * 60;	// UTCに換算
	}
	free(tmp);

	dump_utc("end", result);
	return result;
}

/**
 * 指定したファイルが存在する場合にそのタイムスタンプを返す
 * 存在しなければNULL(0)を返す
 */
TIME_T get_timestamp_utc(const char *path) {
//	ENTER();

	TIME_T result = 0;
	if (!access(path, R_OK | W_OK)) {
		struct stat file_stat;
		struct timespec spec;
		struct timeval tv;
		struct timezone timezone;
		gettimeofday(NULL, &timezone);				// タイムゾーンオフセット時間[分]を取得
		if (!stat(path, &file_stat)) {
//			result = file_stat.st_atime;			// 最終アクセス時刻を取得。これはローカル時間
			result = file_stat.st_mtime;			// 最終修正時刻を取得。これはローカル時間
			result += timezone.tz_minuteswest * 60;	// UTCに変換
		}
	}
	return result;	//	RETURN(result, time_t);
}
