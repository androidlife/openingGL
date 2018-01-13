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
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 1/10/18.
 */

public class ReticleRect {

    private Context context;
    private float radius;

    public ReticleRect(Context context, float radius) {
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
    float zPos = ControllerActivity.zPos + 0.5f;
    float[] circlePositions = {
            -0.04f, 0.04f, zPos,
            -0.04f, -0.04f, zPos,
            0.04f, -0.04f, zPos,
            0.04f, 0.04f, zPos
    };
    float[] leftTop = {-0.04f, 0.04f, zPos, 1f};
    float[] rightBottom = {0.04f, -0.04f, zPos, 1f};

    RectF circleRect = new RectF();

    public void enableCircleData() {
        circleProgramId = ProgramCreator.createProgram(context, R.raw.circle_controller);
        glUseProgram(circleProgramId);
        circleMatrixId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Matrix");
        circleColorId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Color");
        circlePositionId = GLHelper.getIdLocationForAttribute(circleProgramId, "a_Position");
        //circlePositions = GLHelper.drawCircle(0f, 0f, radius, ControllerActivity.zPos, numOfSegment);
        circlePositionBuffer = GLHelper.allocateFloatBuffer(circlePositions, 0);
        circleRect.left = -0.04f;
        circleRect.top = 0.04f;
        circleRect.right = 0.04f;
        circleRect.bottom = -0.04f;
    }

//    public void printTranslation(String label, float[] translationMatrix) {
//        Timber.d("%s-----------------", label);
//        int length = circlePositions.length / 3;
//        int index = -1;
//        for (int i = 0; i < length; ++i) {
//            float[] result = new float[4];
//            float[] input = {circlePositions[++index], circlePositions[++index], circlePositions[++index], 1};
//            Matrix.multiplyMV(result, 0, translationMatrix, 0, input, 0);
//            Timber.d("|%.2f %.2f %.2f|", result[0], result[1], result[2]);
//        }
//    }


    public RectF rectF = new RectF();

    public void printTranslation(String label, float[] translationMatrix) {
        Timber.d("%s--------------", label);
        float[] resultLeftTop = new float[4];
        float[] resultRightBottom = new float[4];
        Matrix.multiplyMV(resultLeftTop, 0, translationMatrix, 0, leftTop, 0);
        Matrix.multiplyMV(resultRightBottom, 0, translationMatrix, 0, rightBottom, 0);
        Timber.d("Left =%.2f, Top =%.2f,Right = %.2f, Bottom =%.2f,",
                resultLeftTop[0], resultLeftTop[1], resultRightBottom[0], resultRightBottom[1]);
        rectF.left = resultLeftTop[0];
        rectF.top = resultLeftTop[1];
        rectF.right = resultRightBottom[0];
        rectF.bottom = resultRightBottom[1];
    }

    private void printMatrix(float[] result) {
        int size = circlePositions.length / 3;
        int index = -1;
        for (int i = 0; i < size; ++i) {
            Timber.d("| %.2f %.2f %.2f |", result[++index], result[++index], result[++index]);
        }
        Timber.d("------------------------");
    }

    public void drawMoving(float[]... matrices) {
        float[] perspective = matrices[0];
        float[] eyeView = matrices[1];
        float[] controllerMatrix = matrices[2];
        Matrix.multiplyMM(circleView, 0, perspective, 0, eyeView, 0);
        Matrix.multiplyMM(circleMPV, 0, circleView, 0, controllerMatrix, 0);
        drawCircle();
        printTranslation("MOVING", circleMPV);
    }

    public void drawStatic(float[]... matrices) {
        Matrix.setIdentityM(circleModel, 0);

        float[] perspective = matrices[0];
        float[] eyeView = matrices[1];
        float[] color = matrices[2];
        //float[] camera = matrices[2];

        Matrix.multiplyMM(circleView, 0, perspective, 0, eyeView, 0);
        //Matrix.multiplyMM(circleMV, 0, circleView, 0, circleModel, 0);
        Matrix.multiplyMM(circleMPV, 0, circleView, 0, circleModel, 0);
        //drawCircle();
        //printTranslation("STATIC", circleMPV);

        glUseProgram(circleProgramId);
        circlePositionBuffer.position(0);
        glUniformMatrix4fv(circleMatrixId, 1, false, circleMPV, 0);

        glVertexAttribPointer(circlePositionId, 3, GL_FLOAT, false, 0, circlePositionBuffer);
        glEnableVertexAttribArray(circlePositionId);
//        glUniform4f(circleColorId, 0.0f, 0.0f, 0.0f, 1.0f);
        glUniform4fv(circleColorId, 1, color, 0);
        glDrawArrays(GL_TRIANGLE_FAN, 0, circlePositions.length / 3);
    }

    private void drawCircle() {
        glUseProgram(circleProgramId);
        circlePositionBuffer.position(0);
        glUniformMatrix4fv(circleMatrixId, 1, false, circleMPV, 0);

        glVertexAttribPointer(circlePositionId, 3, GL_FLOAT, false, 0, circlePositionBuffer);
        glEnableVertexAttribArray(circlePositionId);
        glUniform4f(circleColorId, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, circlePositions.length / 3);
    }
}
