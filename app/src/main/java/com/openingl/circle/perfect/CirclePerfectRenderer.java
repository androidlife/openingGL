package com.openingl.circle.perfect;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

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
 * Created by laaptu on 12/3/17.
 */

public class CirclePerfectRenderer implements GLSurfaceView.Renderer {

    private Context context;
    //1byte = 8 bit
    //1float = 32 bit = 4 * 8 bit = 4 byte
    float[] circlePoints;
    FloatBuffer floatBuffer;

    private static final String VAR_UNIFORM_COLOR = "u_color", VAR_ATTRIB_POSITION = "a_position",
            VAR_UNIFORM_MATRIX = "u_matrix";
    private int varColorId, varPositionId, varMatrixId;
    float[] projectionMatrix = new float[16];

    public CirclePerfectRenderer(Context context) {
        this.context = context;
        populatePoints();

    }

    private void populatePoints() {
        float radius = 0.5f;
        float origin = 0f;
        int angle = 10;
        int totalAngle = 360;
        int count = totalAngle / angle;
        //going clockwise
        //this will start the index from 0.5,0
        //and moving on  0,-0.5
        // -0.5,0
        // 0,0.5
        int startAngle = 360;
        //why this??
        /**
         * count = totalAngle/angle = 360/90 = 4
         * if there are 4 points and we include x,y, it will be 4*2 = 8
         * why +2 again, this is for the origin point  0,0
         * and again why +2 at last, we need to end the circle. In order to end
         * the circle, we need to specify the end point
         * which becomes value at angle 0 or 360 : 0.5,0
         * other wise it won't be a complete one
         */
        circlePoints = new float[count * 2 + 2 + 2];
        int index = 0;
        circlePoints[index] = 0f;
        circlePoints[++index] = 0f;

        for (int i = 0; i < count; ++i) {
            //we need to convert the radian to degree value
            final float mainAngle = convertToDegree(startAngle);
            // simple formula to get values or x,y position of points in a circle
            final float x = getTwoDecimalValue(origin + radius * Math.cos(mainAngle));
            final float y = getTwoDecimalValue(origin + radius * Math.sin(mainAngle));
            //simply updating the indices
            // if confusing, try this example or this method with simple list and you will
            // get an overview of what is going on
            circlePoints[++index] = x;
            circlePoints[++index] = y;
            // since we need to go with clockwise direction, we are starting from 360
            // and negating the value
            startAngle -= angle;
        }
        //to complete our circle we need last points which happens to be
        // second and third coordinate
        circlePoints[++index] = circlePoints[2];
        circlePoints[++index] = circlePoints[3];
        index = 0;
        for (int i = 0; i < circlePoints.length / 2; ++i) {
            Timber.d("x:%f , y:%f", circlePoints[index + i], circlePoints[index + i + 1]);
            ++index;
        }

        floatBuffer = ByteBuffer.allocateDirect(circlePoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(circlePoints);

    }

    private float convertToDegree(int angle) {
        return (float) (angle * Math.PI / 180f);
    }

    private float getTwoDecimalValue(double value) {
        return Math.round(value * 100) / 100.0f;
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
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.circleperfect);
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

                glVertexAttribPointer(varPositionId, 2, GL_FLOAT, false, 0, floatBuffer);
                glEnableVertexAttribArray(varPositionId);
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
     * Q) Since both the id value is 0 ( varColorId and varPositionId)
     * how does OpenGL differentiate these two
     */
    private void getIdsOfVariables(int programId) {
        Timber.d("----------GET ID OF VAR-------------------");
        varColorId = glGetUniformLocation(programId, VAR_UNIFORM_COLOR);
        printVarIdLocation(VAR_UNIFORM_COLOR, varColorId);
        varPositionId = glGetAttribLocation(programId, VAR_ATTRIB_POSITION);
        printVarIdLocation(VAR_ATTRIB_POSITION, varPositionId);
        varMatrixId = glGetUniformLocation(programId, VAR_UNIFORM_MATRIX);
        printVarIdLocation(VAR_UNIFORM_MATRIX, varMatrixId);
    }

    private void printVarIdLocation(String varName, int varId) {
        Timber.d("variable: %s ( %d ) is declared in glsl file = %b",
                varName, varId, varId != GLError.VAR_NOT_FOUND);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        final float aspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUniform4f(varColorId, 1f, 0f, 0f, 1f);
        glUniformMatrix4fv(varMatrixId, 1, false, projectionMatrix, 0);
        glDrawArrays(GL_TRIANGLE_FAN, 0, circlePoints.length / 2);
    }
}
