package com.openingl.triangle.drawelement;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/3/17.
 */

public class DrawElementRenderer implements GLSurfaceView.Renderer {

    private Context context;
    //1byte = 8 bit
    //1float = 32 bit = 4 * 8 bit = 4 byte
    //clockwise backface
    public static final float[] COORDS = new float[]{
            -0.5f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
    };

    public static final short[] INDICES = new short[]{
            0, 1, 2,
            1, 3, 2
    };
    FloatBuffer floatBuffer;
    ShortBuffer indexBuffer;

    private int colorVarId, pointsVarId;

    public DrawElementRenderer(Context context) {
        this.context = context;
        indexBuffer = ByteBuffer.allocateDirect(INDICES.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(INDICES);
        indexBuffer.position(0);
        floatBuffer = ByteBuffer.allocateDirect(COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(COORDS);
        floatBuffer.position(0);
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
        programId = ProgramCreator.createProgram(context, R.raw.draw_element);
        glUseProgram(programId);
        colorVarId = GLHelper.getIdLocationForUniform(programId, "u_Color");
        pointsVarId = GLHelper.getIdLocationForAttribute(programId, "a_Position");
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(programId);
        glUniform4f(colorVarId, 1f, 0f, 0f, 1f);
        glEnableVertexAttribArray(pointsVarId);
        glVertexAttribPointer(pointsVarId, 3, GL_FLOAT, false, 0, floatBuffer);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indexBuffer);
        glDisableVertexAttribArray(pointsVarId);

    }
}
