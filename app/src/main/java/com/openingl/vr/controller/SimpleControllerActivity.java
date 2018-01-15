package com.openingl.vr.controller;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;
import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 */

public class SimpleControllerActivity extends GvrActivity implements GvrView.StereoRenderer {

    @BindView(R.id.gvr_view)
    GvrView gvrView;

    float xPos = 1824f, yPos = 989f;
    public static final float zPos = -3f;
    float[] vertices = {
            0.0f, yPos, zPos, // top
            xPos, yPos, zPos,// bottom left
            xPos, 0f, zPos,// bottom right

            0f, yPos, zPos,
            xPos, 0f, zPos,
            0f, 0f, zPos
    };
    float[] colors = {
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,

            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f
    };
    FloatBuffer vertexBuffer, colorBuffer;
    int colorId, positionId, matrixId;
    int triangleProgramId;

    private ControllerManager controllerManager;
    private ControllerEventListener controllerEventListener;
    private Controller controller;

    private ReticleRect staticReticle, movingReticle;


    float[] modelTransformationMatrix;
    float[] model = new float[16];
    float[] view = new float[16];
    float[] mvp = new float[16];
    //
    float[] controllerMatrix = new float[16];
    float[] centerPos = {xPos / 2, yPos / 2, zPos, 1};
    float[] staticCenterPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplevr);
        ButterKnife.bind(this);
        vertexBuffer = GLHelper.allocateFloatBuffer(vertices, 0);
        colorBuffer = GLHelper.allocateFloatBuffer(colors, 0);
        initController();
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setEGLContextClientVersion(2);
        gvrView.setRenderer(this);
        setGvrView(gvrView);
    }

    private void initController() {
        controllerEventListener = new ControllerEventListener();
        controllerManager = new ControllerManager(this, controllerEventListener);
        controller = controllerManager.getController();
        controller.setEventListener(controllerEventListener);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {
        glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        float[] eyeView = eye.getEyeView();
        float[] perspective = eye.getPerspective(0.1f, 200f);
        model = modelTransformationMatrix;
        //draw rectangle
        Matrix.multiplyMM(view, 0, perspective, 0, eyeView, 0);
        Matrix.multiplyMM(mvp, 0, view, 0, model, 0);

        glUseProgram(triangleProgramId);
        glUniformMatrix4fv(matrixId, 1, false, mvp, 0);
        vertexBuffer.position(0);
        glVertexAttribPointer(positionId, 3, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(positionId);
        colorBuffer.position(0);
        glVertexAttribPointer(colorId, 4, GL_FLOAT, false, 0, colorBuffer);
        glEnableVertexAttribArray(colorId);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glDisableVertexAttribArray(positionId);
        glDisableVertexAttribArray(colorId);

        //draw reticles
        if (controller != null) {
            controller.update();
            controller.orientation.toRotationMatrix(controllerMatrix);
            movingReticle.drawMoving(perspective, eyeView, controllerMatrix);
            boolean contains = isLookingAtObject(controllerMatrix, modelTransformationMatrix);
            float[] color = contains ? new float[]{0.0f, 0.0f, 1.0f, 1.0f} :
                    new float[]{0.0f, 0.0f, 0.0f, 1.0f};
            staticReticle.drawStatic(perspective, eyeView, color);
        }

    }


    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        enableTriangleData();
        initTransformationMatrix();
        enableCircleData();
    }


    private void initTransformationMatrix() {
        if (modelTransformationMatrix != null)
            return;
        float[] orthoMatrix = new float[16];
        float[] translateMatrix = new float[16];

        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, -1f, 0f, 0f);

        Matrix.setIdentityM(orthoMatrix, 0);

        modelTransformationMatrix = new float[16];
        if (xPos > yPos) {
            GLHelper.orthoM(orthoMatrix, 0, 0, xPos, yPos, 0, 0, 0.5f,
                    xPos / yPos, 1);
        } else {
            GLHelper.orthoM(orthoMatrix, 0, 0, xPos, yPos, 0, 0, 0.5f,
                    1, yPos / xPos);
        }

        Matrix.multiplyMM(modelTransformationMatrix, 0, translateMatrix, 0, orthoMatrix, 0);
    }

    private void enableTriangleData() {
        glClearColor(1f, 1f, 1f, 1f);
        triangleProgramId = ProgramCreator.createProgram(this, R.raw.rectangle_vr);
        glUseProgram(triangleProgramId);
        matrixId = GLHelper.getIdLocationForUniform(triangleProgramId, "u_matrix");
        colorId = GLHelper.getIdLocationForAttribute(triangleProgramId, "a_color");
        positionId = GLHelper.getIdLocationForAttribute(triangleProgramId, "a_position");
    }

    private void enableCircleData() {

        staticCenterPos = new float[4];
        Matrix.multiplyMV(staticCenterPos, 0, modelTransformationMatrix, 0, centerPos, 0);
        Timber.d("Center Pos = %.2f,%.2f,%.2f", staticCenterPos[0], staticCenterPos[1],
                staticCenterPos[2]);

        float zPoss = zPos + 0.05f;
        staticReticle = new ReticleRect(this, staticCenterPos[0], staticCenterPos[1], 0.04f,
                zPoss);
        staticReticle.setUp();
        movingReticle = new ReticleRect(this, zPoss);
        movingReticle.setUp();
    }


    private float[] tempPosition = new float[4];


    private static final float YAW_LIMIT = 0.52f;
    private static final float PITCH_LIMIT = 0.30f;

    private float[] modelView = new float[16];

//    private static final float YAW_LIMIT = 0.15f;
//    private static final float PITCH_LIMIT = 0.15f;

    private boolean isLookingAtObject(float[] headView, float[] modelCube) {
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, centerPos, 0);

        float pitch = (float) Math.atan2(tempPosition[1], -tempPosition[2]);
        float yaw = (float) Math.atan2(tempPosition[0], -tempPosition[2]);
        Timber.d("PITCH =%.2f , YAW = %.2f", Math.abs(pitch), Math.abs(yaw));
        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        controllerManager.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        controllerManager.stop();
    }


    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }
}
