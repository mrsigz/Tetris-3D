package com.ru.tgra.shapes;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;

public class Tetris_3D_Game extends ApplicationAdapter implements InputProcessor {
	
	Shader shader;
	
	private float angle;
	private Camera cam;
	private float dropTime;
	private float test;
	private float fov = 90.0f;
	
	@Override
	public void create () {
		
		DisplayMode disp = Gdx.graphics.getDesktopDisplayMode();
		Gdx.graphics.setDisplayMode(disp.width, disp.height, true);
		
		shader = new Shader();
		
		Gdx.input.setInputProcessor(this);
		
		angle = 0;

		BoxGraphic.create(shader.getVertexPointer(), shader.getNormalPointer());
		SphereGraphic.create(shader.getVertexPointer(), shader.getNormalPointer());
		SincGraphic.create(shader.getVertexPointer());
		CoordFrameGraphic.create(shader.getVertexPointer());

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		ModelMatrix.main = new ModelMatrix();
		ModelMatrix.main.loadIdentityMatrix();
		//ModelMatrix.main.setShaderMatrix(modelMatrixLoc);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		cam = new Camera();
		cam.look(new Point3D(-3f, 0f, 3f), new Point3D(0,3,0), new Vector3D(0,1,0));
		dropTime = 2;
		test = 0;
		
		
	}

	private void input(float deltaTime)
	{
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			cam.rotate(90.0f * deltaTime);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			cam.rotate(-90.0f * deltaTime);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			cam.pitch(90.0f * deltaTime);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			cam.pitch(-90.0f * deltaTime);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.A) && ModelMatrix.main.getOrigin().x > -5) {
			ModelMatrix.main.addTranslationBaseCoords(-1, 0, 0);
			//cam.slide(-3.0f * deltaTime, 0, 0);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.D) && ModelMatrix.main.getOrigin().x < 5) {
			ModelMatrix.main.addTranslationBaseCoords(1, 0, 0);

			//cam.slide(3.0f * deltaTime, 0, 0);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.W)) {
			ModelMatrix.main.addRotationZ(-90);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			ModelMatrix.main.addRotationZ(90);	
		}
		if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.graphics.setDisplayMode(500,500,false);
			Gdx.app.exit();
		}
	}
	
	private void update()
	{
		float deltaTime = Gdx.graphics.getDeltaTime();
		input(deltaTime);	
		angle += 180.0f * deltaTime;
		test += deltaTime;
		
	}
	
	private void display()
	{
		//do all actual drawing and rendering here
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.perspectiveProjection(fov, 2.0f, 0.1f, 100.0f);
		shader.setViewMatrix(cam.getViewMatrix());
		shader.setProjectionMatrix(cam.getProjectionMatrix());
		shader.setEyePosition(cam.eye.x, cam.eye.y, cam.eye.z, 1.0f);
		
		
		
		//set world light
		//float s = (float)Math.sin(angle * Math.PI / 180.0);
		//float c = (float)Math.cos(angle * Math.PI / 180.0);
		shader.setLightColor(1, 1, 1, 1);
		shader.setLightPosition(1, 1, 1, 1.0f);
		shader.setGlobalAmbience(1, 1, 1, 1);
		
		/*ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(1, 1, 1, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		ModelMatrix.main.addTranslation(1,1, 1);
		//ModelMatrix.main.addScale(1, 1, 1);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		//BoxGraphic.drawSolidCube();
		//SphereGraphic.drawSolidSphere();
		ModelMatrix.main.popMatrix();*/
		if(test > dropTime) {
			drop();
			test = 0;
		}
		shapeT();
		System.out.println(ModelMatrix.main.getOrigin());
		
	}
	public void drop() {
		ModelMatrix.main.addTranslationBaseCoords(0,-1,0);
	}
	
	
	public void shapeO(){
		ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(1, 1, 0, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
	}
	public void shapeI() {
		ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(0.2f, 0.2f, 1, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
	}
	public void shapeS() {
		ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(1, 0.5f, 0.0f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
	}
	public void shapeZ() {
		ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(0, 1.0f, 0.0f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,-1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
	}
	public void shapeL() {
		ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(1, 0.0f, 0.0f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
	}
	public void shapeJ() {
		ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(1, 0.8f, 0.9f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
	}
	public void shapeT() {
		ModelMatrix.main.pushMatrix();
		shader.setMaterialDiffuse(0.5f, 0.0f, 0.5f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
	}
	@Override
	public void render () {
		//put the code inside the update and display methods, depending on the nature of the code
		update();
		display();
	}


	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}


}