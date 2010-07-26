package org.cocos2d.grid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLU;

import org.cocos2d.opengl.CCCamera;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCTexture2D;
import org.cocos2d.types.ccGridSize;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.utils.CCFormatter;

import javax.microedition.khronos.opengles.GL10;

public abstract class GridBase {

    protected boolean active;
    protected int reuseGrid;
    protected ccGridSize gridSize;
    protected CCTexture2D texture;
    protected CGPoint step;

    public static final int kTextureSize = 512;

    public boolean reuseGrid() {
        return reuseGrid > 0;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean flag) {
        active = flag;
    }

    public int getGridWidth() {
        return gridSize.x;
    }

    public int getGridHeight() {
        return gridSize.y;
    }

    public GridBase(ccGridSize gSize) {
        active = false;
        reuseGrid = 0;
        gridSize = gSize;

        CGSize win = CCDirector.sharedDirector().winSize();

        if (texture == null) {
            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = Bitmap.createBitmap(kTextureSize, kTextureSize, config);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, 0, 0, new Paint());

            texture = new CCTexture2D(bitmap, win);
        }

        step = CGPoint.make(0, 0);
        step.x = win.width / gridSize.x;
        step.y = win.height / gridSize.y;
    }

    public String toString() {
        return new CCFormatter().format("<%s = %08X | Dimensions = %ix%i>", GridBase.class, this, gridSize.x, gridSize.y);
    }

    private static final boolean LANDSCAPE_LEFT = false;

    // This routine can be merged with Director
    public void applyLandscape(GL10 gl) {
        boolean landscape = CCDirector.sharedDirector().getLandscape();

        if (landscape) {
            gl.glTranslatef(160, 240, 0);

            if (LANDSCAPE_LEFT) {
                gl.glRotatef(-90, 0, 0, 1);
                gl.glTranslatef(-240, -160, 0);
            } else {
                // rotate left
                gl.glRotatef(90, 0, 0, 1);
                gl.glTranslatef(-240, -160, 0);
            } // LANDSCAPE_LEFT
        }
    }

    public void set2DProjection(GL10 gl) {
        CGSize winSize = CCDirector.sharedDirector().winSize();

        gl.glLoadIdentity();
        gl.glViewport(0, 0, (int) winSize.width, (int) winSize.height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0, winSize.width, 0, winSize.height, -100, 100);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    // This routine can be merged with Director
    public void set3DProjection(GL10 gl) {
        CGSize winSize = CCDirector.sharedDirector().displaySize();

        gl.glViewport(0, 0, (int) winSize.width, (int) winSize.height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 60, winSize.width / winSize.height, 0.5f, 1500.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        GLU.gluLookAt(gl, winSize.width / 2, winSize.height / 2, CCCamera.getZEye(),
                winSize.width / 2, winSize.height / 2, 0,
                0.0f, 1.0f, 0.0f
        );
    }

    public void beforeDraw(GL10 gl) {
        set2DProjection(gl);
    }

    public void afterDraw(GL10 gl, CCCamera camera) {
        set3DProjection(gl);
        applyLandscape(gl);

        boolean cDirty = camera.getDirty();
        camera.setDirty(true);
        camera.locate(gl);
        camera.setDirty(cDirty);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.name());

        blit(gl);

        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    public abstract void blit(GL10 gl);

    public abstract void reuse();
}