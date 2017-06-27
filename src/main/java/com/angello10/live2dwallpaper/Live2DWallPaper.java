package com.angello10.live2dwallpaper;

import net.rbgrn.android.glwallpaperservice.*;

import android.content.*;

import android.service.wallpaper.WallpaperService.*;
import android.service.wallpaper.WallpaperService.Engine.*;


public class Live2DWallPaper extends GLWallpaperService {
	public Live2DWallPaper() {
		super();
	}
	
	@Override
	public Engine onCreateEngine() {
		Live2DEngine engine = new Live2DEngine();
		return engine;
	}

	class Live2DEngine extends GLEngine {
		Live2DRenderer renderer;
		public Live2DEngine() {
			super();
            renderer = new Live2DRenderer(getApplicationContext());
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
		}
		
		public void onDestroy() {
            super.onDestroy();
            if (renderer != null) {
                renderer.release();
            }
            renderer = null;
        }
    }
}
