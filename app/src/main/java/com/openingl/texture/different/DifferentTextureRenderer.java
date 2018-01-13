package com.openingl.texture.different;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;
import com.openingl.utils.ShaderCreator;
import com.openingl.utils.TextReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/12/17.
 */

public class DifferentTextureRenderer implements GLSurfaceView.Renderer {
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

    private FloatBuffer firstBuffer, secondBuffer, thirdBuffer, fourthBuffer;
    private FloatBuffer texturePosBuffer;

    int aTexturePosId, aPositionId, uMatrixId;
    int uTextureUnit;
    float[] aspectMatrix;
    float[] firstModel, secondModel, thirdModel, fourthModel;

    int[] textureIds = new int[4];
    int[] bitmapResIds = {
            R.drawable.pirate, R.drawable.lena, R.drawable.reed, R.drawable.some_icon
    };

    private static final int POSITION_COUNT = 2;
    private static final int TEXTURE_COORDINATE_COUNT = 2;
    private static final int ROW_COUNT = POSITION_COUNT + TEXTURE_COORDINATE_COUNT;
    private static final int BYTES_PER_FLOAT = 4;

    public DifferentTextureRenderer(Context context) {
        this.context = context;

        firstBuffer = allocateBuffer(firstVertex);
        secondBuffer = allocateBuffer(secondVertex);
        thirdBuffer = allocateBuffer(thirdVertex);
        fourthBuffer = allocateBuffer(fourthVertex);
        texturePosBuffer = allocateBuffer(texturePositions);

        aspectMatrix = new float[16];
        firstModel = new float[16];
        secondModel = new float[16];
        thirdModel = new float[16];
        fourthModel = new float[16];

    }


    private FloatBuffer allocateBuffer(float[] vertices) {
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(vertices);
        floatBuffer.position(0);
        return floatBuffer;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.different_texture);
        int shaderVertexId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int shaderFragmentId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        int programId = ProgramCreator.createProgram(shaderVertexId, shaderFragmentId);
        glUseProgram(programId);
        aTexturePosId = GLHelper.getIdLocationForAttribute(programId, "a_texturePosition");
        aPositionId = GLHelper.getIdLocationForAttribute(programId, "a_position");
        uMatrixId = GLHelper.getIdLocationForUniform(programId, "u_matrix");
        uTextureUnit = GLHelper.getIdLocationForUniform(programId, "u_textureUnit");
        attachBitmap();
    }

    private void attachBitmap() {
        for (int i = 0; i < bitmapResIds.length; ++i) {
            int resId = bitmapResIds[i];
            textureIds[i] = GLHelper.generateTextureId(GLHelper.getBitmapFrom(context, resId),true);
        }
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLHelper.performOrthographicProjection(width, height, aspectMatrix);
        addAspect(aspectMatrix, firstModel);
        addAspect(aspectMatrix, secondModel);
        addAspect(aspectMatrix, thirdModel);
        addAspect(aspectMatrix, fourthModel);


    }

    private void addAspect(float[] aspectMatrix, float[] model) {
        float[] transform = new float[16];
        Matrix.setIdentityM(transform, 0);
        Matrix.translateM(transform, 0, -0.5f, -0.5f, 0f);
        Matrix.scaleM(transform, 0, 0.01f, 0.01f, 0);
        Matrix.multiplyMM(model, 0, aspectMatrix, 0, transform, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);

        glVertexAttribPointer(aPositionId, POSITION_COUNT, GL_FLOAT, false, 0, firstBuffer);
        glEnableVertexAttribArray(aPositionId);
        enableTexture(textureIds[0],texturePosBuffer);
        glUniformMatrix4fv(uMatrixId, 1, false, firstModel, 0);
        glDrawArrays(GL_TRIANGLE_FAN, 0, firstVertex.length / 2);

        glVertexAttribPointer(aPositionId, POSITION_COUNT, GL_FLOAT, false, 0, secondBuffer);
        glEnableVertexAttribArray(aPositionId);
        enableTexture(textureIds[1],texturePosBuffer);
        glUniformMatrix4fv(uMatrixId, 1, false, secondModel, 0);
        glDrawArrays(GL_TRIANGLE_FAN, 0, secondVertex.length / 2);

        glVertexAttribPointer(aPositionId, POSITION_COUNT, GL_FLOAT, false, 0, thirdBuffer);
        glEnableVertexAttribArray(aPositionId);
        enableTexture(textureIds[2],texturePosBuffer);
        glUniformMatrix4fv(uMatrixId, 1, false, thirdModel, 0);
        glDrawArrays(GL_TRIANGLE_FAN, 0, thirdVertex.length / 2);

        glVertexAttribPointer(aPositionId, POSITION_COUNT, GL_FLOAT, false, 0, fourthBuffer);
        glEnableVertexAttribArray(aPositionId);
        enableTexture(textureIds[3],texturePosBuffer);
        glUniformMatrix4fv(uMatrixId, 1, false, fourthModel, 0);
        glDrawArrays(GL_TRIANGLE_FAN, 0, fourthVertex.length / 2);


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
