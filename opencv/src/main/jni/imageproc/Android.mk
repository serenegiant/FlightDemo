#
# By downloading, copying, installing or using the software you agree to this license.
# If you do not agree to this license, do not download, install,
# copy or use the software.
#
#
#                           License Agreement
#                        (3-clause BSD License)
#
# Copyright (C) 2015-2017, saki t_saki@serenegiant.com
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
#   * Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
#   * Redistributions in binary form must reproduce the above copyright notice,
#     this list of conditions and the following disclaimer in the documentation
#     and/or other materials provided with the distribution.
#
#   * Neither the names of the copyright holders nor the names of the contributors
#     may be used to endorse or promote products derived from this software
#     without specific prior written permission.
#
# This software is provided by the copyright holders and contributors "as is" and
# any express or implied warranties, including, but not limited to, the implied
# warranties of merchantability and fitness for a particular purpose are disclaimed.
# In no event shall copyright holders or contributors be liable for any direct,
# indirect, incidental, special, exemplary, or consequential damages
# (including, but not limited to, procurement of substitute goods or services;
# loss of use, data, or profits; or business interruption) however caused
# and on any theory of liability, whether in contract, strict liability,
# or tort (including negligence or otherwise) arising in any way out of
# the use of this software, even if advised of the possibility of such damage.
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/ \

# OpenCV3 (3.10)を使う時
include $(LOCAL_PATH)/../opencv3/OpenCV.mk
LOCAL_SHARED_LIBRARIES := libopencv_java3
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../opencv3/include \

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%)
#マクロ定義
LOCAL_CFLAGS += -DANDROID_NDK
LOCAL_CFLAGS += -DNDEBUG							# LOG_ALLを無効にする・assertを無効にする場合
LOCAL_CFLAGS += -DLOG_NDEBUG						# デバッグメッセージを出さないようにする時
#LOCAL_CFLAGS += -DUSE_LOGALL						# define USE_LOGALL macro to enable all debug string

# public関数のみエクスポートする
LOCAL_CFLAGS += -Wl,--version-script,ImageProcessor.map

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -ldl	# to avoid NDK issue(no need for static library)
LOCAL_LDLIBS += -ldl
LOCAL_LDLIBS += -llog
#LOCAL_LDLIBS += -landroid					# Android native related library(when you use nativeActivity etc.)
LOCAL_LDLIBS += -lz							# zlib これを入れとかんとOpenCVのリンクに失敗する
LOCAL_LDLIBS += -lm
#LOCAL_LDLIBS += -lEGL -lGLESv1_CM			# OpenGL|ES 1.1ライブラリ
#LOCAL_LDLIBS += -lEGL -lGLESv2				# OpenGL|ES 2.0ライブラリ
LOCAL_LDLIBS += -lEGL -lGLESv3				# OpenGL|ES 2.0|ES 3ライブラリ
LOCAL_LDLIBS += -latomic

LOCAL_SHARED_LIBRARIES += common

LOCAL_SRC_FILES := \
	IPBase.cpp \
	IPPreprocess.cpp \
	IPFrame.cpp \
	IPDetector.cpp \
	IPDetectorLine.cpp \
	IPDetectorCurve.cpp \
	IPDetectorCorner.cpp \
	ImageProcessor.cpp \
	thinning.cpp \

LOCAL_ARM_MODE := arm
LOCAL_MODULE := imageproc
include $(BUILD_SHARED_LIBRARY)

