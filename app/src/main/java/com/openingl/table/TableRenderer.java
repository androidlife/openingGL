package com.openingl.table;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
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
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 10/24/17.
 */

public class TableRenderer implements GLSurfaceView.Renderer {

    float[] tableVerticesWithTriangles = {
            // Order of coordinates: X, Y, Z, W, R, G, B

            // Triangle Fan
            0f, 0f, 0f, 1.5f, 1f, 1f, 1f,
            -0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,
            0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,
            0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f,
            -0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,

            // Line 1
            -0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,
            0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,

            // Mallets
            0f, -0.4f, 0f, 1.25f, 0f, 0f, 1f,
            0f, 0.4f, 0f, 1.75f, 1f, 0f, 0f
    };

    FloatBuffer floatBuffer;
    Context context;

    private int programId;

    private static final String VAR_ATTRIB_POSTION = "a_position";
    private static final String VAR_ATTRIB_COLOR = "a_color";
    private static final String VAR_UNIFORM_MATRIX = "u_matrix";

    private int varPositionId, varColorId, varMatrixId;

    final float[] projectionMatrix = new float[16];

    public TableRenderer(Context context) {
        this.context = context;
        floatBuffer = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.table);
        int vertexShaderId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int fragmentShaderId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        if (vertexShaderId != 0 && fragmentShaderId != 0) {
            programId = ProgramCreator.createProgram(vertexShaderId, fragmentShaderId);
            if (programId != 0) {
                glUseProgram(programId);
                varPositionId = glGetAttribLocation(programId, VAR_ATTRIB_POSTION);
                varColorId = glGetAttribLocation(programId, VAR_ATTRIB_COLOR);
                varMatrixId = glGetUniformLocation(programId, VAR_UNIFORM_MATRIX);
                Timber.d("varPositionId id = %d, color value id = %d, matrix id = %d",
                        varPositionId, varColorId, varMatrixId);
                if (varPositionId != -1 && varColorId != -1) {
                    floatBuffer.position(0);
                    glVertexAttribPointer(varPositionId, 4, GL_FLOAT, false, (4 + 3) * 4,
                            floatBuffer);
                    glEnableVertexAttribArray(varPositionId);
                    floatBuffer.position(4);
                    glVertexAttribPointer(varColorId, 3, GL_FLOAT, false, (4 + 3) * 4,
                            floatBuffer);
                    glEnableVertexAttribArray(varColorId);

                }
            }
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLHelper.performOrthographicProjection(width, height, projectionMatrix);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUniformMatrix4fv(varMatrixId, 1, false, projectionMatrix, 0);
        //Draw the table
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        //Draw the line
        glDrawArrays(GL_LINES, 6, 2);
        //Draw the mallet
        glDrawArrays(GL_POINTS, 8, 1);
        //Draw another mallet
        glDrawArrays(GL_POINTS, 9, 1);
    }
}
