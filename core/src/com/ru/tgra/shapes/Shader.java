package com.ru.tgra.shapes;

import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class Shader {
	
	//Local variables for program ID's
	private int renderingProgramID;
	private int vertexShaderID;
	private int fragmentShaderID;

	//Local variables for storing the locations of variables in the shader program
	private int positionLoc;
	private int normalLoc;
	private int uvLoc;

	private int modelMatrixLoc;
	private int viewMatrixLoc;
	private int projectionMatrixLoc;

	private int eyePosLoc;
	
	private int globalAmbLoc;
	
	private int lightPosLoc;
	private int lightColorLoc;
	
	private int spotDirLoc;
	private int spotExpLoc;
	private int constantAttLoc;
	private int linearAttLoc;
	private int quadraticAttLoc;
	
	private int matDifLoc;
	private int matSpecLoc;
	private int matShineLoc;
	private int matEmissionLoc;
	
	private int difTexLoc;
	private int specTexLoc;
	private int useDifTexLoc;
	private int useSpecTexLoc;
	
	public boolean usesDiffuseTexture;
	public boolean usesSpecularTexture;

	public Shader(){
		//string variables for holding the shader program code
		String vertexShaderString;
		String fragmentShaderString;
		
		//reading code files into a string as text
		//program code for vertex shader
		vertexShaderString = Gdx.files.internal("shaders/lighting3D.vert").readString();
		//program code for fragment shader
		fragmentShaderString =  Gdx.files.internal("shaders/lighting3D.frag").readString();
		
		/* Ask openGL to create a shader program, telling it what type the shader should be and asking 
		 * it to return an ID so from now on I know the identity of the shader I am using. 
		 * I can use several different shaders and getting ID's for each of them I can tell openGL
		 * to compile and use different shaders at different times.
		 * The programs created are empty programs until I link a code string to the ID and 
		 * ask openGL to compile the shader given a specific ID
		 * */
		//Ask for a vertex shader ID
		vertexShaderID = Gdx.gl.glCreateShader(GL20.GL_VERTEX_SHADER);
		//Ask for a fragment shader ID
		fragmentShaderID = Gdx.gl.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		//Link the vertex program source-code to the vertex shader program ID
		Gdx.gl.glShaderSource(vertexShaderID, vertexShaderString);
		//Link the fragment program source-code to the fragment shader program ID
		Gdx.gl.glShaderSource(fragmentShaderID, fragmentShaderString);
		
		//Ask openGL to compile a vertex shader given the vertex shader program ID
		Gdx.gl.glCompileShader(vertexShaderID);
		//Ask openGL to compile a fragment shader given the fragment shader program ID
		Gdx.gl.glCompileShader(fragmentShaderID);
		
		//Ask openGL to compile an empty program and return it's ID
		renderingProgramID = Gdx.gl.glCreateProgram();
		
		System.out.println(Gdx.gl.glGetShaderInfoLog(vertexShaderID));
		System.out.println(Gdx.gl.glGetShaderInfoLog(fragmentShaderID));
		
		//Attach the compiled shaders to the empty program ID by the shader ID
		Gdx.gl.glAttachShader(renderingProgramID, vertexShaderID);
		Gdx.gl.glAttachShader(renderingProgramID, fragmentShaderID);
		
		//Link the program by the program ID
		Gdx.gl.glLinkProgram(renderingProgramID);
		/*
		 * If everything compiles without errors at this point then
		 * the shader have been set up from text to program
		 */
		
		//Ask openGL to get the location of certain variables in the shader program through the
		//rendering program ID and store that location in local variables
		positionLoc				= Gdx.gl.glGetAttribLocation(renderingProgramID, "a_position");
		Gdx.gl.glEnableVertexAttribArray(positionLoc);

		normalLoc				= Gdx.gl.glGetAttribLocation(renderingProgramID, "a_normal");
		Gdx.gl.glEnableVertexAttribArray(normalLoc);
		
		uvLoc				= Gdx.gl.glGetAttribLocation(renderingProgramID, "a_uv");
		Gdx.gl.glEnableVertexAttribArray(uvLoc);

		modelMatrixLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_modelMatrix");
		viewMatrixLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_viewMatrix");
		projectionMatrixLoc		= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_projectionMatrix");

		eyePosLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_eyeposition");

		globalAmbLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_globalAmbient");
		
		lightPosLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_lightPosition");
		lightColorLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_lightColor");
		
		spotDirLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_spotDirection");
		spotExpLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_spotExponent");
		constantAttLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_constantAttenuation");
		linearAttLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_linearAttenuation");
		quadraticAttLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_quadraticAttenuation");
		
		matDifLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_materialDiffuse");
		matSpecLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_materialSpecular");	
		matShineLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_materialshine");
		matEmissionLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_materialEmission");
		
		difTexLoc               = Gdx.gl.glGetUniformLocation(renderingProgramID, "u_textureDiffuse");
		useDifTexLoc            = Gdx.gl.glGetUniformLocation(renderingProgramID, "u_usesDiffuseTexture");
		specTexLoc              = Gdx.gl.glGetUniformLocation(renderingProgramID, "u_textureSpecular");
		useSpecTexLoc           = Gdx.gl.glGetUniformLocation(renderingProgramID, "u_usesSpecularTexture");
		
		//Tell openGL the ID of the shader program to use
		Gdx.gl.glUseProgram(renderingProgramID);
	}

	public void setEyePosition(float x, float y, float z, float w){
		//populate the variable u_eyePosition in the vertex shader with the given values
		Gdx.gl.glUniform4f(eyePosLoc, x, y, z, w);
	}
	
	public void setGlobalAmbience(float r, float g, float b, float a){
		//populate the variable u_globalAmbient in the vertex shader with the given values
		Gdx.gl.glUniform4f(globalAmbLoc, r, g, b, a);
	}
	
	public void setLightPosition(float x, float y, float z, float w){
		//populate the variable u_lightPosition in the vertex shader with the given values
		Gdx.gl.glUniform4f(lightPosLoc, x, y, z, w);
	}
	
	public void setSpotDirection(float x, float y, float z, float w){
		//populate the variable u_lightDirection in the vertex shader with the given values
		Gdx.gl.glUniform4f(spotDirLoc, x, y, z, w);
	}
	
	public void setSpotExponent(float exp){
		//populate the variable u_materialshine in the vertex shader with the given values
		Gdx.gl.glUniform1f(spotExpLoc, exp);
	}
	
	public void setConstantAttenuation(float att){
		//populate the variable u_materialshine in the vertex shader with the given values
		Gdx.gl.glUniform1f(constantAttLoc, att);
	}
	
	public void setLinearAttenuation(float att){
		//populate the variable u_materialshine in the vertex shader with the given values
		Gdx.gl.glUniform1f(linearAttLoc, att);
	}
	
	public void setQuadraticAttenuation(float att){
		//populate the variable u_materialshine in the vertex shader with the given values
		Gdx.gl.glUniform1f(quadraticAttLoc, att);
	}
	
	public void setLightColor(float r, float g, float b, float a){
		//populate the variable u_color in the vertex shader with the given values
		Gdx.gl.glUniform4f(lightColorLoc, r, g, b, a);
	}
	
	public void setMaterialDiffuse(float r, float g, float b, float a){
		//populate the variable u_materialDiffuse in the vertex shader with the given values
		Gdx.gl.glUniform4f(matDifLoc, r, g, b, a);
	}
	
	public void setMaterialSpecular(float r, float g, float b, float a){
		//populate the variable u_materialSpecular in the vertex shader with the given values
		Gdx.gl.glUniform4f(matSpecLoc, r, g, b, a);
	}
	
	public void setMaterialShine(float shine){
		//populate the variable u_materialshine in the vertex shader with the given values
		Gdx.gl.glUniform1f(matShineLoc, shine);
	}
	
	public void setMaterialEmission(float x, float y, float z, float w){
		//populate the variable u_eyePosition in the vertex shader with the given values
		Gdx.gl.glUniform4f(matEmissionLoc, x, y, z, w);
	}
	
	public void setDiffuseTexture(Texture tex){
		if(tex == null){
			Gdx.gl.glUniform1f(useDifTexLoc, 0.0f);
			usesDiffuseTexture = false;
		}else{
			tex.bind(0);
			Gdx.gl.glUniform1i(difTexLoc, 0);
			Gdx.gl.glUniform1f(useDifTexLoc, 1.0f);
			usesDiffuseTexture = true;
			
			Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_REPEAT);
			Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_REPEAT);
		}
	}

	public void setSpecularTexture(Texture tex){
		if(tex == null){
			Gdx.gl.glUniform1f(useSpecTexLoc, 0.0f);
			usesSpecularTexture = false;
		}else{
			tex.bind(1);
			Gdx.gl.glUniform1i(specTexLoc, 1);
			Gdx.gl.glUniform1f(useSpecTexLoc, 1.0f);
			usesSpecularTexture = true;

			Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_REPEAT);
			Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_REPEAT);
		}
	}
	
	public boolean usesTextures(){
		return (usesDiffuseTexture||usesSpecularTexture);
	}
	
	public int getVertexPointer(){
		//return the location of a_position from the vertex shader
		return positionLoc;
	}
	
	public int getNormalPointer(){
		//return the location of a_normal from the vertex shader
		return normalLoc;
	}
	
	public int getUVPointer(){
		//return the location of a_normal from the vertex shader
		return uvLoc;
	}
	
	public void setModelMatrix(FloatBuffer matrix){
		//populate the variable u_modelMatrix in the vertex shader with the given FloatBuffer
		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, matrix);
	}
	
	public void setViewMatrix(FloatBuffer matrix){
		//populate the variable u_viewMatrix in the vertex shader with the given FloatBuffer
		Gdx.gl.glUniformMatrix4fv(viewMatrixLoc, 1, false, matrix);
	}
	
	public void setProjectionMatrix(FloatBuffer matrix){
		//populate the variable u_projectionMatrix in the vertex shader with the given FloatBuffer
		Gdx.gl.glUniformMatrix4fv(projectionMatrixLoc, 1, false, matrix);
	}
}
