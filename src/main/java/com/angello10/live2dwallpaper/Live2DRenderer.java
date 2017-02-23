package com.angello10.live2dwallpaper;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;
import android.content.Context;
import android.content.res.AssetManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.rbgrn.android.glwallpaperservice.*;
import android.widget.*;
import java.io.*;

public class Live2DRenderer implements GLWallpaperService.Renderer {
	Context ctx;

	Live2DModelAndroid live2DModel ;
	Live2DMotion motion;
	MotionQueueManager manager;

	String MODEL_PATH = "";
	String[] TEXTURE_PATHS = {} ;
	String MOTION_PATH = "";
	float[] colors = {1, 1, 1};


	public Live2DRenderer(Context ctx) {
		this.ctx = ctx;
		manager = new MotionQueueManager();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("sdcard/live2d.txt"));
			String read;
			while((read = br.readLine()) != null) {
				read = read.replaceAll(" ", "");
				String[] data;
				if(read.indexOf(":") == -1) continue;
				data = read.split(":");
				switch(data[0]) {
					case "texture_paths":
						TEXTURE_PATHS = data[1].indexOf(",") == -1 ? new String[]{data[1]} : data[1].split(",");
						break;
					case "model_path":
						MODEL_PATH = data[1];
						break;
					case "motion_path":
						MOTION_PATH = data[1];	
						break;
					case "background_color":
						String[] color = data[1].split(",");
						for(int i = 0;i < 3;i++) colors[i] = Float.parseFloat(color[i]);
					}
			}
		}catch(Exception error) {}
	}


	public void onDrawFrame(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glClearColor(colors[0], colors[1], colors[2], 1.0f);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);

		live2DModel.loadParam();

		if(manager.isFinished()) {
			manager.startMotion(motion, false);
		}
		else {
			manager.updateParam(live2DModel);
		}

		live2DModel.saveParam();

		live2DModel.setGL(gl);

		live2DModel.update();
		live2DModel.draw();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
    	gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		float modelWidth = live2DModel.getCanvasWidth();
		gl.glOrthof(0, modelWidth, modelWidth * height / width, 0, 0.5f, -0.5f);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	try {
			InputStream is = new FileInputStream(MODEL_PATH);
			live2DModel = Live2DModelAndroid.loadModel(is);
			is.close();
		} catch(Exception error) {}

		try {
			for (int i = 0;i < TEXTURE_PATHS.length;i++) {
				InputStream is = new FileInputStream(TEXTURE_PATHS[i]);
				live2DModel.setTexture(i, UtOpenGL.loadTexture(gl, is, true)) ;
				is.close();
			}
		} catch(Exception error) {}

		try {
			InputStream is = new FileInputStream(MOTION_PATH);
			motion = Live2DMotion.loadMotion(is) ;
			is.close();
		} catch(Exception error) {}
    }

    public void release() {
    }
}