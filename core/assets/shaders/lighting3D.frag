
#ifdef GL_ES
precision mediump float;
#endif

//main() is called once per fragment(pixel)

uniform sampler2D u_diffuseTexture;
uniform sampler2D u_specularTexture;

uniform float u_usesDiffuseTexture;
uniform float u_usesSpecularTexture;

uniform vec4 u_globalAmbient;

uniform vec4 u_spotDirection;
uniform float u_spotExponent;

uniform vec4 u_lightColor;

uniform float u_constantAttenuation;
uniform float u_linearAttenuation;
uniform float u_quadraticAttenuation;

uniform float u_materialshine;
uniform vec4 u_materialDiffuse;
uniform vec4 u_materialSpecular;

varying vec2 v_uv;
varying vec4 v_normal;
varying vec4 v_s;
varying vec4 v_h;

void main()
{
	/* Lighting */
	//for each light
	
	vec4 materialDiffuse;
	if(u_usesDiffuseTexture == 1.0){
		materialDiffuse = texture2D(u_diffuseTexture, v_uv) * u_materialDiffuse;
	}
	else{
		materialDiffuse = u_materialDiffuse;
	}
	
	vec4 materialSpecular;
	if(u_usesSpecularTexture == 1.0){
		materialSpecular = texture2D(u_specularTexture, v_uv) * u_materialSpecular;
	}
	else{
		materialSpecular = u_materialSpecular;
	}
	
	float length_s = length(v_s);
	float length_n = length(v_normal);
	
	float lambert = min(0.0, dot(v_normal, v_s) / (length_n * length_s));
	float phong = min(0.0, dot(v_normal, v_h) / (length_n * length(v_h)));
	
	vec4 diffuseColor = lambert * u_lightColor * materialDiffuse;
	vec4 specularColor = pow(phong, u_materialshine) * u_lightColor * materialSpecular;
	vec4 ambience = u_globalAmbient * materialDiffuse;
	
	float attenuation = 1.0;
	if(u_spotExponent != 0.0){
		float spotAttenuation = max(0.0, dot(-v_s, u_spotDirection) / (length_s * length(u_spotDirection)));
		spotAttenuation = pow(spotAttenuation, u_spotExponent);
		attenuation *= spotAttenuation;
	}

	attenuation *= 1.0 /(u_constantAttenuation + length_s * u_linearAttenuation + pow(length_s, 2) * u_quadraticAttenuation);
	
	//end for each
	
	gl_FragColor = ambience + (attenuation * (diffuseColor + specularColor));
}