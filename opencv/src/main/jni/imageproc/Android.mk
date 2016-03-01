LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_MODULES := libopencv_core
OPENCV_MODULES += libopencv_calib3d
OPENCV_MODULES += libopencv_imgproc
OPENCV_MODULES += libopencv_stitching
OPENCV_MODULES += libopencv_superres
OPENCV_MODULES += libopencv_features2d
OPENCV_MODULES += libopencv_ml
OPENCV_MODULES += libopencv_video
OPENCV_MODULES += libopencv_flann
OPENCV_MODULES += libopencv_objdetect
OPENCV_MODULES += libopencv_videoio
OPENCV_MODULES += libopencv_highgui
OPENCV_MODULES += libopencv_photo
OPENCV_MODULES += libopencv_videostab
OPENCV_MODULES += libopencv_imgcodecs
OPENCV_MODULES += libopencv_shape

3RDPARTY_MODULES += libIlmImf
3RDPARTY_MODULES += liblibjasper
3RDPARTY_MODULES += liblibjpeg
3RDPARTY_MODULES += liblibpng
3RDPARTY_MODULES += liblibtiff
3RDPARTY_MODULES += liblibwebp
3RDPARTY_MODULES += libtbb

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -ldl	# to avoid NDK issue(no need for static library)
LOCAL_LDLIBS += -llog

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/ \
	$(LOCAL_PATH)/../opencv/include \

LOCAL_STATIC_LIBRARIES := $(OPENCV_MODULES)
LOCAL_STATIC_LIBRARIES += $(3RDPARTY_MODULES)
LOCAL_MODULE := libopencv310

include $(BUILD_SHARED_LIBRARY)

