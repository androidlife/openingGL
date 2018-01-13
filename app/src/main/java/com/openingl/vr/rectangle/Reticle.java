package com.openingl.vr.rectangle;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.Matrix;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.FloatBuffer;

import timber.log.Timber;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 1/10/18.
 */

public class Reticle {

    private Context context;
    private float radius;

    public Reticle(Context context, float radius) {
        this.context = context;
        this.radius = radius;
    }

    int circleMatrixId, circleColorId, circlePositionId;
    int numOfSegment = 20;
    int circleProgramId;
    FloatBuffer circlePositionBuffer;

    float[] circleModel = new float[16];
    float[] circleView = new float[16];
    float[] circleMV = new float[16];
    float[] circleMPV = new float[16];
    float[] circlePositions;

    RectF circleRect = new RectF();

    public void enableCircleData() {
        circleProgramId = ProgramCreator.createProgram(context, R.raw.circle_controller);
        glUseProgram(circleProgramId);
        circleMatrixId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Matrix");
        circleColorId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Color");
        circlePositionId = GLHelper.getIdLocationForAttribute(circleProgramId, "a_Position");
        circlePositions = GLHelper.drawCircle(0f, 0f, radius, ControllerActivity.zPos, numOfSegment);
        circlePositionBuffer = GLHelper.allocateFloatBuffer(circlePositions, 0);
        circleRect.left = -0.04f;
        circleRect.top = 0.04f;
        circleRect.right = 0.04f;
        circleRect.bottom = -0.04f;
    }

//    public void printTranslation(float[] translationMatrix) {
//        float[] leftTop = {circleRect.left, circleRect.top, ControllerActivity.zPos, 1};
//        float[] rightBottom = {circleRect.right, circleRect.bottom, ControllerActivity.zPos, 1};
//        float[] leftTopTrans = new float[4];
//        float[] rightBottomTrans = new float[4];
//        Matrix.multiplyMV(leftTopTrans, 0, translationMatrix, 0, leftTop, 0);
//        Matrix.multiplyMV(rightBottomTrans, 0, translationMatrix, 0, rightBottom, 0);
//        Timber.d("Left=%.2f,Top=%.2f,Right=%.2f,Bottom=%.2f,zPos=%.2f", leftTopTrans[0], leftTopTrans[1],
//                rightBottomTrans[0], rightBottomTrans[1],
//                leftTop[2]);
//    }


    public void printTranslation(float[] translationMatrix) {
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, translationMatrix, 0, circlePositions, 0);
        GLHelper.printMatrixByRow(result, "RETICLE");
    }

    public void drawMoving(float[]... matrices) {
        float[] perspective = matrices[0];
        float[] eyeView = matrices[1];
        float[] controllerMatrix = matrices[2];
        Matrix.multiplyMM(circleView, 0, perspective, 0, eyeView, 0);
        Matrix.multiplyMM(circleMPV, 0, circleView, 0, controllerMatrix, 0);
        drawCircle();
        Timber.d("Moving ------------");
        printTranslation(controllerMatrix);
    }

    public void drawStatic(float[]... matrices) {
        Matrix.setIdentityM(circleModel, 0);

        float[] perspective = matrices[0];
        float[] eyeView = matrices[1];
        float[] camera = matrices[2];

        Matrix.multiplyMM(circleView, 0, eyeView, 0, camera, 0);
        Matrix.multiplyMM(circleMV, 0, circleView, 0, circleModel, 0);
        Matrix.multiplyMM(circleMPV, 0, perspective, 0, circleMV, 0);
        drawCircle();

        Timber.d("Static-------------");
        printTranslation(circleMPV);
    }

    private void drawCircle() {
        glUseProgram(circleProgramId);
        circlePositionBuffer.position(0);
        glUniformMatrix4fv(circleMatrixId, 1, false, circleMPV, 0);

        glVertexAttribPointer(circlePositionId, 3, GL_FLOAT, false, 0, circlePositionBuffer);
        glEnableVertexAttribArray(circlePositionId);
        glUniform4f(circleColorId, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, numOfSegment);
    }
}
