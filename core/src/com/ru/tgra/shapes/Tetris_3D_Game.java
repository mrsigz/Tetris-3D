package com.ru.tgra.shapes;


import java.util.Random;

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
	private int shape;
	private int rotation = 0;
	private boolean[][] board;
	private float[][][] color;
	private float[] shapeColor;
	private float[][] position;
	@Override
	public void create () {
		
		DisplayMode disp = Gdx.graphics.getDesktopDisplayMode();
		//Gdx.graphics.setDisplayMode(disp.width, disp.height, true);
		
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
		dropTime = 1;
		test = 0;
		getNewShape();
		//shape = 2;
		board = new boolean[10][25];
		position = new float[4][2];
		color = new float[10][25 ][3];
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
			rotation= (rotation - 90) % 360;
			//System.out.println(rotation);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.S) && shape != 0) {
			ModelMatrix.main.addRotationZ(90);
			rotation = (rotation + 90) % 360;
			//System.out.println(rotation);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.graphics.setDisplayMode(500,500,false);
			Gdx.app.exit();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
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
		shapeOnScreen();
		//shapeO();
		if(test > dropTime && ModelMatrix.main.getOrigin().y > -20) {
			if(!checkCollision()) {
				drop();
			} else {
				stickShapeOnBoard();
				ModelMatrix.main.loadIdentityMatrix();
				getNewShape();
			}
			test = 0;
		}
		for(int i = 0; i < 4; i++) {
			if(position[i][1] <= -20) {
				stickShapeOnBoard();
				ModelMatrix.main.loadIdentityMatrix();
				getNewShape();
			}
		}
		
		
		
	}
	public void fillupBoard() {
		
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 22; j++) {
				if(board[i][j]) {
					shader.setMaterialDiffuse(color[i][j][0], color[i][j][1], color[i][j][2], 1);
					ModelMatrix.main.pushMatrix();
					ModelMatrix.main.loadIdentityMatrix();
					ModelMatrix.main.addTranslation((i-5), j-22, 0);
					shader.setModelMatrix(ModelMatrix.main.getMatrix());
					BoxGraphic.drawSolidCube();
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
		for(int i = 0; i < 4; i++){
			//System.out.println("i:" + i + ", x: " + (int)position[i][0] + ", y:" + (int)position[i][1]);
		}
	}
	public boolean checkCollision() {
		for(int i = 0; i < 4; i++) {
			int x = (int)position[i][0];
			int y = (int)position[i][1];
			//System.out.println("x: " + (x+5) + " y:" + (y+22));
			if(board[x+5][y+21]){
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
			if(board[x+4][y+21]){
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
			if(board[x+6][y+21]){
				return true;
			}
		}
		return false;
	}
	public void stickShapeOnBoard() {
		for(int i = 0; i < 4; i++){
			int x = (int)position[i][0];
			int y = (int)position[i][1];
			board[x+5][y+22] = true;
			color[x+5][y+22][0] = shapeColor[0];
			color[x+5][y+22][1] = shapeColor[1];
			color[x+5][y+22][2] = shapeColor[2];
		}
	}
	public void getNewShape() {
		Random rand = new Random();
		shape = rand.nextInt(7);
		rotation = 0;
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
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
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
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,-2,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
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
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1,-1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
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
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,-1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
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
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,-2,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
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
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0, 1, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(0,-2,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
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
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[1][0] = ModelMatrix.main.getOrigin().x;
		position[1][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(2,0,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[2][0] = ModelMatrix.main.getOrigin().x;
		position[2][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.addTranslation(-1,-1,0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		position[3][0] = ModelMatrix.main.getOrigin().x;
		position[3][1] = ModelMatrix.main.getOrigin().y;
		BoxGraphic.drawSolidCube();
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