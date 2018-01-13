package com.openingl.texture.net;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.openinggl.R;
import com.openingl.utils.GLError;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/12/17.
 */

public class NetTextureRenderer implements GLSurfaceView.Renderer {
    private Context context;

    private float[] firstVertex = {
            0f, 0f,
            40f, 0f,
            40f, 40f,
            0f, 40f
    };

    private float[] secondVertex = {
            60f, 0f,
            100f, 0f,
            100f, 40f,
            60f, 40f
    };

    private float[] thirdVertex = {
            60f, 60f,
            100f, 60f,
            100f, 100f,
            60f, 100f
    };
    private float[] fourthVertex = {
            0f, 60f,
            40f, 60f,
            40f, 100f,
            0f, 100f
    };

    private float[] texturePositions = {
            0f, 1f,
            1f, 1f,
            1f, 0f,
            0f, 0f
    };

    private FloatBuffer texturePosBuffer;

    int aTexturePosId, aPositionId, uMatrixId;
    int uTextureUnit;
    float[] aspectMatrix;

    String[] imageUrls = {
            "https://www.planwallpaper.com/static/images/880665-road-wallpapers.jpg",
            "https://wallpapercave.com/wp/Ou1L18s.jpg",
            "http://hddesktopwallpapers.in/wp-content/uploads/2015/06/backgrounds-beach.jpg",
            "https://static.pexels.com/photos/36764/marguerite-daisy-beautiful-beauty.jpg"
    };


    private RequestManager glide;

    private List<GridItem> gridItems;
    private int totalGrids = 4;
    private int loadingTextureId;


    public NetTextureRenderer(Context context) {
        this.context = context;
        glide = Glide.with(context);


        gridItems = new ArrayList<>(totalGrids);
        for (int i = 0; i < totalGrids; ++i) {
            GridItem gridItem = new GridItem(glide, getVerticesByPosition(i), imageUrls[i], i);
            gridItems.add(gridItem);
        }
        texturePosBuffer = GLHelper.allocateFloatBuffer(texturePositions, 0);
        aspectMatrix = new float[16];

    }

    private float[] getVerticesByPosition(int index) {
        switch (index) {
            case 0:
                return firstVertex;
            case 1:
                return secondVertex;
            case 2:
                return thirdVertex;
            default:
                return fourthVertex;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);

        int programId = ProgramCreator.createProgram(context, R.raw.different_texture);
        glUseProgram(programId);
        aTexturePosId = GLHelper.getIdLocationForAttribute(programId, "a_texturePosition");
        aPositionId = GLHelper.getIdLocationForAttribute(programId, "a_position");
        uMatrixId = GLHelper.getIdLocationForUniform(programId, "u_matrix");
        uTextureUnit = GLHelper.getIdLocationForUniform(programId, "u_textureUnit");

        loadingTextureId = GLHelper.generateTextureId(GLHelper.getBitmapFrom(context, R.drawable.loading),true);

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLHelper.performOrthographicProjection(width, height, aspectMatrix);
        for (GridItem gridItem : gridItems)
            gridItem.addAspect(aspectMatrix);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        for (GridItem gridItem : gridItems) {
            gridItem.positionVertices(aPositionId);
            if (!gridItem.canDraw()) {
                enableTexture(loadingTextureId, texturePosBuffer);
                gridItem.loadBitmap();
            } else
                enableTexture(gridItem.getTextureId(), texturePosBuffer);
            glUniformMatrix4fv(uMatrixId, 1, false, gridItem.getModel(), 0);
            gridItem.draw();
        }
        GLError.checkError("Drawing something");
    }

    private void enableTexture(int textureId, FloatBuffer floatBuffer) {
        floatBuffer.position(0);
        glVertexAttribPointer(aTexturePosId, 2, GL_FLOAT, false, 0, floatBuffer);
        glEnableVertexAttribArray(aTexturePosId);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnit, 0);
    }
}
