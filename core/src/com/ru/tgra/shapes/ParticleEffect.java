package com.ru.tgra.shapes;

import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class ParticleEffect {
	private Point3D position;
	private Queue<Particle> particles;
	private Texture emissionTexture;
	private Texture specularTexture;
	
	public ParticleEffect(Point3D position, Texture emissionTexture, Texture specularTexture){
		this.position = position;
		particles = new LinkedList<Particle>();
		this.emissionTexture = emissionTexture;
		this.specularTexture = specularTexture;
	}
	
	public void addParticle(){
		particles.add(new Particle(new Point3D(position), emissionTexture, specularTexture));
	}
	
	public void draw(Shader shader, float angle){
		Gdx.gl.glEnable(GL20.GL_BLEND);
		for(Particle particle : particles){
			particle.draw(shader, angle);
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
}
