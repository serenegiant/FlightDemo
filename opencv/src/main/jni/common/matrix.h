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

#ifndef MATRIX_H_
#define MATRIX_H_

#include "vec.h"
#include "quaternion.h"

class Matrix;
//********************************************************************************
//********************************************************************************
Matrix operator*(const Matrix &m, const float &k);
Matrix operator*(const Matrix &m1, const Matrix m2);
Vector3 operator*(const Matrix &m, const Vector3 &v);
Vector3 operator*(const Vector3 &v, const Matrix &m);
Matrix operator+(const Matrix &m, const Matrix &m2);
Matrix operator-(const Matrix &m1, const Matrix &m2);

//********************************************************************************
//********************************************************************************
class Matrix {
private:
	float m_glmatrix[16];
protected:
	Vector4 m_vec[4];
public:
	Matrix() { setIdentity(); };
	Matrix(const Matrix &src);
	Matrix(const float *mat);
	Matrix(const float &xx, const float &xy, const float &xz,
			const float &yx, const float &yy, const float &yz,
			const float &zx, const float &zy, const float &zz);
	Matrix(
		const float &xx, const float &xy, const float &xz, const float &xw,
		const float &yx, const float &yy, const float &yz, const float &yw,
		const float &zx, const float &zy, const float &zz, const float &zw,
		const float &wx, const float &wy, const float &wz, const float &ww);
//	Matrix &operator=(const Matrix &other);	// デフォルトの代入演算子でOK
	const Vector4 getColumn(int i) const;
	const Vector4 &getRow(int i) const;
	Vector4 &operator[](int i);
	const Vector4 &operator[](int i) const;
	Matrix &operator*=(const Matrix &m);
	Matrix &operator+=(const Matrix &m);
	Matrix &operator-=(const Matrix &m);
	bool operator==(const Matrix &m);
	const bool operator==(const Matrix &m) const;
	Matrix &setFromOpenGLSubMatrix(const float *m);
	Matrix &set(
		const float &xx, const float &xy, const float &xz,
		const float &yx, const float &yy, const float &yz,
		const float &zx, const float &zy, const float &zz);
	Matrix &set(
		const float &xx, const float &xy, const float &xz, const float &xw,
		const float &yx, const float &yy, const float &yz, const float &yw,
		const float &zx, const float &zy, const float &zz, const float &zw,
		const float &wx, const float &wy, const float &wz, const float &ww);
	Matrix &setIdentity(void);
	void getOpenGLSubMatrix(float *m);
	const float *getOpenGLSubMatrix(void);
	operator float *();
	float *asArray();
	Matrix &scale(const float &s) const;
	Matrix scaled(const Vector3 &s) const;
	Matrix absolute() const;
	Matrix transpose() const;
	Matrix inverse() const;
	Matrix &setRotate(const Vector3 &angle);
	Matrix &setRotate(const Quaternion &q);
	Matrix &setTranslate(const Vector3 &offset);
	Matrix &setTranslate(const float &x, const float &y, const float &z);
	Matrix &setScale(const float &scale);
	Matrix &setScale(const float &sx, const float &sy, const float &sz);
	Matrix &setScale(const Vector3 &scale);
	Matrix &rotate(const Vector3 &angle);
	Matrix &rotate(const Quaternion &q);
	Matrix &translate(const Vector3 &offset);
	Matrix &translate(const float &x, const float &y, const float &z);
	Matrix &scale(const float &scale);
	Matrix &scale(const Vector3 &scale);
	Matrix &ortho(
		const float &left, const float &right,
		const float &bottom, const float &top,
		const float &near, const float &far);
	Matrix &frustum(
		const float &left, const float &right,
		const float &bottom, const float &top,
		const float &near, const float &far);
	Matrix &perspective(
		const float &fovy, const float &aspect,
		const float &zNear, const float &zFar);
	Matrix &lookAt(
		const float &eyeX, const float &eyeY, const float &eyeZ,
		const float &centerX, const float &centerY, const float &centerZ,
		const float &upX, const float &upY, const float &upZ);
	Matrix &lookAt(const Vector3 &eye, const Vector3 &at, const Vector3 &up);

	inline float cofac(int r1, int c1, int r2, int c2) const;
	inline float tdotx(const Vector3 &v) const;
	inline float tdoty(const Vector3 &v) const;
	inline float tdotz(const Vector3 &v) const;
	inline float tdotw(const Vector3 &v) const;
};

#endif /* MATRIX_H_ */
