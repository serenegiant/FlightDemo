#/*
# * By downloading, copying, installing or using the software you agree to this license.
# * If you do not agree to this license, do not download, install,
# * copy or use the software.
# *
# *
# *                           License Agreement
# *                For Open Source Computer Vision Library
# *                        (3-clause BSD License)
# *
# * Copyright (C) 2014-2017, saki t_saki@serenegiant.com
# *
# * Redistribution and use in source and binary forms, with or without modification,
# * are permitted provided that the following conditions are met:
# *
# *   * Redistributions of source code must retain the above copyright notice,
# *     this list of conditions and the following disclaimer.
# *
# *   * Redistributions in binary form must reproduce the above copyright notice,
# *     this list of conditions and the following disclaimer in the documentation
# *     and/or other materials provided with the distribution.
# *
# *   * Neither the names of the copyright holders nor the names of the contributors
# *     may be used to endorse or promote products derived from this software
# *     without specific prior written permission.
# *
# * This software is provided by the copyright holders and contributors "as is" and
# * any express or implied warranties, including, but not limited to, the implied
# * warranties of merchantability and fitness for a particular purpose are disclaimed.
# * In no event shall copyright holders or contributors be liable for any direct,
# * indirect, incidental, special, exemplary, or consequential damages
# * (including, but not limited to, procurement of substitute goods or services;
# * loss of use, data, or profits; or business interruption) however caused
# * and on any theory of liability, whether in contract, strict liability,
# * or tort (including negligence or otherwise) arising in any way out of
# * the use of this software, even if advised of the possibility of such damage.
# */

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := common_static

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/../ \
	$(LOCAL_PATH)/ \

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)
#LOCAL_EXPORT_C_INCLUDES := \
#	$(LOCAL_PATH)/../ \
#	$(LOCAL_PATH)/ \

LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%)

LOCAL_SRC_FILES := \
	common_utils.cpp \
	Threads.cpp \
	AAtomizer.cpp \
	ABuffer.cpp \
	AMessage.cpp \
	AString.cpp \
	hexdump.cpp \
	JNIHelp.cpp \
	JniConstants.cpp \
	RefBase.cpp \
	SharedBuffer.cpp \
	VectorImpl.cpp \
	base64.cpp \
	url_escape.cpp \
	time_utc.cpp \
	Timers.cpp \
	assets.cpp \
	eglbase.cpp \
	glutils.cpp \
	glProgram.cpp \
	eglwindow.cpp \
	gltexture.cpp \
	glrenderer.cpp \
	frame_conv.cpp \
	gloffscreen.cpp \
	matrix.cpp \
	crc32.cpp \
	binutils.cpp \
	charutils.cpp \

#	matrix.cpp \
#	glutstuff.cpp \
#	gltexsurfaceoffscreen.cpp \

#EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%)
#マクロ定義
LOCAL_CFLAGS += -DANDROID_NDK
LOCAL_CFLAGS += -DNDEBUG							# LOG_ALLを無効にする・assertを無効にする場合
LOCAL_CFLAGS += -DLOG_NDEBUG						# デバッグメッセージを出さないようにする時
#LOCAL_CFLAGS += -DUSE_LOGALL						# define USE_LOGALL macro to enable all debug string
#LOCAL_CFLAGS += -DDISABLE_IMPORTGL					# when static link OpenGL|ES library
#
#LOCAL_CPPFLAGS += -fexceptions						# 例外を有効にする
#LOCAL_CPP_FEATURES += exceptions 
LOCAL_CPPFLAGS += -frtti							# RTTI(実行時型情報)を有効にする
LOCAL_CFLAGS += -Wno-multichar

#public関数のみエクスポートする
LOCAL_CFLAGS += -Wl,--version-script,common.map

#最適化設定
LOCAL_CFLAGS += -DAVOID_TABLES
LOCAL_CFLAGS += -O3 -fstrict-aliasing
LOCAL_CFLAGS += -fprefetch-loop-arrays

#アセンブラのソース(リスティングファイル)を出力させる(遅くなるけど)
#LOCAL_CFLAGS += -fverbose-asm
#LOCAL_CFLAGS +=-save-temps					# このオプションをつけると中間ファイルを削除しない(プロジェクトルートに残る)
#LOCAL_FILTER_ASM := python -c 'import sys; import shutil; src = open(sys.argv[1], "rb"); dst = open(sys.argv[2], "wb"); shutil.copyfileobj(src,dst);'

LOCAL_EXPORT_LDLIBS := -L$(SYSROOT)/usr/lib -ldl	# to avoid NDK issue(no need for static library)
LOCAL_EXPORT_LDLIBS += -llog						# log output library
LOCAL_EXPORT_LDLIBS += -landroid					# Android native related library(when you use nativeActivity etc.)
#LOCAL_EXPORT_LDLIBS += -lEGL -lGLESv1_CM			# OpenGL|ES 1.1ライブラリ
LOCAL_EXPORT_LDLIBS += -lEGL -lGLESv2				# OpenGL|ES 2.0ライブラリ
LOCAL_EXPORT_LDLIBS += -latomic

LOCAL_ARM_MODE := arm

include $(BUILD_STATIC_LIBRARY)

######################################################################
# libcommon.so
######################################################################
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_EXPORT_LDLIBS += -llog
LOCAL_EXPORT_C_INCLUDES := \
	$(LOCAL_PATH)/ \

LOCAL_WHOLE_STATIC_LIBRARIES = common_static

LOCAL_MODULE := libcommon
include $(BUILD_SHARED_LIBRARY)
