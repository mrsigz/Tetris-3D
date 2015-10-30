package com.ru.tgra.lab1.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ru.tgra.shapes.Tetris_3D_Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title = "Ingibergur and Sigurgrímur - Tetris 3D"; // or whatever you like
		config.width = 1028;  //experiment with
		config.height = 748;  //the window size
		config.x = 150;
		config.y = 50;

		new LwjglApplication(new Tetris_3D_Game(), config);
	}
}
