package com.openingl.vr.cube;

import android.opengl.Matrix;
import android.os.Bundle;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 */

public class CubeActivity extends GvrActivity implements GvrView.StereoRenderer {

    @BindView(R.id.gvr_view)
    GvrView gvrView;


    float[] cubeVertices = {
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
    FloatBuffer vertexBuffer;
    int colorId, positionId, matrixId;
    int cubeProgramId;

    private static final int POSITION_COUNT = 3, COLOR_COUNT = 3;
    private static final int STRIDE = POSITION_COUNT + COLOR_COUNT;

    private float[] camera, view, modelCube, modelView, modelViewProjection;
    private float CAMERA_Z = 0.01f;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;
    private static final float MAX_MODEL_DISTANCE = 7.0f;
    private static final float TIME_DELTA = 0.3f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplevr);
        ButterKnife.bind(this);
        vertexBuffer = ByteBuffer.allocateDirect(cubeVertices.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(cubeVertices);

        camera = new float[16];
        view = new float[16];
        modelCube = new float[16];
        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, 0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f);
        GLHelper.printMatrixByRow(modelCube,"ModelCube");
        modelView = new float[16];
        modelViewProjection = new float[16];

        gvrView.setRenderer(this);
        setGvrView(gvrView);

    }


    //Some stereo rendering things
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);
        Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
    }

    @Override
    public void onDrawEye(Eye eye) {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawTriangle();
    }

    private void drawTriangle() {
        //the following steps are important on Google Vr
        glUseProgram(cubeProgramId);
        vertexBuffer.position(0);
        glVertexAttribPointer(positionId, 3, GL_FLOAT, false, STRIDE * 4, vertexBuffer);
        glEnableVertexAttribArray(positionId);
        vertexBuffer.position(POSITION_COUNT);
        glVertexAttribPointer(colorId, 3, GL_FLOAT, false, STRIDE * 4, vertexBuffer);
        glEnableVertexAttribArray(colorId);

        glUniformMatrix4fv(matrixId, 1, false, modelViewProjection, 0);
        glDrawArrays(GL_TRIANGLES, 0, cubeVertices.length / STRIDE);
    }

    private void enableTriangleData() {
        cubeProgramId = ProgramCreator.createProgram(this, R.raw.cube_vr);
        glUseProgram(cubeProgramId);
        positionId = GLHelper.getIdLocationForAttribute(cubeProgramId, "a_position");
        colorId = GLHelper.getIdLocationForAttribute(cubeProgramId, "a_color");
        matrixId = GLHelper.getIdLocationForUniform(cubeProgramId, "u_matrix");

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
    }


    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        enableTriangleData();
    }


    @Override
    public void onRendererShutdown() {
    }
}
