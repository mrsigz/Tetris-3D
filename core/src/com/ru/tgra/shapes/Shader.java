package com.ru.tgra.shapes;

import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class Shader {
	
	//Local variables for program ID's I intend to use
	private int renderingProgramID;
	private int vertexShaderID;
	private int fragmentShaderID;

	//Local variables for storing the locations of variables in the shader program
	private int positionLoc;
	private int normalLoc;

	private int modelMatrixLoc;
	private int viewMatrixLoc;
	private int projectionMatrixLoc;

	private int eyePosLoc;
	
	private int globalAmbLoc;
	//private int colorLoc;
	private int lightPosLoc;
	private int lightColorLoc;
	private int matDifLoc;
	private int matSpecLoc;
	private int matShineLoc;

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

		modelMatrixLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_modelMatrix");
		viewMatrixLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_viewMatrix");
		projectionMatrixLoc		= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_projectionMatrix");

		eyePosLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_eyeposition");

		globalAmbLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_globalAmbient");
		lightPosLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_lightPosition");
		lightColorLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_lightColor");
		
		matDifLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_materialDiffuse");
		matSpecLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_materialSpecular");	
		matShineLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_materialshine");
		
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
	
	public int getVertexPointer(){
		//return the location of a_position from the vertex shader
		return positionLoc;
	}
	
	public int getNormalPointer(){
		//return the location of a_normal from the vertex shader
		return normalLoc;
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
