package com.openingl.rectangle.border;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/3/17.
 */

public class BorderRenderer implements GLSurfaceView.Renderer {

    private Context context;
    //1byte = 8 bit
    //1float = 32 bit = 4 * 8 bit = 4 byte
    float[] trianglePoints = {
            0f, 0f,
            0f, 0.5f,
            1f, 0.5f,
            1f, 0f,
            0f, 0f
    };
    FloatBuffer floatBuffer;

    private int uColorId, aPositionId, vPositionId, uWidthId;

    public BorderRenderer(Context context) {
        this.context = context;
        floatBuffer = ByteBuffer.allocateDirect(trianglePoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(trianglePoints);

    }

    /**
     * Steps
     * 1. Get the shader codes
     * 2. Create shader with those codes
     * 3. create program and link the shader with the program
     * 4. Use the program
     * 5. Get variables id from program id
     * 6. Set appropriate values to it
     */

    int programId;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        programId = ProgramCreator.createProgram(context, R.raw.border_rectangle);
        glUseProgram(programId);

        uColorId = GLHelper.getIdLocationForUniform(programId, "u_Color");
        aPositionId = GLHelper.getIdLocationForAttribute(programId, "a_Position");
        uWidthId = GLHelper.getIdLocationForUniform(programId, "u_Width");


        floatBuffer.position(0);
        glVertexAttribPointer(aPositionId, 2, GL_FLOAT, false, 0, floatBuffer);
        glEnableVertexAttribArray(aPositionId);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUniform1f(uWidthId, 0.1f);
        glUniform4f(uColorId, 1f, 0f, 0f, 1f);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glDrawArrays(GL_TRIANGLES, 2, 3);


    }
}
