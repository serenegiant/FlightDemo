#ifndef GLUT_STUFF2_H
#define GLUT_STUFF2_H

#include "glutils.h"
#include "Matrix.h"
#include <assets.h>

class GLUStuff {
private:
	void init(void);
protected:
	GLuint mShaderProgram;
	GLuint mVertexShader;
	GLuint mFragmentShader;
	GLuint loc_u_projection_m;	// 射影行列のlocation
	GLuint loc_u_modelview_m;	// モデルビュー行列のlocation
	GLuint loc_u_normal_m;		// モデル行列の逆転置行列のlocation
	Matrix mProjMatrix;			// 射影行列
	Matrix mViewMatrix;			// ビュー行列
	Matrix mModelMatrix;		// モデル行列
public:
	GLUStuff(const char* pVertexSource, const char* pFragmentSource);
	GLUStuff(Assets &assets, const char *vertexfile, const char *fragmentfile);
	~GLUStuff();
	inline const GLuint getProgram(void) const { return mShaderProgram; }
	void select(void);
	// 射影行列を設定する
	void setProjectionMatrix(Matrix &projMatrix);
	// ビュー行列を設定する
	void setViewMatrix(Matrix &ViewMatrix);
	// モデル行列を設定する
	void setModelMatrix(Matrix &ViewMatrix);
	// 視錐台設定(平行投影)
	void ortho(
		const float &left, const float &right,
		const float &bottom, const float &top,
		const float &near, const float &far);
	// 視錐台設定
	void frustum(
			const float &left, const float &right,
			const float &bottom, const float &top,
			const float &near, const float &far);
	// 視錐台設定(透視投影)
	void perspective(
		const float &fovy, const float &aspect,
		const float &zNear, const float &zFar);
	// 視点設定
	void lookAt(const Vector3 &eye, const Vector3 &at, const Vector3 &up);
};

#endif //GLUT_STUFF2_H
