
#ifdef GL_ES
precision mediump float;
#endif

//main() is called once per vertex
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_uv;

//uniform variables are changed whenever we want in the code, given values by variable location in our shader class
//set in the vertex shader and used in the fragment shader
uniform mat4 u_modelMatrix;
uniform mat4 u_viewMatrix;
uniform mat4 u_projectionMatrix;

uniform vec4 u_eyeposition;
uniform vec4 u_lightPosition; //need vec3 for light position but with vec4 we can use the 4th value to separate position light and direction light

//varying variables can be set in the vertex shader and used in fragment shader
varying vec2 v_uv;
varying vec4 v_normal;
varying vec4 v_s;
varying vec4 v_h;

void main()
{
	vec4 position = vec4(a_position.x, a_position.y, a_position.z, 1.0);
	position = u_modelMatrix * position;

	vec4 normal = vec4(a_normal.x, a_normal.y, a_normal.z, 0.0);
	normal = u_modelMatrix * normal;
	
	/*Global coordinates*/
	
	/*Preparation for lighting*/
	
	v_normal = normal;
	v_s = u_lightPosition - position; //direction to the light
	vec4 v = u_eyeposition - position; //direction of the camera
	v_h = v_s + v;	
	position = u_viewMatrix * position; 

	
	/*eye coordinates*/
	
	v_uv = a_uv;
	gl_Position = u_projectionMatrix * position;
}