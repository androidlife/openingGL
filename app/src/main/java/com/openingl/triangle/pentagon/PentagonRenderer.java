package com.openingl.triangle.pentagon;

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
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 10/24/17.
 */

public class PentagonRenderer implements GLSurfaceView.Renderer {

    //now varPositionId with color values
    //X,Y, R,G,B
    // now we need to define two variables on glsl
    // one for varPositionId
    // one for color values
    float[] pentagonPoints = {
            0f, 0.5f,
            0.6f, 0.2f,
            0.4f, -0.5f,
            -0.4f, -0.5f,
            -0.6f, 0.2f
    };

    FloatBuffer floatBuffer;
    Context context;

    private int programId;

    private static final String VAR_ATTRIB_POSTION = "a_position";
    private static final String VAR_UNIFORM_COLOR = "u_color";
    private static final String VAR_UNIFORM_MATRIX = "u_matrix";

    private int varPositionId, varColorId, varMatrixId;

    final float[] projectionMatrix = new float[16];

    public PentagonRenderer(Context context) {
        this.context = context;
        floatBuffer = ByteBuffer.allocateDirect(pentagonPoints.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(pentagonPoints);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.polygon);
        int vertexShaderId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int fragmentShaderId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        if (vertexShaderId != 0 && fragmentShaderId != 0) {
            programId = ProgramCreator.createProgram(vertexShaderId, fragmentShaderId);
            if (programId != 0) {
                glUseProgram(programId);
                varPositionId = glGetAttribLocation(programId, VAR_ATTRIB_POSTION);
                varColorId = glGetUniformLocation(programId, VAR_UNIFORM_COLOR);
                varMatrixId = glGetUniformLocation(programId, VAR_UNIFORM_MATRIX);
                Timber.d("varPositionId id = %d, color value id = %d, matrix id = %d",
                        varPositionId, varColorId, varMatrixId);
                if (varPositionId != -1 && varColorId != -1) {
                    floatBuffer.position(0);
                    glVertexAttribPointer(varPositionId, 2, GL_FLOAT, false, 0,
                            floatBuffer);
                    glEnableVertexAttribArray(varPositionId);
                }
            }
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLHelper.performOrthographicProjection(width,height,projectionMatrix);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUniformMatrix4fv(varMatrixId, 1, false, projectionMatrix, 0);
        glUniform4f(varColorId, 1f, 0f, 0f, 1f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, pentagonPoints.length / 2);
    }
}
