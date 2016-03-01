LOCAL_PATH := $(call my-dir)

################################################################################
# OpenCV JNI interface library
################################################################################
###include $(CLEAR_VARS)
###LOCAL_MODULE := libopencv_java3
###LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE)$(TARGET_SONAME_EXTENSION)
###include $(PREBUILT_SHARED_LIBRARY)

################################################################################
# OpenCV static library
################################################################################
include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_calib3d
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_imgproc
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_stitching
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_core
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_superres
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_features2d
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_ml
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_video
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_flann
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_objdetect
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_videoio
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_highgui
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_photo
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_videostab
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_imgcodecs
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_shape
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

################################################################################
# 3rdParty modules
################################################################################
# HDR画像用のOpenEXRライブラリ(未使用)
include $(CLEAR_VARS)
LOCAL_MODULE := libIlmImf
LOCAL_SRC_FILES := 3rdparty/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

# JPEG-2000画像用のオープンソースライブラリ(JasPer)(未使用)
include $(CLEAR_VARS)
LOCAL_MODULE := liblibjasper
LOCAL_SRC_FILES := 3rdparty/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

# JPEG画像用(未使用)
include $(CLEAR_VARS)
LOCAL_MODULE := liblibjpeg
LOCAL_SRC_FILES := 3rdparty/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

# PNG画像用(未使用)
include $(CLEAR_VARS)
LOCAL_MODULE := liblibpng
LOCAL_SRC_FILES := 3rdparty/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

# TIFF画像用(未使用)
include $(CLEAR_VARS)
LOCAL_MODULE := liblibtiff
LOCAL_SRC_FILES := 3rdparty/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

# WebP画像用(未使用)
include $(CLEAR_VARS)
LOCAL_MODULE := liblibwebp
LOCAL_SRC_FILES := 3rdparty/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)

# Intel Threading Building Blocks（TBB）ライブラリ
# マルチコアCPUでの処理の並列化・高速化
include $(CLEAR_VARS)
LOCAL_MODULE := libtbb
LOCAL_SRC_FILES := 3rdparty/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE).a
include $(PREBUILT_STATIC_LIBRARY)
