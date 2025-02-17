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
	private float time;
	private float fov = 90.0f;
	private int shape;
	private boolean[][] board;
	private int lastRotation;
	private int linesKilledAtOnce;
	private int totalLinesKilled;
	private int delta;
	
	private Texture tex;
	private Texture specTex;
	private Texture skyBoxTex;
	private Texture borgCube;
	private Texture particleTex;

	private boolean spaceOn;
	private float[][][] color;
	private float[] shapeColor;
	private float[][] position;
	
	boolean move = false;
	float targetX = 0.0f, targetY = 0.0f, targetFOV = 0.0f;

	@Override
	public void create () {
		
		shader = new Shader();
		
		DisplayMode disp = Gdx.graphics.getDesktopDisplayMode();
		Gdx.graphics.setDisplayMode(disp.width, disp.height, true);
		
		Gdx.input.setInputProcessor(this);
		
		angle = 0;
		delta = 10;
		BoxGraphic.create();
		SphereGraphic.create();
		SpriteGraphic.create();
		SincGraphic.create(shader.getVertexPointer());
		CoordFrameGraphic.create(shader.getVertexPointer());

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		ModelMatrix.main = new ModelMatrix();
		ModelMatrix.main.loadIdentityMatrix();
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_CONSTANT_ALPHA);


		cam = new Camera();
		cam.look(new Point3D(0.0f, -13f, 16f), new Point3D(0,-8,0), new Vector3D(0,1,0));
		
		tex = new Texture(Gdx.files.internal("textures/Frame.png"));
		specTex = new Texture(Gdx.files.internal("textures/metalSpecTex.png"));
		//specTex = null;
		skyBoxTex = new Texture("textures/starSkyBox.png");
		borgCube = new Texture("textures/borgCube.jpg");
		//particleTex = new Texture("textures/GreyGradiant01.png");
		
		dropTime = 0.5f;
		linesKilledAtOnce = 0;
		totalLinesKilled = 0;
		time = 0;
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			move = true;
			targetX = -15.0f;
			targetY = -10.0f;
			targetFOV = 70.0f;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			move = true;
			targetX = 15.0f;
			targetY = -10.0f;
			targetFOV = 70.0f;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			move = true;
			targetX = 0.0f;
			targetY = 13.0f;
			targetFOV = 70.0f;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
			move = true;
			targetX = 0.0f;
			targetY = -13.0f;
			targetFOV = 90.0f;
		}
		if(move){
			if(fov < targetFOV){
				fov += 90.0f * deltaTime;
				if(fov > targetFOV){
					fov = targetFOV;
				}
			}else if(fov > targetFOV){
				fov -= 90.0f * deltaTime;
				if(fov < targetFOV){
					fov = targetFOV;
				}
			}
			if(cam.eye.x < targetX){
				cam.eye.x += 90.0f * deltaTime;
				if(cam.eye.x > targetX){
					cam.eye.x = targetX;
				}
			}else if(cam.eye.x > targetX){
				cam.eye.x -= 90.0f * deltaTime;
				if(cam.eye.x < targetX){
					cam.eye.x = targetX;
				}
			}
			if(cam.eye.y < targetY){
				cam.eye.y += 90.0f * deltaTime;
				if(cam.eye.y > targetY){
					cam.eye.y = targetY;
				}
			}else if(cam.eye.y > targetY){
				cam.eye.y -= 90.0f * deltaTime;
				if(cam.eye.y < targetY){
					cam.eye.y = targetY;
				}
			}
			if(fov == targetFOV && cam.eye.x == targetX && cam.eye.y == targetY){
				move = false;
			}
			cam.look(new Point3D(cam.eye.x, cam.eye.y, 16f), new Point3D(0,-8,0), new Vector3D(0,1,0));	
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.W) && shape != 0 || Gdx.input.isKeyJustPressed(Input.Keys.UP) && shape != 0 ) {
			ModelMatrix.main.addRotationZ(-90);
			lastRotation = 90;
			if(checkCollision() || checkLeftCollision() || checkRightCollision())
			{
				ModelMatrix.main.addRotationZ(lastRotation);
			}
			
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.S) && shape != 0 || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && shape != 0) {
			ModelMatrix.main.addRotationZ(90);
			lastRotation = -90;
			if(checkCollision() || checkLeftCollision() || checkRightCollision())
			{
				ModelMatrix.main.addRotationZ(lastRotation);
			}
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
		time += deltaTime;
		
	}
	
	private void display()
	{
		//do all actual drawing and rendering here
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.perspectiveProjection(fov , 2.0f, 0.1f, 250.0f);
		shader.setViewMatrix(cam.getViewMatrix());
		shader.setProjectionMatrix(cam.getProjectionMatrix());
		shader.setEyePosition(cam.eye.x, cam.eye.y, cam.eye.z, 1.0f);
		
		
		//set world light	
		shader.setLightColor(1.0f, 1.0f, 1.0f, 1.0f);
		shader.setLightPosition(0, 8f, 16f, 0);
		shader.setSpotDirection(0.0f, 0.0f, -1.0f, 0.0f);
		shader.setSpotExponent(10.0f);
		shader.setConstantAttenuation(2.0f);
		shader.setLinearAttenuation(0.8f);
		shader.setQuadraticAttenuation(0.2f);
		
		shader.setGlobalAmbience(0.3f, 0.3f, 0.3f, 1.0f);
		
		ModelMatrix.main.pushMatrix();
		ModelMatrix.main.loadIdentityMatrix();
		
		/* Skybox */
		shader.setMaterialDiffuse(1.0f, 1.0f, 1.0f, 1.0f);
		shader.setMaterialSpecular(1.0f, 1.0f, 1.0f, 1.0f);
		shader.setMaterialShine(10.0f);
		shader.setMaterialEmission(0.1f, 0.1f, 0.1f, 1.0f);
		ModelMatrix.main.addScale(150.0f, 150.0f, 150.0f);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		SphereGraphic.drawSolidSphere(shader, skyBoxTex, skyBoxTex);
		ModelMatrix.main.popMatrix();
		
		/* Game */
		drawGrid();
		fillupBoard();
		shapeOnScreen();
		
		for(int i = 0; i < 4; i++){
			if(position[i][0] > 4 || position[i][0] < -5) {
				ModelMatrix.main.addRotationZ(lastRotation);
				break;
			}
		}
		
		for(int i = 0; i < 4; i++) {
			if(position[i][1] <= -20) {
				spaceOn = false;
				if(time > dropTime) {
					stickShapeOnBoard();
					ModelMatrix.main.loadIdentityMatrix();
					getNewShape();
					time = 0;
					spaceOn = true;
					break;
				}
			}
		}
		for(int i = 0; i < 4; i++){
			if(time > dropTime && position[i][1] > -20) {
				if(!checkCollision()) {
					drop();
				} else {
					stickShapeOnBoard();
					ModelMatrix.main.loadIdentityMatrix();
					getNewShape();
				}
				time = 0;
				break;
			}
		}	
	}
	public void drawGrid(){
		ModelMatrix.main.pushMatrix();
		for(int i = 1; i < 11; i++) {
			for(int j = 0; j < 22; j++) {
					Gdx.gl.glEnable(GL20.GL_BLEND);
					shader.setMaterialDiffuse(0.3f, 0.3f, 0.3f, 1);
					ModelMatrix.main.loadIdentityMatrix();
					ModelMatrix.main.addTranslation((i-6), j-20, -0.7f);
					shader.setMaterialEmission(0,0,0,1);
					shader.setModelMatrix(ModelMatrix.main.getMatrix());
					SpriteGraphic.drawSprite(shader, tex, specTex);
					Gdx.gl.glDisable(GL20.GL_BLEND);
			}
		}
		ModelMatrix.main.popMatrix();
	}
	public void fillupBoard() {
		
		for(int i = 1; i < 11; i++) {
			for(int j = 0; j < 22; j++) {
				if(board[i][j]) {
					shader.setMaterialDiffuse(color[i][j][0], color[i][j][1], color[i][j][2], 1);
					ModelMatrix.main.pushMatrix();
					ModelMatrix.main.loadIdentityMatrix();
					ModelMatrix.main.addTranslation((i-6), j-22, 0);
					shader.setMaterialEmission(0,0,0,1);
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
		time = 0;
	}
	public boolean checkCollision() {
		for(int i = 0; i < 4; i++) {
			int x = (int)position[i][0];
			int y = (int)position[i][1];
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
		
		while(dropLine()){
		}
		totalLinesKilled += linesKilledAtOnce;
		if(totalLinesKilled > delta){
			dropTime /= 1.1;
			delta+=10;
		}
		linesKilledAtOnce = 0;
		if(gameOver()){
			/*Dunno hva� vi� �tlum a� gera ef �a� er game over. �tla bara a� restarta eins og er*/
			restart();
		}
	}
	public void restart(){
		for(int i = 0; i < 14; i++){
			for(int j = 0; j <25; j++) {
				board[i][j] = false;
			}
		}
		totalLinesKilled = 0;
		time = 0;
		getNewShape();
		delta = 10;
		dropTime = 0.5f;
	}
	public boolean gameOver(){
		for(int i = 0; i < 14; i++){
			if(board[i][22]){
				return true;
			}
		}
		return false;
	}
	public boolean dropLine() {
		boolean ret = false;
		for(int i = 0; i < 22; i++){
			boolean dropline = true;
			for(int j = 1; j < 11; j++){
				if(!board[j][i]){
					dropline = false;
					break;
				}
			}
			if(dropline) {
				linesKilledAtOnce++;
				ret = true;
				for(int j = 1; j < 11; j++){
					for(int k = i; k < 22; k++){
						board[j][k] = board[j][k+1];
						color[j][k][0] = color[j][k+1][0];
						color[j][k][1] = color[j][k+1][1];
						color[j][k][2] = color[j][k+1][2];
					}
				}
			}
		}
		return ret;
		
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
		shader.setMaterialEmission(0,0,0,1);
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
		shader.setMaterialEmission(0,0,0,1);
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
		shader.setMaterialEmission(0,0,0,1);
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
		shader.setMaterialEmission(0,0,0,1);
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
		shader.setMaterialEmission(0,0,0,1);
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
		shader.setMaterialEmission(0,0,0,1);
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
		shader.setMaterialEmission(0,0,0,1);
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