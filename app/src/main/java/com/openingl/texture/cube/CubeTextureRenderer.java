package com.openingl.texture.cube;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateInterpolator;

import com.openinggl.R;
import com.openingl.utils.GLError;
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
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/3/17.
 */

public class CubeTextureRenderer implements GLSurfaceView.Renderer {

    private Context context;
    //1byte = 8 bit
    //1float = 32 bit = 4 * 8 bit = 4 byte
    //clockwise backface
    float[] trianglePoints = {
            //front face
            // X  Y Z  U V
            -0.5f, -0.5f, 0.5f, 0f, 0f,
            0.5f, -0.5f, 0.5f, 1f, 0f,
            0.5f, 0.5f, 0.5f, 1f, 1f,

            -0.5f, -0.5f, 0.5f, 0f, 0f,
            0.5f, 0.5f, 0.5f, 1f, 1f,
            -0.5f, 0.5f, 0.5f, 0f, 1f,
            //side face 1
            0.5f, -0.5f, 0.5f, 0f, 0f,
            0.5f, -0.5f, -0.5f, 1f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 1f,

            0.5f, -0.5f, 0.5f, 0f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 1f,
            0.5f, 0.5f, 0.5f, 0f, 1f,
            //back face
            -0.5f, -0.5f, -0.5f, 0f, 0f,
            0.5f, -0.5f, -0.5f, 1f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 1f,

            -0.5f, -0.5f, -0.5f, 0f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 1f,
            -0.5f, 0.5f, -0.5f, 0f, 1f,
            //side face 2
            -0.5f, -0.5f, 0.5f, 0f, 0f,
            -0.5f, -0.5f, -0.5f, 1f, 0f,
            -0.5f, 0.5f, -0.5f, 1f, 1f,

            -0.5f, -0.5f, 0.5f, 0f, 0f,
            -0.5f, 0.5f, -0.5f, 1f, 1f,
            -0.5f, 0.5f, 0.5f, 0f, 1f,
            //top face
            -0.5f, 0.5f, 0.5f, 0f, 0f,
            0.5f, 0.5f, 0.5f, 1f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 1f,

            -0.5f, 0.5f, 0.5f, 0f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 1f,
            -0.5f, 0.5f, -0.5f, 0f, 1f,
            //bottom face
            -0.5f, -0.5f, 0.5f, 0f, 0f,
            0.5f, -0.5f, 0.5f, 1f, 0f,
            0.5f, -0.5f, -0.5f, 1f, 1f,

            -0.5f, -0.5f, 0.5f, 0f, 0f,
            0.5f, -0.5f, -0.5f, 1f, 1f,
            -0.5f, -0.5f, -0.5f, 0f, 1f
    };
    FloatBuffer floatBuffer;

    private int colorVarId, pointsVarId, matrixId;
    private int textureId, textureUnitId;

    private float[] aspectMatrix;

    private static final int POSITION_COUNT = 3, COLOR_COUNT = 2;
    private static final int STRIDE = POSITION_COUNT + COLOR_COUNT;


    private float[] cubeTransform;
    // Viewing variables
    private float[] cubeView;
    private ValueAnimator valueAnimator;

    public CubeTextureRenderer(Context context) {
        this.context = context;
        floatBuffer = ByteBuffer.allocateDirect(trianglePoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(trianglePoints);
        aspectMatrix = new float[16];

        //
        cubeView = new float[16];
        cubeTransform = new float[16];
        // Rotate and position the cube

        valueAnimator = ValueAnimator.ofInt(0, -360);
        valueAnimator.setDuration(4000);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle = (int) animation.getAnimatedValue();
            }
        });
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);

    }

    private int angle = 0, prevAngle = 0;
    private Handler handler = new Handler(Looper.getMainLooper());


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
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.cube_texture);
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
                glVertexAttribPointer(pointsVarId, POSITION_COUNT, GL_FLOAT, false,
                        STRIDE * 4, floatBuffer);
                glEnableVertexAttribArray(pointsVarId);
                floatBuffer.position(POSITION_COUNT);
                glVertexAttribPointer(colorVarId, COLOR_COUNT, GL_FLOAT, false,
                        STRIDE * 4, floatBuffer);
                glEnableVertexAttribArray(colorVarId);
            }
        }
        textureId = GLHelper.generateTextureId(BitmapFactory.decodeResource(context.getResources(), R.drawable.pirate),true);
        handler.post(new Runnable() {
            @Override
            public void run() {
                valueAnimator.start();
            }
        });

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
        colorVarId = glGetAttribLocation(programId, "a_texturePos");
        printVarIdLocation("a_texturePos", colorVarId);
        pointsVarId = glGetAttribLocation(programId, "a_position");
        printVarIdLocation("a_position", pointsVarId);
        matrixId = glGetUniformLocation(programId, "u_matrix");
        printVarIdLocation("u_matrix", matrixId);
        textureUnitId = glGetUniformLocation(programId, "u_textureUnit");
        printVarIdLocation("u_textureUnit", textureUnitId);

    }

    private void printVarIdLocation(String varName, int varId) {
        Timber.d("variable: %s ( %d ) is declared in glsl file = %b",
                varName, varId, varId != GLError.VAR_NOT_FOUND);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLHelper.performOrthographicProjection(width, height, aspectMatrix);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (prevAngle != angle) {
            Matrix.setIdentityM(cubeTransform, 0);
            Matrix.rotateM(cubeTransform, 0, angle, 1, 1, 0);
            prevAngle = angle;
        }
        Matrix.multiplyMM(cubeView, 0, aspectMatrix, 0, cubeTransform, 0);
        glUniformMatrix4fv(matrixId, 1, false, cubeView, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(textureUnitId, 0);


        glDrawArrays(GL_TRIANGLES, 0, trianglePoints.length / STRIDE);
    }
}
