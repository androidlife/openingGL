package com.openingl.vr.rectangle;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;
import com.google.vr.sdk.controller.Orientation;
import com.openinggl.R;
import com.openingl.utils.GLError;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 */

public class VrRectangleMoveActivity extends GvrActivity implements GvrView.StereoRenderer {

    @BindView(R.id.gvr_view)
    GvrView gvrView;

    float xPos = 1824f, yPos = 989f, zPos = -1f;
    float[] vertices = {
            0.0f, yPos, zPos, // top
            xPos, yPos, zPos,// bottom left
            xPos, 0f, zPos,// bottom right

            0f, yPos, zPos,
            xPos, 0f, zPos,
            0f, 0f, zPos
    };

    RectF rectF = new RectF(0, 0, xPos, yPos);
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

    float[] camera, view, model, modelView;
    private static final float CAMERA_Z = 0.01f;

    FloatBuffer circlePositionBuffer;
    int circleProgramId;

    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplevr);
        ButterKnife.bind(this);
        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(vertices);
        colorBuffer = GLHelper.allocateFloatBuffer(colors, 0);

        camera = new float[16];
        view = new float[16];
        model = new float[16];
        modelView = new float[16];

        AndroidCompat.setVrModeEnabled(this, true);
        initController();

        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setEGLContextClientVersion(2);
        gvrView.setRenderer(this);
        setGvrView(gvrView);
        transformationTest1();
    }

    private void transformationTest() {
        float[] transformation = new float[16];
        if (xPos > yPos) {
            GLHelper.orthoM1(transformation, 0, 0, xPos, yPos, 0,
                    xPos / yPos, 1);
        } else {
            GLHelper.orthoM1(transformation, 0, 0, xPos, yPos, 0,
                    1, yPos / xPos);
        }

        int length = vertices.length / 3;
        int index = -1;
        for (int i = 0; i < length; ++i) {
            float[] vertex = {vertices[++index], vertices[++index], vertices[++index], 1};
            float[] result = new float[4];
            Matrix.multiplyMV(result, 0, transformation, 0, vertex, 0);
            Timber.d("result[0] =%.2f, result[1]=%.2f,result[2]=%.2f,result[3]=%.2f",
                    result[0], result[1], result[2], result[3]);
        }
    }


    private RectF rectFTrans;

    private void transformationTest1() {
        float[] startPos = {rectF.left, rectF.top, zPos, 1};
        float[] startPosTrans = new float[4];
        float[] endPos = {rectF.right, rectF.bottom, zPos, 1};
        float[] endPosTrans = new float[4];

        float[] transformation = new float[16];
        if (xPos > yPos) {
            GLHelper.orthoM1(transformation, 0, 0, xPos, yPos, 0,
                    xPos / yPos, 1);
        } else {
            GLHelper.orthoM1(transformation, 0, 0, xPos, yPos, 0,
                    1, yPos / xPos);
        }

        Matrix.multiplyMV(startPosTrans, 0, transformation, 0, startPos, 0);
        Matrix.multiplyMV(endPosTrans, 0, transformation, 0, endPos, 0);

        rectFTrans = new RectF();
        rectFTrans.left = startPosTrans[0];
        rectFTrans.top = startPosTrans[1];
        rectFTrans.right = endPosTrans[0];
        rectFTrans.bottom = endPosTrans[1];

        Timber.d("Left=%.2f,top=%.2f,right=%.2f,bottom=%.2f",
                rectFTrans.left, rectFTrans.top, rectFTrans.right, rectFTrans.bottom);
    }

    private ControllerManager controllerManager;
    private ControllerEventListener controllerEventListener;
    private Controller controller;

    private void initController() {
        uiHandler = new Handler();
        controllerEventListener = new ControllerEventListener();
        controllerManager = new ControllerManager(this, controllerEventListener);
        controller = controllerManager.getController();
        controller.setEventListener(controllerEventListener);
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

    private class ControllerEventListener extends Controller.EventListener implements
            ControllerManager.EventListener {

        //ControllerManager.EventListener
        @Override
        public void onApiStatusChanged(int i) {
            String apiStatus = ControllerManager.ApiStatus.toString(i);
            Timber.d("onApiStatusChanged = %d, apiStatus = %s", i, apiStatus);
            //uiHandler.post(this);
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            PointF pointF = translateClick(controller.orientation, rectFTrans, zPos);
//            if (pointF != null)
//                Timber.d("Contains in rectF = %b", rectFTrans.contains(pointF.x, pointF.y));
//            else
//                Timber.d("Contains pointF null");
        }

        @Override
        public void onRecentered() {
            Timber.d("onRecentered()");
            //uiHandler.post(this);
        }

        //Controller.EventListener

        @Override
        public void onConnectionStateChanged(int state) {
            Timber.d("OnConnection state changed %d, connectionStateChanged = %s",
                    state, Controller.ConnectionStates.toString(state));
            //uiHandler.post(this);
        }

//        @Override
//        public void run() {
//            controller.update();
//        }
    }


    //Some stereo rendering things
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, 1f,
                0.0f, 0.0f, -1.0f,
                0.0f, 1.0f, 0.0f);
        //GLHelper.printMatrixByRow(camera, "Camera");
    }

    float[] mvp = new float[16];

    @Override
    public void onDrawEye(Eye eye) {
        glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Apply the eye transformation to the camera.
        float[] eyeView = eye.getEyeView();
        float[] perspective = eye.getPerspective(0.01f, 200f);

        Matrix.setIdentityM(model, 0);
//        Matrix.orthoM(model, 0, 0, xPos, yPos, 0, 0, 0.5f);
//        float aspect = 1.0f;
//        if (xPos > yPos) {
//            Matrix.scaleM(model, 0, xPos / yPos, 1f, 1f);
//        } else {
//            Matrix.scaleM(model, 0, 1f, yPos / xPos, 1f);
//        }
//
//        Matrix.translateM(model, 0, 0, 0, -5f);
        if (xPos > yPos) {
            GLHelper.orthoM(model, 0, 0, xPos, yPos, 0, 0, 0.5f,
                    xPos / yPos, 1);
        } else {
            GLHelper.orthoM(model, 0, 0, xPos, yPos, 0, 0, 0.5f,
                    1, yPos / xPos);
        }

        //Matrix.translateM(model, 0, 0, 0, -0.5f);
        Matrix.multiplyMM(view, 0, eyeView, 0, camera, 0);
        Matrix.multiplyMM(modelView, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, perspective, 0, modelView, 0);
        //the following steps are important on Google Vr
        glUseProgram(triangleProgramId);
        glUniformMatrix4fv(matrixId, 1, false, mvp, 0);

        vertexBuffer.position(0);
        glVertexAttribPointer(positionId, 3, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(positionId);

        colorBuffer.position(0);
        glVertexAttribPointer(colorId, 4, GL_FLOAT, false, 0, colorBuffer);
        glEnableVertexAttribArray(colorId);
        //this is same in open gl
        glDrawArrays(GL_TRIANGLES, 0, 6);
        GLError.checkError("Draw Triangles");

        //draw circles
        if (controller != null) {
            controller.update();
            controller.orientation.toRotationMatrix(controllerMatrix);
            //GLHelper.printMatrixByRow(controllerMatrix, "ControllerMatrix");
            //Timber.d("Controller Matrix size = %d", controllerMatrix.length);
        }

        glUseProgram(circleProgramId);
        circlePositionBuffer.position(0);

        Matrix.multiplyMM(circleView, 0, perspective, 0, eyeView, 0);
        Matrix.multiplyMM(circleMPV, 0, circleView, 0, controllerMatrix, 0);
        calculateCirclePos(circleMPV);
        glUniformMatrix4fv(circleMatrixId, 1, false, circleMPV, 0);

        glVertexAttribPointer(circlePositionId, 3, GL_FLOAT, false, 0, circlePositionBuffer);
        glEnableVertexAttribArray(circlePositionId);
        glUniform4f(circleColorId, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, numOfSegment);

    }

    private void calculateCirclePos(float[] transformation) {
        RectF rectF = circleRect;
        float[] startPos = {rectF.left, rectF.top, zPos, 1};
        float[] startPosTrans = new float[4];
        float[] endPos = {rectF.right, rectF.bottom, zPos, 1};
        float[] endPosTrans = new float[4];

        Matrix.multiplyMV(startPosTrans, 0, transformation, 0, startPos, 0);
        Matrix.multiplyMV(endPosTrans, 0, transformation, 0, endPos, 0);

        RectF rectFTrans = new RectF();
        rectFTrans.left = startPosTrans[0];
        rectFTrans.top = startPosTrans[1];
        rectFTrans.right = endPosTrans[0];
        rectFTrans.bottom = endPosTrans[1];

        Timber.d("CIRCLE POS Left=%.2f,top=%.2f,right=%.2f,bottom=%.2f",
                rectFTrans.left, rectFTrans.top, rectFTrans.right, rectFTrans.bottom);
        Timber.d("Contains CIRCLE POS = %b", this.rectFTrans.contains(rectFTrans));
    }

    private boolean isLookingAtObject(float[] transformation) {
        float[] initVec = {0, 0, 0, 1.0f};
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        float[] modelView = new float[16];
        Matrix.multiplyMM(modelView, 0, transformation, 0, vertices, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }

    private static final float YAW_LIMIT = 0.15f;
    private static final float PITCH_LIMIT = 0.15f;

    float[] controllerMatrix = new float[16];
    float[] tmpMatrix = new float[16];


    private void enableTriangleData() {
        glClearColor(1f, 1f, 1f, 1f);
        triangleProgramId = ProgramCreator.createProgram(this, R.raw.rectangle_vr);
        glUseProgram(triangleProgramId);
        matrixId = GLHelper.getIdLocationForUniform(triangleProgramId, "u_matrix");
        colorId = GLHelper.getIdLocationForAttribute(triangleProgramId, "a_color");
        positionId = GLHelper.getIdLocationForAttribute(triangleProgramId, "a_position");
    }

    int circleMatrixId, circleColorId, circlePositionId;
    int numOfSegment = 20;
    float[] circleModel = new float[16];
    float[] circleView = new float[16];
    float[] circleMV = new float[16];
    float[] circleMPV = new float[16];
    RectF circleRect = new RectF();

    private void enableCircleData() {
        circleProgramId = ProgramCreator.createProgram(this, R.raw.circle_controller);
        glUseProgram(circleProgramId);
        circleMatrixId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Matrix");
        circleColorId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Color");
        circlePositionId = GLHelper.getIdLocationForAttribute(circleProgramId, "a_Position");
        float[] circlePositions = GLHelper.drawCircle(0f, 0f, 0.04f, zPos, numOfSegment);
        circlePositionBuffer = GLHelper.allocateFloatBuffer(circlePositions, 0);
        circleRect.left = -0.04f;
        circleRect.top = 0.04f;
        circleRect.right = 0.04f;
        circleRect.bottom = -0.04f;
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
        enableCircleData();
    }


    @Override
    public void onRendererShutdown() {
    }

    public static final int PX_PER_UNIT = 1;

    static PointF translateClick(Orientation orientation, RectF rectF, float DISTANCE) {
        float[] angles = orientation.toYawPitchRollRadians(new float[3]);
        // Make a rough guess of the bounds of the Quad in polar coordinates. This works as long as the
        // Quad isn't too large.
        float WIDTH = Math.abs(rectF.width());
        float HEIGHT = Math.abs(rectF.height());
        float horizontalHalfAngle = (float) Math.atan2(WIDTH / 2, DISTANCE);
        float verticleHalfAngle = (float) Math.atan2(HEIGHT / 2, DISTANCE);

        if (angles[1] < -verticleHalfAngle || angles[1] > verticleHalfAngle
                || angles[0] < -horizontalHalfAngle || angles[0] > horizontalHalfAngle) {
            // Click is outside of the quad.
            Timber.d("outside CLICK");
            return null;
        }
        Timber.d("inside CLICK");

        // Convert from the polar coordinates of the controller to the rectangular coordinates of the
        // View. Note the negative yaw & pitch used to generate Android-compliant x & y coordinates.
        float xPercent = (horizontalHalfAngle - angles[0]) / (2 * horizontalHalfAngle);
        float yPercent = (verticleHalfAngle - angles[1]) / (2 * verticleHalfAngle);
        float xPx = xPercent * WIDTH * PX_PER_UNIT;
        float yPx = yPercent * HEIGHT * PX_PER_UNIT;
        Timber.d("xPx =%.2f, yPx = %.2f", xPx, yPx);

        return new PointF(xPx, yPx);
    }
}
