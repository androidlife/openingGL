package com.openingl.vr.triangle;

import android.os.Bundle;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.openinggl.R;
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
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 */

public class SingleTriangleActivity extends GvrActivity implements GvrView.StereoRenderer {

    @BindView(R.id.gvr_view)
    GvrView gvrView;

    float[] vertices = {
            0.0f, 0.0f, 0.0f, // top
            0f, 0.5f, 0.0f,// bottom left
            0.5f, 0f, 0.0f// bottom right
    };
    FloatBuffer vertexBuffer;
    int colorId, positionId;
    int triangleProgramId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplevr);
        ButterKnife.bind(this);
        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(vertices);
        gvrView.setRenderer(this);
        setGvrView(gvrView);

    }


    //Some stereo rendering things
    @Override
    public void onNewFrame(HeadTransform headTransform) {
    }

    @Override
    public void onDrawEye(Eye eye) {
        drawTriangle();
    }

    private void drawTriangle() {
        //the following steps are important on Google Vr
        glUseProgram(triangleProgramId);
        glVertexAttribPointer(positionId, 3, GL_FLOAT, false, 0, vertexBuffer);
        //this is same in open gl
        glUniform4f(colorId, 1f, 0f, 0f, 1f);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void enableTriangleData() {
        String[] shaderCodes = TextReader.readShaderFromRawFile(this, R.raw.triangle_vr);
        int shaderVertexId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int shaderFragmentId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        triangleProgramId = ProgramCreator.createProgram(shaderVertexId, shaderFragmentId);
        glUseProgram(triangleProgramId);
        positionId = glGetAttribLocation(triangleProgramId, "a_position");
        colorId = glGetUniformLocation(triangleProgramId, "u_color");
        Timber.d("Program Id = %d, pointsId = %d, colorId = %d", triangleProgramId, positionId, colorId);

        vertexBuffer.position(0);
        glVertexAttribPointer(positionId, 3, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(positionId);
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
