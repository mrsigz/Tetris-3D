package com.ru.tgra.shapes;

import com.badlogic.gdx.graphics.Texture;

public class Particle {
	Texture emissionTexture;
	Texture specularTexture;
	Point3D position;
	
	public Particle(Point3D pos, Texture emTex, Texture specTex){
		this.emissionTexture = emTex;
		this.specularTexture = specTex;
		this.position = pos;
	}

	public void draw(Shader shader, float angle){
		float s = (float)Math.sin(angle * Math.PI / 180.0);
		float c = (float)Math.cos(angle * Math.PI / 180.0);
		
		ModelMatrix.main.pushMatrix();
		ModelMatrix.main.loadIdentityMatrix();
		ModelMatrix.main.addTranslation(position.x + c, position.y, position.z + s);
		//ModelMatrix.main.addScale(0.02f, 0.02f, 0.02f);
		shader.setMaterialDiffuse(0.1f, 0.1f, 0.1f, 1.0f);
		shader.setMaterialSpecular(0.0f, 0.0f, 0.0f, 1.0f);
		//shader.setMaterialEmission(1.0f, 1.0f, 1.0f, 1.0f);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		SpriteGraphic.drawSprite(shader, emissionTexture, specularTexture);
		ModelMatrix.main.popMatrix();
	}
}
