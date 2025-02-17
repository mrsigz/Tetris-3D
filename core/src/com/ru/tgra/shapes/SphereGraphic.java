package com.ru.tgra.shapes;

import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;

public class SphereGraphic {

	private static FloatBuffer vertexBuffer;
	private static FloatBuffer normalBuffer;
	private static FloatBuffer uvBuffer;

	private static int stacks = 60;
	private static int slices = 60;
	private static int vertexCount;
	
	public static void create() {	
		//VERTEX ARRAY IS FILLED HERE

		vertexCount = 0;
		float[] array = new float[(stacks)*(slices+1)*6];
		float[] uvArray = new float[(stacks)*(slices+1)*4];
		
		float stackInterval = (float)Math.PI / (float)stacks;
		float sliceInterval = 2.0f*(float)Math.PI / (float)slices;
		float stackAngle, sliceAngle;
		for(int stackCount = 0; stackCount < stacks; stackCount++)
		{
			stackAngle = stackCount * stackInterval;
			for(int sliceCount = 0; sliceCount < slices+1; sliceCount++)
			{
				sliceAngle = sliceCount * sliceInterval;
				array[vertexCount*3] = 	 (float)Math.sin(stackAngle) * (float)Math.cos(sliceAngle);
				array[vertexCount*3 + 1] = (float)Math.cos(stackAngle);
				array[vertexCount*3 + 2] = (float)Math.sin(stackAngle) * (float)Math.sin(sliceAngle);

				uvArray[vertexCount * 2] = (float)(sliceCount) / (float)(slices);
				uvArray[vertexCount * 2 + 1] = (float)(stackCount) / (float)(stacks);
				
				array[vertexCount*3 + 3] = (float)Math.sin(stackAngle + stackInterval) * (float)Math.cos(sliceAngle);
				array[vertexCount*3 + 4] = (float)Math.cos(stackAngle + stackInterval);
				array[vertexCount*3 + 5] = (float)Math.sin(stackAngle + stackInterval) * (float)Math.sin(sliceAngle);
				
				uvArray[vertexCount * 2 + 2] = (float)(sliceCount) / (float)(slices);
				uvArray[vertexCount * 2 + 3] = (float)(stackCount + 1) / (float)(stacks);
				
				vertexCount += 2;
			}
		}
		vertexBuffer = BufferUtils.newFloatBuffer(vertexCount*3);
		BufferUtils.copy(array, 0, vertexBuffer, vertexCount*3);
		vertexBuffer.rewind();
		normalBuffer = BufferUtils.newFloatBuffer(vertexCount*3);
		BufferUtils.copy(array, 0, normalBuffer, vertexCount*3);
		normalBuffer.rewind();
		uvBuffer = BufferUtils.newFloatBuffer(vertexCount*2);
		BufferUtils.copy(uvArray, 0, uvBuffer, vertexCount*2);
		uvBuffer.rewind();
	}

	public static void drawSolidSphere(Shader shader, Texture diffuseTexture, Texture specularTexture) {
		shader.setDiffuseTexture(diffuseTexture);
		shader.setSpecularTexture(specularTexture);

		Gdx.gl.glVertexAttribPointer(shader.getVertexPointer(), 3, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glVertexAttribPointer(shader.getNormalPointer(), 3, GL20.GL_FLOAT, false, 0, normalBuffer);
		Gdx.gl.glVertexAttribPointer(shader.getUVPointer(), 2, GL20.GL_FLOAT, false, 0, uvBuffer);

		for(int i = 0; i < vertexCount; i += (slices+1)*2)
		{
			Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, i, (slices+1)*2);
		}


	}

	public static void drawOutlineSphere(Shader shader, Texture diffuseTexture, Texture specularTexture) {
		shader.setDiffuseTexture(diffuseTexture);
		shader.setSpecularTexture(specularTexture);

		Gdx.gl.glVertexAttribPointer(shader.getVertexPointer(), 3, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glVertexAttribPointer(shader.getNormalPointer(), 3, GL20.GL_FLOAT, false, 0, normalBuffer);
		Gdx.gl.glVertexAttribPointer(shader.getUVPointer(), 2, GL20.GL_FLOAT, false, 0, uvBuffer);

		for(int i = 0; i < vertexCount; i += (slices+1)*2)
		{
			Gdx.gl.glDrawArrays(GL20.GL_LINE_STRIP, i, (slices+1)*2);
		}
	}

}
