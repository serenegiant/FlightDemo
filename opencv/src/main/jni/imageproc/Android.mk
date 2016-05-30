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

