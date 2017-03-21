/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2014-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

#ifndef FRAME_CONV_H_
#define FRAME_CONV_H_

#include <stdint.h>

typedef int (*frameconv_func_t)(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);

//********************************************************************************
/**
 * rgbaからyuv420Planarに変換(y→v→u)
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * rgbaからiyuv420Planarに変換(yuyv_yuv420Planarとuとvの順が逆, y→u→v)
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからyuv420Planarに変換(y→v→u)
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからiyuv420Planarに変換(yuyv_yuv420Planarとuとvの順が逆, y→u→v)
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv420Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
//--------------------------------------------------------------------------------
/**
 * rgbaからyuv420SemiPlanarに変換(y→vu)
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * rgbaからiyuv420SemiPlanarに変換(y→uv)
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからyuv420SemiPlanarに変換(y→vu)
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからiyuv420SemiPlanarに変換(y→uv)
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv420SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
//================================================================================
/**
 * rgbaからyuv422Planarに変換(y→v→u)
 * rgba/rgba/rgba/rgba => yyyyyyyy/vvvv/uuuu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width, const int32_t dest_height, bool flipY);
/**
 * rgbaからiyuv422Planarに変換(yuyv_yuv422Planarとuとvの順が逆, y→u→v)
 * rgba/rgba/rgba/rgba => yyyyyyyy/uuuu/vvvv
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからyuv422Planarに変換(y→v→u)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vvvv/uuuu
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからiyuv422Planarに変換(yuyv_yuv422Planarとuとvの順が逆, y→u→v)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/uuuu/vvvv
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv422Planar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
//--------------------------------------------------------------------------------
/**
 * rgbaからyuv422SemiPlanarに変換(y→vu)
 * rgba/rgba/rgba/rgba => yyyyyyyy/vuvuvuvu
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_yuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * rgbaからiyuv422SemiPlanarに変換(y→uv)
 * rgba/rgba/rgba/rgba => yyyyyyyy/uvuvuvuv
 * 領域チェックしないので前もって確認しておくこと
 */
int rgba_iyuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからyuv422SemiPlanarに変換(y→vu)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/vuvuvuvu
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_yuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
/**
 * yuyvからiyuv422SemiPlanarに変換(y→uv)
 * yuyv/yuyv/yuyv/yuyv => yyyyyyyy/uvuvuvuv
 * 領域チェックしないので前もって確認しておくこと
 */
int yuyv_iyuv422SemiPlanar(const uint8_t *src, uint8_t *dest,
	const int32_t src_width_pixels, const int32_t src_height_pixels,
	const int32_t src_stride_bytes,
	const int32_t dest_width_pixels, const int32_t dest_height_pixels, bool flipY);
//********************************************************************************

#endif /* FRAME_CONV_H_ */
