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
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 */

public class ControllerActivity extends GvrActivity implements GvrView.StereoRenderer {

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

//    float[] vertices = {
//            -0.011f, 0.011f, zPos, // top
//            -0.011f, -0.011f, zPos,// bottom left
//            0.011f, -0.011f, zPos,// bottom right
//
//            -0.011f, 0.011f, zPos,
//            0.011f, -0.011f, zPos,
//            0.011f, 0.011f, zPos
//    };

    RectF rectF = new RectF(0, 0, 1.84f, -1f);
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

    float[] rectLeftTop = {0, 0, zPos, 1};
    float[] rectRightBottom = {xPos, yPos, zPos, 1};
    float[] rectModel = new float[16];

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

        Matrix.setIdentityM(rectModel, 0);
        float[] centerPos = {xPos / 2, yPos / 2, zPos, 1};
        initTransformationMatrix();
        float[] translatedPos = new float[4];
        Matrix.multiplyMV(translatedPos, 0, transformationMatrix, 0, centerPos, 0);
        Timber.d("CenterPos =%.2f,%.2f,%.2f", translatedPos[0], translatedPos[1], translatedPos[2]);
        Matrix.translateM(rectModel, 0, translatedPos[0], translatedPos[1], translatedPos[2]);

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


    }

    float[] headView = new float[16];

    //Some stereo rendering things
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, 1f,
                0.0f, 0.0f, -1f,
                0.0f, 1.0f, 0.0f);
        //GLHelper.printMatrixByRow(camera, "Camera");
        headTransform.getHeadView(headView, 0);
    }


    float[] transformationMatrix;
    float[] mvp = new float[16];

    private void initTransformationMatrix() {
        if (transformationMatrix != null)
            return;
        transformationMatrix = new float[16];
        float[] someMatrix = new float[16];
        Matrix.setIdentityM(someMatrix, 0);
        if (xPos > yPos) {
            GLHelper.orthoM(someMatrix, 0, 0, xPos, yPos, 0, 0, 0.5f,
                    xPos / yPos, 1);
        } else {
            GLHelper.orthoM(someMatrix, 0, 0, xPos, yPos, 0, 0, 0.5f,
                    1, yPos / xPos);
        }
        float[] otherMatrix = new float[16];
        Matrix.setIdentityM(otherMatrix, 0);
        Matrix.translateM(otherMatrix, 0, -0.5f, 0f, 0f);
        Matrix.multiplyMM(transformationMatrix, 0, otherMatrix, 0, someMatrix, 0);

    }

    @Override
    public void onDrawEye(Eye eye) {
        glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Apply the eye transformation to the camera.
        float[] eyeView = eye.getEyeView();
        //GLHelper.printMatrixByRow(eyeView, "EyeView");
        float[] perspective = eye.getPerspective(0.0001f, 200f);
        initTransformationMatrix();


//        Matrix.setIdentityM(model, 0);
//        if (xPos > yPos) {
//            GLHelper.orthoM(model, 0, 0, xPos, yPos, 0, 0, 0.5f,
//                    xPos / yPos, 1);
//        } else {
//            GLHelper.orthoM(model, 0, 0, xPos, yPos, 0, 0, 0.5f,
//                    1, yPos / xPos);
//        }
        model = transformationMatrix;


        Matrix.multiplyMM(view, 0, perspective, 0, eyeView, 0);
        //Matrix.multiplyMM(modelView, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, view, 0, model, 0);

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
            movingReticle.drawMoving(perspective, eyeView, controllerMatrix);
            //testForController(controllerMatrix);
            boolean contains = printTranslation("Rectangle", mvp);
            contains = isLookingAtObject(controllerMatrix, rectModel);
            //contains = isLookingAtObject(headView,rectModel);
            float[] color = contains ? new float[]{0.0f, 0.0f, 1.0f, 1.0f} : new float[]{0.0f, 0.0f, 0.0f, 1.0f};
            staticReticle.drawStatic(perspective, eyeView, color);
        }

    }

    RectF rectRectF = new RectF();

    public boolean printTranslation(String label, float[] translationMatrix) {
        Timber.d("%s--------------", label);
        float[] resultLeftTop = new float[4];
        float[] resultRightBottom = new float[4];
        Matrix.multiplyMV(resultLeftTop, 0, translationMatrix, 0, rectLeftTop, 0);
        Matrix.multiplyMV(resultRightBottom, 0, translationMatrix, 0, rectRightBottom, 0);
        Timber.d("Left =%.2f, Top =%.2f,Right = %.2f, Bottom =%.2f,",
                resultLeftTop[0], resultLeftTop[1], resultRightBottom[0], resultRightBottom[1]);
        rectRectF.left = resultLeftTop[0];
        rectRectF.top = resultLeftTop[1];
        rectRectF.right = resultRightBottom[0];
        rectRectF.bottom = resultRightBottom[1];
        boolean contains = rectRectF.left <= movingReticle.rectF.left && rectRectF.right >= movingReticle.rectF.right
                && rectRectF.top >= movingReticle.rectF.top && rectRectF.bottom <= movingReticle.rectF.bottom;
        PointF pointF = translateClick(controller.orientation, rectRectF, resultRightBottom[3]);
        contains = pointF != null;
        contains = isLookingAtObject(controllerMatrix, model);
        //contains = isLookingAtObject(headView,model)
        return contains;

    }

    // The number of pixels in this quad affect how Android positions Views in it. VideoUiView in VR
    // will be 1024 x 128 px in size which is similar to its 2D size. For Views that only have VR
    // layouts, using a number that results in ~10-15 px / degree is good.
    public static final int PX_PER_UNIT = 1024;

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
            Timber.d("CONTAINS OUTSIDE");
            return null;
        }
        Timber.d("CONTAINS INSIDE");

        // Convert from the polar coordinates of the controller to the rectangular coordinates of the
        // View. Note the negative yaw & pitch used to generate Android-compliant x & y coordinates.
        float xPercent = (horizontalHalfAngle - angles[0]) / (2 * horizontalHalfAngle);
        float yPercent = (verticleHalfAngle - angles[1]) / (2 * verticleHalfAngle);
        float xPx = xPercent * WIDTH * PX_PER_UNIT;
        float yPx = yPercent * HEIGHT * PX_PER_UNIT;

        return new PointF(xPx, yPx);
    }

    private boolean isLookingAtObject(float[] headView, float[] modelCube) {
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);

        float pitch = (float) Math.atan2(tempPosition[1], -tempPosition[2]);
        float yaw = (float) Math.atan2(tempPosition[0], -tempPosition[2]);

        Timber.d("PITCH =%.2f , YAW = %.2f", Math.abs(pitch), Math.abs(yaw));

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }


    private void testForController(float[] rotationMatrix) {
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, rotationMatrix, 0, modelView, 0);
        Matrix.multiplyMV(tempPosition, 0, result, 0, POS_MATRIX_MULTIPLY_VEC, 0);
        Timber.d("Temp Position = %.2f,%.2f,%.2f,%.2f",
                tempPosition[0], tempPosition[1], tempPosition[2], tempPosition[3]);
    }

    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};
    private float[] tempPosition = new float[4];


    private static final float YAW_LIMIT = 0.52f;
    private static final float PITCH_LIMIT = 0.30f;

//    private static final float YAW_LIMIT = 0.15f;
//    private static final float PITCH_LIMIT = 0.15f;

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

    private ReticleRect staticReticle, movingReticle;

    private void enableCircleData() {
        staticReticle = new ReticleRect(this, 0.04f);
        staticReticle.enableCircleData();
        movingReticle = new ReticleRect(this, 0.04f);
        movingReticle.enableCircleData();
    }


    @Override
    public void onRendererShutdown() {
    }

}
