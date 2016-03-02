LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=off
#OPENCV_LIB_TYPE:=SHARED
include $(LOCAL_PATH)/../opencv/OpenCV.mk

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/ \
	$(LOCAL_PATH)/../opencv/include \

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
LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -landroid					# Android native related library(when you use nativeActivity etc.)
LOCAL_LDLIBS += -lz							# zlib これを入れとかんとOpenCVのリンクに失敗する
LOCAL_LDLIBS += -lm
LOCAL_LDLIBS += -ldl

LOCAL_SHARED_LIBRARIES := common libopencv_java3

LOCAL_SRC_FILES := \
	ImageProcessor.cpp \

LOCAL_ARM_MODE := arm
LOCAL_MODULE := imageproc
include $(BUILD_SHARED_LIBRARY)

