package com.openingl.vr.triangle;

import android.opengl.GLES20;
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
import com.openingl.utils.ShaderCreator;
import com.openingl.utils.TextReader;

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
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 */

public class TriangleMoveActivity extends GvrActivity implements GvrView.StereoRenderer {

    @BindView(R.id.gvr_view)
    GvrView gvrView;

    float[] vertices = {
            0.0f, 0.0f, 0.0f, // top
            0f, 0.5f, 0.0f,// bottom left
            0.5f, 0f, 0.0f// bottom right
    };
    FloatBuffer vertexBuffer;
    int colorId, positionId, matrixId;
    int triangleProgramId;

    float[] camera, view;
    private static final float CAMERA_Z = 0.01f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplevr);
        ButterKnife.bind(this);
        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(vertices);

        camera = new float[16];
        view = new float[16];

        gvrView.setRenderer(this);
        setGvrView(gvrView);


    }


    //Some stereo rendering things
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);
        GLHelper.printMatrixByRow(camera, "Camera");
    }

    @Override
    public void onDrawEye(Eye eye) {
        glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Apply the eye transformation to the camera.
        float[] eyeView = eye.getEyeView();
        GLHelper.printMatrixByRow(eyeView, "EYE");
        Matrix.multiplyMM(view, 0, eyeView, 0, camera, 0);
        GLHelper.printMatrixByRow(view, "View");
        drawTriangle();
    }

    private void drawTriangle() {
        //the following steps are important on Google Vr
        glUseProgram(triangleProgramId);
        glUniformMatrix4fv(matrixId, 1, false, view, 0);
        glVertexAttribPointer(positionId, 3, GL_FLOAT, false, 0, vertexBuffer);
        //glEnableVertexAttribArray(positionId);
        glUniform4f(colorId, 1f, 0f, 0f, 1f);
        //this is same in open gl
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void enableTriangleData() {
        glClearColor(1f, 1f, 1f, 1f);
        String[] shaderCodes = TextReader.readShaderFromRawFile(this, R.raw.triangle_vr_move);
        int shaderVertexId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int shaderFragmentId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        triangleProgramId = ProgramCreator.createProgram(shaderVertexId, shaderFragmentId);
        glUseProgram(triangleProgramId);
        positionId = glGetAttribLocation(triangleProgramId, "a_position");
        colorId = glGetUniformLocation(triangleProgramId, "u_color");
        matrixId = glGetUniformLocation(triangleProgramId, "u_matrix");
        Timber.d("Program Id = %d, pointsId = %d, matrixId = %d, " +
                "colorId = %d", triangleProgramId, positionId, matrixId, colorId);
        vertexBuffer.position(0);
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
