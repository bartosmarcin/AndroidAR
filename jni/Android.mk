TOP_LOCAL_PATH:=$(call my-dir)
include $(call all-subdir-makefiles)
LOCAL_PATH := $(TOP_LOCAL_PATH)  

include $(CLEAR_VARS)
 
include  /home/marcin/Downloads/OpenCV-2.4.10-android-sdk/sdk/native/jni/OpenCV.mk
 
LOCAL_MODULE    := gray
LOCAL_SRC_FILES := gray.cpp
LOCAL_LDLIBS +=  -llog -ldl
 
include $(BUILD_SHARED_LIBRARY)