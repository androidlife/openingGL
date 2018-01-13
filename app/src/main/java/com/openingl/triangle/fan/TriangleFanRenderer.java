package com.openingl.triangle.fan;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.openinggl.R;
import com.openingl.utils.ProgramCreator;
import com.openingl.utils.ShaderCreator;
import com.openingl.utils.TextReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 10/24/17.
 */

public class TriangleFanRenderer implements GLSurfaceView.Renderer {

    float[] firstTriangles = {
            //first triangle
            -0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f,
            //second triangle
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f

    };

    //now coordinate with small triangles
    float[] triangleFans = {
            0f, 0f,
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f,
            -0.5f, -0.5f
    };
    //now coordinate with color values
    //X,Y, R,G,B
    // now we need to define two variables on glsl
    // one for coordinate
    // one for color values
    float[] triangleFansWithColors = {
            0f, 0f, 1f, 1f, 1f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
            0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
            0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
            -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f
    };
//    float[] triangleFansWithColors = {
//            0f, 0f, 1f, 1f, 1f,
//            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
//            0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
//            0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
//            -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
//            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f
//    };

    FloatBuffer floatBuffer;
    Context context;

    private int programId;

    private static final int BYTES_PER_FLOAT = 4;
    //R G B
    private static final int COLOR_COMPONENT_COUNT = 3;
    // X Y
    private static final int POSITION_COUNT = 2;

    private static final int STRIDE = (POSITION_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final String COORDINATE = "coordinate";
    private static final String COLOR_VALUE = "colorValue";

    private int coordinate, colorValue;

    public TriangleFanRenderer(Context context) {
        this.context = context;
        floatBuffer = ByteBuffer.allocateDirect(triangleFansWithColors.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(triangleFansWithColors);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.trianglefan);
        int vertexShaderId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int fragmentShaderId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        if (vertexShaderId != 0 && fragmentShaderId != 0) {
            programId = ProgramCreator.createProgram(vertexShaderId, fragmentShaderId);
            if (programId != 0) {
                glUseProgram(programId);
                coordinate = glGetAttribLocation(programId, COORDINATE);
                colorValue = glGetAttribLocation(programId, COLOR_VALUE);
                Timber.d("coordinate id = %d, color value id = %d",coordinate,colorValue);
                if (coordinate != -1 && colorValue != -1) {
                    //for coordinate value
                    floatBuffer.position(0);
                    glVertexAttribPointer(coordinate, POSITION_COUNT, GL_FLOAT, false, STRIDE, floatBuffer);
                    glEnableVertexAttribArray(coordinate);
                    //for color values
                    floatBuffer.position(POSITION_COUNT);
                    glVertexAttribPointer(colorValue, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, floatBuffer);
                    glEnableVertexAttribArray(colorValue);
                }
            }
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }
}
