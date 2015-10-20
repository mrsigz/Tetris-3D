
#ifdef GL_ES
precision mediump float;
#endif

uniform vec4 u_globalAmbient;
uniform vec4 u_lightColor;

//the higher the shine the higher fragmentation of light will be
uniform float u_materialshine;
uniform vec4 u_materialDiffuse;
uniform float u_materialSpecular;

//fragment color is calculated in the vertex shader
varying vec4 v_normal;
varying vec4 v_s;
varying vec4 v_h;

void main()
{
	/* Lighting */
	//for each light
	//needs s and h
	//the lambert factor tells us the intensity of light considering the direction of the light
	//taking the min of 0.0 or results to avoid negative numbers
	float lambert = min(0.0, dot(v_normal, v_s) / (length(v_normal) * length(v_s)));
	
	//phong is used for specular lighting with regards to the position of the camera
	float phong = min(0.0, dot(v_normal, v_h) / (length(v_normal) * length(v_h)));
	
	
	//multiplying vectors with vectors in shader language multiplies the vectors together component wise
	//so the final color is the intesity of the light multiplied by the color of the light and the color of the object
	
	vec4 diffuseColor = lambert * u_lightColor * u_materialDiffuse;
	vec4 specularColor = pow(phong, u_materialshine) * u_lightColor * u_materialSpecular;
	vec4 ambience = u_globalAmbient * u_materialDiffuse;
	
	// light = diffuseColor + specularColor;
	//end for each
	
	gl_FragColor = ambience + diffuseColor + specularColor; //+= u_globalAmbient * materialDiffuse + each light
	
}