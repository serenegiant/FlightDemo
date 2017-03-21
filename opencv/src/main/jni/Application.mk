#
# By downloading, copying, installing or using the software you agree to this license.
# If you do not agree to this license, do not download, install,
# copy or use the software.
#
#
#                           License Agreement
#                For Open Source Computer Vision Library
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

NDK_TOOLCHAIN_VERSION := clang
#OpenGL|ES3を使うため API>=18とする
APP_PLATFORM := android-18

# Cコンパイラオプション
APP_CFLAGS += -DHAVE_PTHREADS
APP_CFLAGS += -DNDEBUG					# LOG_ALLを無効にする・assertを無効にする場合
APP_CFLAGS += -DLOG_NDEBUG				# デバッグメッセージを出さないようにする時

# C++コンパイラオプション
APP_CPPFLAGS += -std=c++0x
APP_CPPFLAGS += -fexceptions			# 例外を有効にする
APP_CPPFLAGS += -frtti					# RTTI(実行時型情報)を有効にする

# 最適化設定
APP_CFLAGS += -DAVOID_TABLES
APP_CFLAGS += -O3 -fstrict-aliasing
APP_CFLAGS += -fprefetch-loop-arrays

# 警告を消す設定
APP_CFLAGS += -Wno-parentheses
APP_CFLAGS += -Wno-switch
APP_CFLAGS += -Wno-extern-c-compat
APP_CFLAGS += -Wno-empty-body
APP_CFLAGS += -Wno-deprecated-register
APP_CPPFLAGS += -Wreturn-type
APP_CPPFLAGS += -Wno-multichar

# 出力アーキテクチャ
#APP_ABI := armeabi
#APP_ABI := armeabi-v7a
#APP_ABI := arm64-v8a
#APP_ABI := armeabi armeabi-v7a
#APP_ABI := armeabi armeabi-v7a arm64-v8a
#APP_ABI := x86
APP_ABI := armeabi-v7a x86
#APP_ABI := armeabi armeabi-v7a x86
#APP_ABI := arm64-v8a armeabi-v7a
#APP_ABI := all

# STLライブラリ
#APP_STL := stlport_static
#APP_STL := stlport_shared
#APP_STL := c++_static
#APP_STL := c++_shared
#APP_STL := gnustl_static
APP_STL := gnustl_shared

# 出力オプション
APP_OPTIM := release
#APP_OPTIM := debug