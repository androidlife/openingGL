package com.openingl.camera.perspective;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/3/17.
 */

public class PerspectiveRenderer implements GLSurfaceView.Renderer {

    private Context context;
    //1byte = 8 bit
    //1float = 32 bit = 4 * 8 bit = 4 byte
    //clockwise backface
    float[] trianglePoints = {
            //front face
            // X  Y Z R G B
            -0.5f, -0.5f, 0.5f, 1f, 0f, 0f,
            0.5f, -0.5f, 0.5f, 1f, 0f, 0f,
            0.5f, 0.5f, 0.5f, 1f, 0f, 0f,

            -0.5f, -0.5f, 0.5f, 1f, 0f, 0f,
            0.5f, 0.5f, 0.5f, 1f, 0f, 0f,
            -0.5f, 0.5f, 0.5f, 1f, 0f, 0f,
            //side face 1
            0.5f, -0.5f, 0.5f, 0f, 1f, 0f,
            0.5f, -0.5f, -0.5f, 0f, 1f, 0f,
            0.5f, 0.5f, -0.5f, 0f, 1f, 0f,

            0.5f, -0.5f, 0.5f, 0f, 1f, 0f,
            0.5f, 0.5f, -0.5f, 0f, 1f, 0f,
            0.5f, 0.5f, 0.5f, 0f, 1f, 0f,
            //back face
            -0.5f, -0.5f, -0.5f, 0f, 0f, 1f,
            0.5f, -0.5f, -0.5f, 0f, 0f, 1f,
            0.5f, 0.5f, -0.5f, 0f, 0f, 1f,

            -0.5f, -0.5f, -0.5f, 0f, 0f, 1f,
            0.5f, 0.5f, -0.5f, 0f, 0f, 1f,
            -0.5f, 0.5f, -0.5f, 0f, 0f, 1f,
            //side face 2
            -0.5f, -0.5f, 0.5f, 1f, 1f, 0f,
            -0.5f, -0.5f, -0.5f, 1f, 1f, 0f,
            -0.5f, 0.5f, -0.5f, 1f, 1f, 0f,

            -0.5f, -0.5f, 0.5f, 1f, 1f, 0f,
            -0.5f, 0.5f, -0.5f, 1f, 1f, 0f,
            -0.5f, 0.5f, 0.5f, 1f, 1f, 0f,
            //top face
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f,
            0.5f, 0.5f, 0.5f, 0f, 1f, 1f,
            0.5f, 0.5f, -0.5f, 0f, 1f, 1f,

            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f,
            0.5f, 0.5f, -0.5f, 0f, 1f, 1f,
            -0.5f, 0.5f, -0.5f, 0f, 1f, 1f,
            //bottom face
            -0.5f, -0.5f, 0.5f, 1f, 0f, 1f,
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f,
            0.5f, -0.5f, -0.5f, 1f, 0f, 1f,

            -0.5f, -0.5f, 0.5f, 1f, 0f, 1f,
            0.5f, -0.5f, -0.5f, 1f, 0f, 1f,
            -0.5f, -0.5f, -0.5f, 1f, 0f, 1f
    };
    FloatBuffer floatBuffer;

    private int colorVarId, pointsVarId, matrixId;


    private static final int POSITION_COUNT = 3, COLOR_COUNT = 3;
    private static final int STRIDE = POSITION_COUNT + COLOR_COUNT;


    // Viewing variables
    private float[] cubeModel;
    private float[] camera;
    private float[] cubeModelView;
    private float[] perspectiveMatrix;
    private float[] modelViewProjection;

    public PerspectiveRenderer(Context context) {
        this.context = context;
        floatBuffer = ByteBuffer.allocateDirect(trianglePoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(trianglePoints);

        //
        cubeModel = new float[16];
        camera = new float[16];
        cubeModelView = new float[16];
        perspectiveMatrix = new float[16];
        modelViewProjection = new float[16];
        Matrix.setIdentityM(cubeModel, 0);
        Matrix.translateM(cubeModel, 0, 0, 0, -10f);
        //Matrix.rotateM(cubeModel, 0, -45, 0.5f, 0.5f, 0.0f);

        Matrix.setLookAtM(camera, 0,
                0f, 0f, 0f,
                0f, 0f, -1f,
                0f, 1.0f, 0.0f);
        GLHelper.printMatrixByRow(camera, "Camera -Z axis");

        Matrix.multiplyMM(cubeModelView, 0, camera, 0, cubeModel, 0);
    }


    /**
     * Steps
     * 1. Get the shader codes
     * 2. Create shader with those codes
     * 3. create program and link the shader with the program
     * 4. Use the program
     * 5. Get variables id from program id
     * 6. Set appropriate values to it
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        int programId = ProgramCreator.createProgram(context, R.raw.cube_camera);
        glUseProgram(programId);
        colorVarId = GLHelper.getIdLocationForAttribute(programId, "a_color");
        matrixId = GLHelper.getIdLocationForUniform(programId, "u_matrix");
        pointsVarId = GLHelper.getIdLocationForAttribute(programId, "a_position");

        floatBuffer.position(0);
        glVertexAttribPointer(pointsVarId, POSITION_COUNT, GL_FLOAT, false, STRIDE * 4,
                floatBuffer);
        glEnableVertexAttribArray(pointsVarId);
        floatBuffer.position(POSITION_COUNT);
        glVertexAttribPointer(colorVarId, COLOR_COUNT, GL_FLOAT, false, STRIDE * 4,
                floatBuffer);
        glEnableVertexAttribArray(colorVarId);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Matrix.perspectiveM(perspectiveMatrix, 0, 45,
                (float) width / (float) height, 0.5f, 100.0f);
        Matrix.multiplyMM(modelViewProjection, 0, perspectiveMatrix, 0,
                cubeModelView, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUniformMatrix4fv(matrixId, 1, false, modelViewProjection, 0);
        glDrawArrays(GL_TRIANGLES, 0, trianglePoints.length / STRIDE);
    }
}
