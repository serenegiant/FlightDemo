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

#ifndef LOCALDEFINES_H_
#define LOCALDEFINES_H_

#include <jni.h>

#ifndef LOG_TAG
#define LOG_TAG "imageprocessor"
#endif

#define LIBUVC_HAS_JPEG

// write back array that got by getXXXArrayElements into original Java object and release its array
#define	ARRAYELEMENTS_COPYBACK_AND_RELEASE 0
// write back array that got by getXXXArrayElements into original Java object but do not release its array
#define	ARRAYELEMENTS_COPYBACK_ONLY JNI_COMMIT
// never write back array that got by getXXXArrayElements but release its array
#define ARRAYELEMENTS_ABORT_AND_RELEASE JNI_ABORT

#define THREAD_PRIORITY_DEFAULT			0
#define THREAD_PRIORITY_LOWEST			19
#define THREAD_PRIORITY_BACKGROUND		10
#define THREAD_PRIORITY_FOREGROUND		-2
#define THREAD_PRIORITY_DISPLAY			-4
#define THREAD_PRIORITY_URGENT_DISPLAY	-8
#define THREAD_PRIORITY_AUDIO			-16
#define THREAD_PRIORITY_URGENT_AUDIO	-19

//#define USE_LOGALL	// If you don't need to all LOG, comment out this line and select follows
//#define USE_LOGV
#define USE_LOGD
#define USE_LOGI
#define USE_LOGW
#define USE_LOGE
#define USE_LOGF

#ifdef NDEBUG
#undef USE_LOGALL
#endif

#ifdef LOG_NDEBUG
#undef USE_LOGALL
#endif

// Absolute class name of Java object
// if you change the package name of AndroBulletGlue library, you must fix these
#define		JTYPE_SYSTEM				"Ljava/lang/System;"
#define		JTYPE_STRING				"Ljava/lang/String;"
#define		JTYPE_CONTEXT				"Landroid/content/Context;"
#define		JTYPE_ACTIVITY				"Landroid/app/Activity;"
#define		JTYPE_WINDOWMANAGER			"Landroid.view.WindowManager;"
#define		JTYPE_DISPLAY				"Landroid/view/Display;"
#define		JTYPE_DISPLAYMETRICS		"Landroid/util/DisplayMetrics;"
//
//
typedef		jlong						ID_TYPE;

#endif /* LOCALDEFINES_H_ */
