package com.openingl.triangle.two;

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
 * Created by laaptu on 12/4/17.
 */

public class TriangleDoubleRenderer implements GLSurfaceView.Renderer {
    float[] verticesFirst = {
            //first triangle go clockwise
            0f, 0.5f,
            -1f, 1f,
            0f, 1f,
            //second triangle
            0f, 0.5f,
            1f, 0.5f,
            0f, 0f
    };


    FloatBuffer firstTriangleBuffer;
    private Context context;

    private final String VAR_COLOR = "colorValue", VAR_POSITION = "coordinate";
    private int varColorId, varPositionId;

    public TriangleDoubleRenderer(Context context) {
        firstTriangleBuffer = ByteBuffer.allocateDirect(verticesFirst.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(verticesFirst);
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.triangledouble);
        createFirstProgram(shaderCodes[0], shaderCodes[1]);

    }

    private void createFirstProgram(String vertexCode, String fragmentCode) {
        int shaderVertexId = ShaderCreator.createVertexShader(vertexCode);
        int fragmentVertexId = ShaderCreator.createFragmentShader(fragmentCode);
        if (shaderVertexId != GLError.OPEN_GL && fragmentVertexId != GLError.OPEN_GL) {
            int programId = ProgramCreator.createProgram(shaderVertexId, fragmentVertexId);
            if (programId != GLError.OPEN_GL) {
                glUseProgram(programId);
                getIdOfVariables(programId);
                firstTriangleBuffer.position(0);
                glVertexAttribPointer(varPositionId, 2, GL_FLOAT,
                        false, 0, firstTriangleBuffer);
                glEnableVertexAttribArray(varPositionId);
            }
        }
    }

    private void getIdOfVariables(int programId) {
        varColorId = glGetUniformLocation(programId, VAR_COLOR);
        printVarIdLocation(VAR_COLOR, varColorId);
        varPositionId = glGetAttribLocation(programId, VAR_POSITION);
        printVarIdLocation(VAR_POSITION, varPositionId);
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
        glUniform4f(varColorId, 1f, 0f, 0f, 1f);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glUniform4f(varColorId, 0f, 1f, 0f, 1f);
        glDrawArrays(GL_TRIANGLES, 3, 3);
    }
}
