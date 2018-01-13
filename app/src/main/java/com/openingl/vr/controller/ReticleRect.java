package com.openingl.vr.controller;

import android.content.Context;
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
 */

public class ReticleRect {

    int circleMatrixId, circleColorId, circlePositionId;
    int circleProgramId;
    private Context context;
    FloatBuffer circlePositionBuffer;
    float[] circlePositions;
    float[] circleModel = new float[16];
    float[] circleView = new float[16];
    float[] circleMPV = new float[16];

    public ReticleRect(Context context, float centerX, float centerY, float radius, float zPos) {
        this.context = context;
        float leftEnd = centerX - radius;
        float rightEnd = centerX + radius;
        float topEnd = centerY + radius;
        float bottomEnd = centerY - radius;
        Timber.d("Reticle pos = %.2f,%.2f,%.2f,%.2f",leftEnd,topEnd,rightEnd,bottomEnd);
        Timber.d("Circle center pos = %.2f,%.2f,",centerX,centerY);
        circlePositions = new float[]{
                leftEnd, topEnd, zPos,
                leftEnd, bottomEnd, zPos,
                rightEnd, bottomEnd, zPos,
                rightEnd, topEnd, zPos
        };
    }

    public ReticleRect(Context context, float zPos) {
        this.context = context;
        circlePositions = new float[]{-0.04f, 0.04f, zPos,
                -0.04f, -0.04f, zPos,
                0.04f, -0.04f, zPos,
                0.04f, 0.04f, zPos};
    }


    public void setUp() {
        circleProgramId = ProgramCreator.createProgram(context, R.raw.circle_controller);
        glUseProgram(circleProgramId);
        circleMatrixId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Matrix");
        circleColorId = GLHelper.getIdLocationForUniform(circleProgramId, "u_Color");
        circlePositionId = GLHelper.getIdLocationForAttribute(circleProgramId, "a_Position");
        circlePositionBuffer = GLHelper.allocateFloatBuffer(circlePositions, 0);
    }

    public void drawStatic(float[]... matrices) {
        Matrix.setIdentityM(circleModel, 0);

        float[] perspective = matrices[0];
        float[] eyeView = matrices[1];
        float[] color = matrices[2];

        Matrix.multiplyMM(circleView, 0, perspective, 0, eyeView, 0);
        Matrix.multiplyMM(circleMPV, 0, circleView, 0, circleModel, 0);

        glUseProgram(circleProgramId);
        circlePositionBuffer.position(0);
        glUniformMatrix4fv(circleMatrixId, 1, false, circleMPV, 0);

        glVertexAttribPointer(circlePositionId, 3, GL_FLOAT, false, 0, circlePositionBuffer);
        glEnableVertexAttribArray(circlePositionId);
        glUniform4fv(circleColorId, 1, color, 0);
        glDrawArrays(GL_TRIANGLE_FAN, 0, circlePositions.length / 3);
    }

    public void drawMoving(float[]... matrices) {
        float[] perspective = matrices[0];
        float[] eyeView = matrices[1];
        float[] controllerMatrix = matrices[2];
        Matrix.multiplyMM(circleView, 0, perspective, 0, eyeView, 0);
        Matrix.multiplyMM(circleMPV, 0, circleView, 0, controllerMatrix, 0);
        drawCircle();
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
