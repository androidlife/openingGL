package com.openingl.triangle.fanagain;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * Created by laaptu on 10/24/17.
 */

public class TriangleFanRenderer implements GLSurfaceView.Renderer {

    private static float XPOS = 800, YPOS = 800;
    private static final float[] COORDS = new float[]{
            0, YPOS, 0,
            XPOS, YPOS, 0,
            XPOS, 0, 0,

            0, YPOS, 0,
            XPOS, 0, 0,
            0, 0, 0

    };


    static float red = (float) 91 / (float) 255;
    static float green = (float) 84 / (float) 255;
    static float blue = (float) 152 / (float) 255;

    static float red1 = (float) 30 / (float) 255;
    static float green1 = (float) 27 / (float) 255;
    static float blue1 = (float) 65 / (float) 255;

    private static final float[] COLORS = new float[]{
            red1, green1, blue1, 1.0f,
            red1, green1, blue1, 1.0f,
            red1, green1, blue1, 1.0f,

            red, green, blue, 1.0f,
            red, green, blue, 1.0f,
            red, green, blue, 1.0f
    };


    FloatBuffer floatBuffer, colorBuffer;
    Context context;

    private int programId;

    private static final int BYTES_PER_FLOAT = 4;
    //R G B
    private static final int COLOR_COMPONENT_COUNT = 3;
    // X Y
    private static final int POSITION_COUNT = 2;

    private static final int STRIDE = (POSITION_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final String COORDINATE = "coordinate";
    private static final String COLOR_VALUE = "colorValue";

    private int uMatrixId;

    private int coordinate, colorValue;

    public TriangleFanRenderer(Context context) {
        this.context = context;
        floatBuffer = GLHelper.allocateFloatBuffer(COORDS, 0);
        colorBuffer = GLHelper.allocateFloatBuffer(COLORS, 0);
    }

    private float[] model = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);

        programId = ProgramCreator.createProgram(context, R.raw.trianglefan_again);
        glUseProgram(programId);
        coordinate = GLHelper.getIdLocationForAttribute(programId, "a_Position");
        colorValue = GLHelper.getIdLocationForAttribute(programId, "a_Color");
        uMatrixId = GLHelper.getIdLocationForUniform(programId, "u_Matrix");

//        Matrix.setIdentityM(model, 0);
//        Matrix.orthoM(model, 0, 0, 800, 800, 0, 1, -1);
//        glUniformMatrix4fv(uMatrixId, 1, false, model, 0);
//
//        //for coordinate value
//        floatBuffer.position(0);
//        glVertexAttribPointer(coordinate, 3, GL_FLOAT, false, 0, floatBuffer);
//        glEnableVertexAttribArray(coordinate);
//        //for color values
//        colorBuffer.position(0);
//        glVertexAttribPointer(colorValue, 4, GL_FLOAT, false, 0, colorBuffer);
//        glEnableVertexAttribArray(colorValue);


    }

    float[] otherModel = new float[16];
    float[] mainModel = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Matrix.setIdentityM(model, 0);
        float aspect = (float) width / (float) height;
        float left = 0, right = XPOS, top = 0, bottom = YPOS;

//
//        Matrix.orthoM(mainModel, 0, left, right, bottom, top, -1f, 1f);
//
//        if (aspect < 1.0) {
//            bottom /= aspect;
//            top = 0;
//        } else {
//            left = 0;
//            right *= aspect;
//        }
//        Matrix.orthoM(otherModel, 0, left, right, bottom, top, -1f, 1f);
//        Matrix.multiplyMM(model, 0, otherModel, 0, mainModel, 0);

        float mAspectRatioX = (float) Math.min(width, height) / width;
        float mAspectRatioY = (float) Math.min(width, height) / height;
        Matrix.orthoM(model, 0, left, right, bottom, top, -1f, 1f);
        Matrix.perspectiveM(mainModel,0,45,mAspectRatioY,-1f,1f);
        //Matrix.multiplyMM(model, 0, otherModel, 0, mainModel, 0);
        glViewport(0,0,(int)XPOS,(int)YPOS);


    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //glDisable(GL_DEPTH_TEST);


        //glUniformMatrix4fv(uMatrixId, 1, false, model, 0);

        //for coordinate value
        floatBuffer.position(0);
        glVertexAttribPointer(coordinate, 3, GL_FLOAT, false, 0, floatBuffer);
        glEnableVertexAttribArray(coordinate);
        //for color values
        colorBuffer.position(0);
        glVertexAttribPointer(colorValue, 4, GL_FLOAT, false, 0, colorBuffer);
        glEnableVertexAttribArray(colorValue);

        glClear(GL_COLOR_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
}
