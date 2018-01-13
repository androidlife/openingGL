package com.openingl.texture.net;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.openingl.utils.GLHelper;

import java.nio.FloatBuffer;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 */

public class GridItem {

    private float[] vertex;
    private FloatBuffer vertexBuffer;
    private int textureId = -1;
    private String url;
    private Bitmap bitmap;
    private FutureTarget<Bitmap> futureTarget;
    private int index;

    private float[] model;
    private boolean isLoading = false;
    private RequestManager glide;

    private static int EMPTY_BITMAP = 0x1, ERROR_BITMAP = 0x2, LOADING_BITMAP = 0x3,
            TEXTURED_BITMAP = 0x4, LOADED_BITMAP = 0x5;
    private int state = EMPTY_BITMAP;

    public GridItem(RequestManager glide, float[] vertex, String url, int index) {
        this.vertex = vertex;
        this.url = url;
        this.index = index;
        vertexBuffer = GLHelper.allocateFloatBuffer(this.vertex, 0);
        this.glide = glide;
        model = new float[16];
    }

    public int getTextureId() {
        return textureId;
    }

    public boolean canDraw() {
        return textureId != -1;
    }

    public void addAspect(float[] aspectMatrix) {
        float[] transform = new float[16];
        Matrix.setIdentityM(transform, 0);
        Matrix.translateM(transform, 0, -0.5f, -0.5f, 0f);
        Matrix.scaleM(transform, 0, 0.01f, 0.01f, 0);
        Matrix.multiplyMM(model, 0, aspectMatrix, 0, transform, 0);
    }

    public void positionVertices(int aPositionId) {
        glVertexAttribPointer(aPositionId, 2, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(aPositionId);
    }

    public void loadBitmap() {
        if (isLoading)
            return;
        if (!canDraw() && state == LOADED_BITMAP) {
            glide.clear(futureTarget);
            textureId = GLHelper.generateTextureId(bitmap, false);
            bitmap = null;
            state = TEXTURED_BITMAP;
            return;
        }
        isLoading = true;
        futureTarget = glide.asBitmap().load(url).submit(256, 256);
        Observable.fromCallable(() -> getBitmap()).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(b -> {
                    if (b != null) {
                        bitmap = b;
                        state = LOADED_BITMAP;
                        isLoading = false;
                    }
                }, e -> Timber.e("Error loading bitmap"));
    }

    private Bitmap getBitmap() {
        try {
            return futureTarget.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public float[] getModel() {
        return model;
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, vertex.length / 2);
    }
}
