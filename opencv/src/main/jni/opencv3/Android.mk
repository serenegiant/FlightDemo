LOCAL_PATH := $(call my-dir)

################################################################################
# OpenCV JNI interface library
# 数学関数とかの共通関数はこの中に入っているみたいなので必ず入れないとダメっぽい
################################################################################
include $(CLEAR_VARS)
LOCAL_MODULE := libopencv_java3
LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE)$(TARGET_SONAME_EXTENSION)
include $(PREBUILT_SHARED_LIBRARY)
