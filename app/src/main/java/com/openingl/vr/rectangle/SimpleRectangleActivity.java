package com.openingl.vr.rectangle;

import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.openinggl.R;
import com.openingl.utils.GLError;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 1/11/18.
 */

public class SimpleRectangleActivity extends GvrActivity implements GvrView.StereoRenderer {
    @BindView(R.id.gvr_view)
    GvrView gvrView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplevr);
        ButterKnife.bind(this);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setEGLContextClientVersion(2);
        gvrView.setRenderer(this);
        setGvrView(gvrView);
    }


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
    int programId;
    FloatBuffer verticesBuffer, colorBuffer;
    int positionId, colorId, matrixId;

    float[] mvp = new float[16];
    float[] model = new float[16];
    float[] camera = new float[16];
    float[] view = new float[16];

    float[] colors = {
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,

            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f
    };


    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        glClearColor(1f, 1f, 1f, 1f);
        programId = ProgramCreator.createProgram(this, R.raw.rectangle_vr);
        glUseProgram(programId);
        matrixId = GLHelper.getIdLocationForUniform(programId, "u_matrix");
        colorId = GLHelper.getIdLocationForAttribute(programId, "a_color");
        positionId = GLHelper.getIdLocationForAttribute(programId, "a_position");

        verticesBuffer = GLHelper.allocateFloatBuffer(vertices, 0);
        colorBuffer = GLHelper.allocateFloatBuffer(colors, 0);
        setModel();
        modelTransformation("Model", model);
        //setCamera();
        //modelTransformation("Camera*Model", view);
    }

    private void setModel() {
        Matrix.setIdentityM(model, 0);
        if (xPos > yPos) {
            GLHelper.orthoM1(model, 0, 0, xPos, yPos, 0, xPos / yPos, 1);
        } else {
            GLHelper.orthoM1(model, 0, 0, xPos, yPos, 0, 1, yPos / xPos);
        }
    }

    private void setCamera() {
        Matrix.setLookAtM(camera, 0, 0, 0f, 0f,
                0f, 0f, -1f,
                0f, 1f, 0f);
        Matrix.multiplyMM(view, 0, camera, 0, model, 0);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(programId);

        verticesBuffer.position(0);
        colorBuffer.position(0);

        glVertexAttribPointer(colorId, 4, GL_FLOAT, false, 0, colorBuffer);
        glEnableVertexAttribArray(colorId);
        glVertexAttribPointer(positionId, 3, GL_FLOAT, false, 0, verticesBuffer);
        glEnableVertexAttribArray(positionId);


        float[] perspective = eye.getPerspective(0.1f, 100f);
        Matrix.multiplyMM(view, 0, perspective, 0, eye.getEyeView(), 0);
        modelTransformation("perspective*eyeVew ", view);
        Matrix.multiplyMM(mvp, 0, view, 0, model, 0);
        modelTransformation("view*model", mvp);
        glUniformMatrix4fv(matrixId, 1, false, mvp, 0);

        glDrawArrays(GL_TRIANGLES, 0, vertices.length / 3);

        GLError.checkError("Draw Triangle");

        glDisableVertexAttribArray(colorId);
        glDisableVertexAttribArray(positionId);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }


    @Override
    public void onRendererShutdown() {

    }

    private void modelTransformation(String label, float[] model) {
        Timber.d("%s-----------", label);
        int length = vertices.length / 3;
        int index = -1;
        for (int i = 0; i < length; ++i) {
            float[] original = {vertices[++index], vertices[++index], vertices[++index], 1};
            float[] transformed = new float[4];
            Matrix.multiplyMV(transformed, 0, model, 0, original, 0);
            Timber.d("| %.2f %.2f %.2f %.2f|", transformed[0], transformed[1], transformed[2], transformed[3]);
        }
    }
}
