package com.ru.tgra.shapes;

import java.nio.FloatBuffer;

import com.badlogic.gdx.utils.BufferUtils;

public class Camera {
	
	Point3D eye;
	/* Coordinate frame of the camera */
	Vector3D u;
	Vector3D v;
	Vector3D n;
	
	boolean orthographic;
	
	float left;
	float right;
	float bottom;
	float top;
	float near;
	float far;
	
	private FloatBuffer matrixBuffer;
	
	public Camera(){
		matrixBuffer = BufferUtils.newFloatBuffer(16);

		eye = new Point3D();
		u = new Vector3D(1,0,0);
		v = new Vector3D(0,1,0);
		n = new Vector3D(0,0,1);
		
		orthographic = true;
		
		this.left = -1;
		this.right = 1;
		this.bottom = -1;
		this.top = 1;
		this.near = -1;
		this.far = 1;
	}
	
	//copy constructor
	public Camera(Camera cam)
	{
		matrixBuffer = BufferUtils.newFloatBuffer(16);

		this.eye = new Point3D(cam.getEye());
		u = new Vector3D(cam.getU());
		v = new Vector3D(cam.getV());
		n = new Vector3D(cam.getN());
		
		orthographic = true;
		
		this.left = cam.left;
		this.right = cam.right;
		this.bottom = cam.bottom;
		this.top = cam.top;
		this.near = cam.near;
		this.far = cam.far;
	}
	
	/* Building the coordinate frame */
	public void look(Point3D eye, Point3D center, Vector3D up){
		this.eye.set(eye.x,eye.y,eye.z); //set camera to the position we are at
		n = Vector3D.difference(eye, center); //set back vector to the difference to where we are and where we are looking
		u = up.cross(n);
		n.normalize();
		u.normalize();
		v = n.cross(u);
	}
	
	public Vector3D getU(){
		return this.u;
	}
	public Vector3D getV(){
		return this.v;
	}
	public Vector3D getN(){
		return this.n;
	}
	
	public void setEye(float x, float y, float z){
		eye.set(x, y, z);
	}
	
	public Point3D getEye(){
		return eye;
	}
	
	public float getNear(){
		return near;
	}
	
	/* Move camera along it's own axis */
	public void slide(float delU, float delV, float delN){
		eye.x += delU*u.x + delV*u.x + delN*n.x;
		//eye.y += delU*u.y + delV*u.y + delN*n.y; //keep camera on the plane
		eye.z += delU*u.z + delV*u.z + delN*n.z;
	}
	
	public void roll(float angle){
		float radians = angle * (float)Math.PI /180.0f;
		float c = (float)Math.cos(radians);
		float s = (float)Math.sin(radians);
		Vector3D t = new Vector3D(u.x, u.y, u.z);
		
		u.set(t.x * c - v.x * s,  t.y * c - v.y * s,  t.z * c - v.z * s);
		v.set(t.x * s + v.x * c,  t.y*s + v.y*c,  t.z*s + v.z*c);
	}
	
	public void yaw(float angle){
		float radians = angle * (float)Math.PI /180.0f;
		float c = (float)Math.cos(radians);
		float s = (float)Math.sin(radians);
		Vector3D t = new Vector3D(u.x, u.y, u.z);
		
		u.set(t.x*c - n.x*s,  t.y*c - n.y*s,  t.z*c - n.z*s);
		n.set(t.x*s + n.x*c,  t.y*s + n.y*c,  t.z*s + n.z*c);
	}
	
	public void rotate(float angle){
		float radians = angle * (float)Math.PI /180.0f;
		float c = (float)Math.cos(radians);
		float s = (float)Math.sin(radians);
		
		u.set(u.x*c + u.z*s,  u.y,  u.z*c - u.x*s);
		v.set(v.x*c + v.z*s,  v.y,  v.z*c - v.x*s);
		n.set(n.x*c + n.z*s,  n.y,  n.z*c - n.x*s);
	}
	
	public void pitch(float angle){
		float radians = angle * (float)Math.PI /180.0f;
		float c = (float)Math.cos(radians);
		float s = (float)Math.sin(radians);
		Vector3D t = new Vector3D(n.x, n.y, n.z);
		
		n.set(t.x*c - v.x*s,  t.y*c - v.y*s,  t.z*c - v.z*s);
		v.set(t.x*s + v.x*c,  t.y*s + v.y*c,  t.z*s + v.z*c);
	}
	
	public void perspectiveProjection(float fov, float ratio, float near, float far){
		this.top = near * (float)Math.tan(((double)fov / 2.0) * Math.PI / 180.0f);
		this.bottom = -top;
		this.right = ratio * top;
		this.left = -right;
		this.near = near;
		this.far = far;
		orthographic = false;
	}
	
	public void orthographicProjection(float left, float right, float bottom, float top, float near, float far){
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.top = top;
		this.near = near;
		this.far = far;
		orthographic = true;
	}
	

	/* Add circle to camera class for collision detection */
	/*public void draw() {
		ModelMatrix.main.pushMatrix();
		//set circle to follow eye
		ModelMatrix.main.addTranslation(eye.x, eye.y, eye.z);
		//scale circle to be within camera culling
		ModelMatrix.main.addScale(near, near, near);
		ModelMatrix.main.setShaderMatrix();
		SphereGraphic.drawOutlineSphere();
		ModelMatrix.main.popMatrix();	
	}*/
	
	public FloatBuffer getViewMatrix(){
		float[] pm = new float[16];
		Vector3D minusEye = new Vector3D(-eye.x, -eye.y,-eye.z);
		
		/* Build inverse of model matrix */
		pm[0] = u.x; pm[4] = u.y; pm[8] = u.z; pm[12] = minusEye.dot(u);
		pm[1] = v.x; pm[5] = v.y; pm[9] = v.z; pm[13] = minusEye.dot(v);
		pm[2] = n.x; pm[6] = n.y; pm[10] = n.z; pm[14] = minusEye.dot(n);
		pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = 0.0f; pm[15] = 1.0f;
		
		matrixBuffer.put(pm);
		matrixBuffer.rewind();
		
		return matrixBuffer;
	}
	
	public FloatBuffer getProjectionMatrix(){
		float[] pm = new float[16];
		
		if(orthographic){
			pm[0] = 2.0f / (right - left); pm[4] = 0.0f; pm[8] = 0.0f; pm[12] = -(right + left) / (right - left);
			pm[1] = 0.0f; pm[5] = 2.0f / (top - bottom); pm[9] = 0.0f; pm[13] = -(top + bottom) / (top - bottom);
			pm[2] = 0.0f; pm[6] = 0.0f; pm[10] = 2.0f / (near - far); pm[14] = (near + far) / (near - far);
			pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = 0.0f; pm[15] = 1.0f;
		}else{
			pm[0] = (2.0f * near) / (right - left); pm[4] = 0.0f; pm[8] = (right + left) / (right - left); pm[12] = 0.0f;
			pm[1] = 0.0f; pm[5] = (2.0f * near) / (top - bottom); pm[9] = (top + bottom) / (top - bottom); pm[13] = 0.0f;
			pm[2] = 0.0f; pm[6] = 0.0f; pm[10] = -(far + near) / (far - near); 	pm[14] = -(2.0f * far * near) / (far - near);
			pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = -1.0f; pm[15] = 0.0f;
		}
		
		matrixBuffer = BufferUtils.newFloatBuffer(16);
		matrixBuffer.put(pm);
		matrixBuffer.rewind();
		
		return matrixBuffer;
	}
}
