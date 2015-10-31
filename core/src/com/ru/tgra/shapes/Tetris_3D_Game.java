package com.ru.tgra.shapes;


import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class Tetris_3D_Game extends ApplicationAdapter implements InputProcessor {
	
	Shader shader;
	
	private float angle;
	private Camera cam;
	private float dropTime;
	private float test;
	private float fov = 90.0f;
	private int shape;
	private boolean[][] board;
	private int lastRotation;
	private Texture tex;
	private Texture specTex;
	private boolean spaceOn;
	private float[][][] color;
	private float[] shapeColor;
	private float[][] position;

	@Override
	public void create () {
		
		shader = new Shader();
		
		DisplayMode disp = Gdx.graphics.getDesktopDisplayMode();
		//Gdx.graphics.setDisplayMode(disp.width, disp.height, true);
		
		Gdx.input.setInputProcessor(this);
		
		angle = 0;

		BoxGraphic.create();
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
		cam.look(new Point3D(-13f, 0f, 20f), new Point3D(0,3,0), new Vector3D(0,1,0));
		
		tex = new Texture(Gdx.files.internal("textures/Frame.png"));
		specTex = new Texture(Gdx.files.internal("textures/Spec01.png"));
		
		dropTime = 2;

		test = 0;
		getNewShape();
		spaceOn = true;
		//shape = 2;
		board = new boolean[14][25];
		position = new float[4][2];
		color = new float[14][25][3];
		shapeColor = new float[3];
		for(int i = 0; i < 10; i++){
			for(int j = 0; j <22; j++) {
				color[i][j][0] = 1;
				color[i][j][1] = 1;
				color[i][j][2] = 1;
				//board[i][j] = true;
			}
		}
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			
			boolean no = false;
			for(int i = 0; i < 4; i++) {
				if(position[i][0] <= -5) {
					no = true;
					break;
				}
			}
			if(!no) {
				if(!checkLeftCollision()) {
					ModelMatrix.main.addTranslationBaseCoords(-1, 0, 0);
				}
			}
			//cam.slide(-3.0f * deltaTime, 0, 0);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.D) ) {
			boolean no = false;
			for(int i = 0; i < 4; i++) {
				if(position[i][0] >= 4) {
					no = true;
					break;
				}
			}
			if(!no) {
				if(!checkRightCollision()) {
					ModelMatrix.main.addTranslationBaseCoords(1, 0, 0);
				}
			}
			//cam.slide(3.0f * deltaTime, 0, 0);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.W) && shape != 0) {
			ModelMatrix.main.addRotationZ(-90);
			lastRotation = 90;
			if(checkCollision() || checkLeftCollision() || checkRightCollision())
			{
				ModelMatrix.main.addRotationZ(lastRotation);
			}
			
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.S) && shape != 0) {
			ModelMatrix.main.addRotationZ(90);
			lastRotation = -90;
			if(checkCollision() || checkLeftCollision() || checkRightCollision())
			{
				ModelMatrix.main.addRotationZ(lastRotation);
			}
			//System.out.println(rotation);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.graphics.setDisplayMode(500,500,false);
			Gdx.app.exit();
		}
		if(Gdx.input.isKeyPressed(Input.Keys.SPACE) && spaceOn) {
			if(!checkCollision()) {
				drop();
			}
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
		cam.perspectiveProjection(fov , 2.0f, 0.1f, 100.0f);
		shader.setViewMatrix(cam.getViewMatrix());
		shader.setProjectionMatrix(cam.getProjectionMatrix());
		shader.setEyePosition(cam.eye.x, cam.eye.y, cam.eye.z, 1.0f);
		fillupBoard();
		
		
		//set world light
		//float s = (float)Math.sin(angle * Math.PI / 180.0);
		//float c = (float)Math.cos(angle * Math.PI / 180.0);
		shader.setLightColor(1.0f, 1.0f, 1.0f, 1.0f);
		shader.setLightPosition(1.0f, 1.0f, 1.0f, 1.0f);
		shader.setSpotDirection(1.0f, 0.0f, 1.0f, 0.0f);
		/* Set light as a spot light shining from the eye to emphasize 3D */
		//shader.setLightPosition(cam.eye.x, cam.eye.y, cam.eye.z, 1.0f);
		//shader.setSpotDirection(-cam.getN().x, -cam.getN().y, -cam.getN().z, 0.0f);
		shader.setSpotExponent(10.0f);
		shader.setConstantAttenuation(1.0f);
		shader.setLinearAttenuation(8.0f);
		shader.setQuadraticAttenuation(1.0f);
		
		shader.setGlobalAmbience(0.3f, 0.3f, 0.3f, 1.0f);
		for(int i = 0; i < 4; i++){
			if(position[i][0] > 4 || position[i][0] < -5) {
				ModelMatrix.main.addRotationZ(lastRotation);
				break;
			}
		}
		shapeOnScreen();
		//shapeO();
		for(int i = 0; i < 4; i++) {
			if(position[i][1] <= -20) {
				spaceOn = false;
				if(test > dropTime) {
					stickShapeOnBoard();
					ModelMatrix.main.loadIdentityMatrix();
					getNewShape();
					test = 0;
					spaceOn = true;
					break;
				}
			}
		}
		for(int i = 0; i < 4; i++){
			if(test > dropTime && position[i][1] > -20) {
				if(!checkCollision()) {
					drop();
				} else {
					stickShapeOnBoard();
					ModelMatrix.main.loadIdentityMatrix();
					getNewShape();
				}
				test = 0;
				break;
			}
		}
		
		
		
		
	}
	public void fillupBoard() {
		
		for(int i = 1; i < 11; i++) {
			for(int j = 0; j < 22; j++) {
				if(board[i][j]) {
					shader.setMaterialDiffuse(color[i][j][0], color[i][j][1], color[i][j][2], 1);
					ModelMatrix.main.pushMatrix();
					ModelMatrix.main.loadIdentityMatrix();
					ModelMatrix.main.addTranslation((i-6), j-22, 0);
					shader.setModelMatrix(ModelMatrix.main.getMatrix());
					BoxGraphic.drawSolidCube(shader, tex, specTex);
					ModelMatrix.main.popMatrix();
				}
			}
		}
	}
	public void shapeOnScreen() {
		switch(shape) {
		case 0: 
			shapeO();
			break;
		case 1:
			shapeI();
			break;
		case 2:
			shapeS();
			break;
		case 3:
			shapeZ();
			break;
		case 4:
			shapeL();
			break;
		case 5:
			shapeJ();
			break;
		case 6:
			shapeT();
			break;
	}
	}
	public void drop() {
		ModelMatrix.main.addTranslationBaseCoords(0,-1,0);
		test = 0;
	}
	public boolean checkCollision() {
		for(int i = 0; i < 4; i++) {
			int x = (int)position[i][0];
			int y = (int)position[i][1];
			//System.out.println("x: " + (x+5) + " y:" + (y+22));
			if(board[x+6][y+21]){
				return true;
			}
		}
		return false;
	}
	public boolean checkLeftCollision() {
		for(int i = 0; i < 4; i++) {
			int x = (int)position[i][0];
			int y = (int)position[i][1];
			//System.out.println("x: " + (x+5) + " y:" + (y+22));
			if(board[x+5][y+22]){
				return true;
			}
		}
		return false;
	}
	public boolean checkRightCollision() {
		for(int i = 0; i < 4; i++) {
			int x = (int)position[i][0];
			int y = (int)position[i][1];
			//System.out.println("x: " + (x+5) + " y:" + (y+22));
			if(board[x+7][y+22]){
				return true;
			}
		}
		return false;
	}
	public void stickShapeOnBoard() {
		for(int i = 0; i < 4; i++){
			int x = (int)position[i][0];
			int y = (int)position[i][1];
			board[x+6][y+22] = true;
			color[x+6][y+22][0] = shapeColor[0];
			color[x+6][y+22][1] = shapeColor[1];
			color[x+6][y+22][2] = shapeColor[2];
		}
	}
	public void getNewShape() {
		Random rand = new Random();
		shape = rand.nextInt(7);
	}
	
	public void shapeO(){
		ModelMatrix.main.pushMatrix();
		shapeColor[0] =1;
		shapeColor[1] =1;
		shapeColor[2] =0;
		shader.setMaterialDiffuse(1, 1, 0, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());

		position[0][0] = ModelMatrix.main.getOrigin().x;
		position[0][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(-1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);

		ModelMatrix.main.popMatrix();
	}
	public void shapeI() {

		ModelMatrix.main.pushMatrix();
		shapeColor[0] =0.2f;
		shapeColor[1] =0.2f;
		shapeColor[2] =1;
		shader.setMaterialDiffuse(0.2f, 0.2f, 1, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());

		position[0][0] = ModelMatrix.main.getOrigin().x;
		position[0][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0,-3,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);

		ModelMatrix.main.popMatrix();
	}
	public void shapeS() {
		ModelMatrix.main.pushMatrix();
		shapeColor[0] =1;
		shapeColor[1] =0.5f;
		shapeColor[2] =0;
		shader.setMaterialDiffuse(1, 0.5f, 0.0f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());

		position[0][0] = ModelMatrix.main.getOrigin().x;
		position[0][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(-1,-1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(-1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);

		ModelMatrix.main.popMatrix();
	}
	public void shapeZ() {
		ModelMatrix.main.pushMatrix();
		shapeColor[0] =0;
		shapeColor[1] =1;
		shapeColor[2] =0;
		shader.setMaterialDiffuse(0, 1.0f, 0.0f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());

		position[0][0] = ModelMatrix.main.getOrigin().x;
		position[0][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(-1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(1,-1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);

		ModelMatrix.main.popMatrix();
	}
	public void shapeL() {
		ModelMatrix.main.pushMatrix();
		shapeColor[0] =1;
		shapeColor[1] =0;
		shapeColor[2] =0;
		shader.setMaterialDiffuse(1, 0.0f, 0.0f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());

		position[0][0] = ModelMatrix.main.getOrigin().x;
		position[0][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0,-2,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);

		ModelMatrix.main.popMatrix();
	}
	public void shapeJ() {
		ModelMatrix.main.pushMatrix();
		shapeColor[0] =1;
		shapeColor[1] =0.8f;
		shapeColor[2] =0.9f;
		shader.setMaterialDiffuse(1, 0.8f, 0.9f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());

		position[0][0] = ModelMatrix.main.getOrigin().x;
		position[0][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(0,-2,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(-1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);

		ModelMatrix.main.popMatrix();
	}
	public void shapeT() {
		ModelMatrix.main.pushMatrix();
		shapeColor[0] =0.5f;
		shapeColor[1] =0;
		shapeColor[2] =0.5f;
		shader.setMaterialDiffuse(0.5f, 0.0f, 0.5f, 1);
		shader.setMaterialSpecular(1, 1, 1, 1);
		shader.setMaterialShine(50);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());

		position[0][0] = ModelMatrix.main.getOrigin().x;
		position[0][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(-1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(2,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);
		ModelMatrix.main.addTranslation(-1,-1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube(shader, tex, specTex);

		ModelMatrix.main.popMatrix();
	}
	@Override
	public void render() {
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