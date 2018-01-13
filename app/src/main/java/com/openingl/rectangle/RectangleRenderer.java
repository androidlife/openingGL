package com.openingl.rectangle;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.openinggl.R;
import com.openingl.utils.GLError;
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
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/3/17.
 */

public class RectangleRenderer implements GLSurfaceView.Renderer {

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

    private static final String VAR_COLOR = "color", VAR_POSITION = "points";
    private int colorVarId, pointsVarId;

    public RectangleRenderer(Context context) {
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
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.rectangle);
        int shaderVertexId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int shaderFragmentId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        if (shaderVertexId != GLError.OPEN_GL && shaderFragmentId != GLError.OPEN_GL) {
            int programId = ProgramCreator.createProgram(shaderVertexId, shaderFragmentId);
            if (programId != GLError.OPEN_GL) {
                glUseProgram(programId);
                //get varIds from the program
                getIdsOfVariables(programId);
                //setting the values
                floatBuffer.position(0);

                glVertexAttribPointer(pointsVarId, 2, GL_FLOAT, false, 0, floatBuffer);
                glEnableVertexAttribArray(pointsVarId);
            }
        }
    }

    /**
     * To get the id's of variable two things are important
     * 1. var name
     * 2. var type
     * in our glsl, attribute vec4 points;
     * points type is attribute and it's name is "points"
     * that's why we use glGetAttribLocation(programId,"points")
     * Q) Since both the id value is 0 ( colorVarId and pointsVarId)
     * how does OpenGL differentiate these two
     */
    private void getIdsOfVariables(int programId) {
        Timber.d("----------GET ID OF VAR-------------------");
        colorVarId = glGetUniformLocation(programId, VAR_COLOR);
        printVarIdLocation(VAR_COLOR, colorVarId);
        pointsVarId = glGetAttribLocation(programId, VAR_POSITION);
        printVarIdLocation(VAR_POSITION, pointsVarId);
    }

    private void printVarIdLocation(String varName, int varId) {
        Timber.d("variable: %s ( %d ) is declared in glsl file = %b",
                varName, varId, varId != GLError.VAR_NOT_FOUND);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUniform4f(colorVarId, 1f, 0f, 0f, 1f);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glDrawArrays(GL_TRIANGLES, 2, 3);


    }
}
