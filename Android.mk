LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := com.android.support:appcompat-v7:21.0.3
LOCAL_STATIC_JAVA_LIBRARIES += eu.chainfire:libsuperuser:1.0.0.+

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := RomAddons


LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.cardview
LOCAL_AAPT_FLAGS += --extra-packages eu.chainfire:libsuperuser:1.0.0.+

#--extra-packages android.support.v7.appcompat:android.support.v7.cardview:android.support.v7.recyclerview:android.support.design

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
