package com.openingl.circle.color;

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
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/3/17.
 */

public class CircleColorRenderer implements GLSurfaceView.Renderer {

    private Context context;
    //1byte = 8 bit
    //1float = 32 bit = 4 * 8 bit = 4 byte
    float[] circlePoints;
    FloatBuffer floatBuffer;

    private static final String VAR_COLOR = "color", VAR_POSITION = "points";
    private int colorVarId, pointsVarId;

    public CircleColorRenderer(Context context) {
        this.context = context;
        populatePoints();

    }

    private void populatePoints() {
        float radius = 0.5f;
        float origin = 0f;
        int angle = 90;
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
        //360/90 = 4 and two coordinates
        //first coordinate => central vertex from where rendering starts
        //second coordinate => closing coordinate value
        int totalCoordinates = count + 2;
        //x, y coorindate will be 2 so
        // and color values be 3
        circlePoints = new float[totalCoordinates * (2 + 3)];
        int index = 0;
        circlePoints[index] = 0f;
        circlePoints[++index] = 0f;
        boolean green = true;
        index = addColor(index, green);
        green = !green;
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
            index = addColor(index, green);
            green = !green;
            // since we need to go with clockwise direction, we are starting from 360
            // and negating the value
            startAngle -= angle;
        }
        //to complete our circle we need last points which happens to be
        // second and third coordinate
        circlePoints[++index] = circlePoints[5];
        circlePoints[++index] = circlePoints[6];
        addColor(index, green);
        index = -1;
        for (int i = 0; i < circlePoints.length / (2 + 3); ++i) {
            Timber.d("x:%f , y:%f , r:%f, g:%f, b:%f",
                    circlePoints[++index], circlePoints[++index],
                    circlePoints[++index], circlePoints[++index], circlePoints[++index]);
        }

        floatBuffer = ByteBuffer.allocateDirect(circlePoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(circlePoints);

     /*
     float[] triangleFansWithColors = {
     //our float will be in this format not
                X   Y   R   G   B
                0f, 0f, 1f, 1f, 1f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f
        };*/

    }

    private int addColor(int index, boolean green) {
        if (green) {
            circlePoints[++index] = 0f;
            circlePoints[++index] = 1f;
            circlePoints[++index] = 0f;
        } else {
            circlePoints[++index] = 1f;
            circlePoints[++index] = 0f;
            circlePoints[++index] = 0f;
        }
        return index;
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
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.circlecolor);
        int shaderVertexId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int shaderFragmentId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        if (shaderVertexId != GLError.OPEN_GL && shaderFragmentId != GLError.OPEN_GL) {
            int programId = ProgramCreator.createProgram(shaderVertexId, shaderFragmentId);
            if (programId != GLError.OPEN_GL) {
                glUseProgram(programId);
                //get varIds from the program
                getIdsOfVariables(programId);
                //setting the values
                /**
                 * Here we are going to use the same float[] for both the color and position
                 * So, we need to explicitly say OpenGL that how to loop through the float[]
                 * or how to get values
                 * 1. First we set to the initial position
                 * 2. Then assign a pointVarId which will contain these values
                 * 3. Size will be 2 for points ( X,Y)
                 * and another thing is stride, stride is saying that when it loops or needs
                 * another position how should it proceed
                 * Here we have 2 points (X,Y) and 3 values ( R,G,B)
                 *       1f, 1f, R,G,B
                 *       0.5f,0.5f,R1,G1,B1
                 *    OpenGl reads the first values 1f, and 2f and now it needs to go
                 *    to second value
                 *    and we are saying stride = ( 2+3) = 5 i.e. the next two points
                 *    are found on index 5 and 6
                 *    and we are also doing ( 2+3)*4
                 *    In OpenGl everything is byte and we have initialized the same in floatBuffer
                 *    = Float.length*4
                 *    so this is how it is arranged in openGl
                 *
                 *   [4byte][4byte][4byte][4byte][4byte]
                 *   [1f]   [1f]   [R]    [G]    [B]
                 *   so after ready 2*4 bytes for position, it needs to go next 3*4 bytes to reach
                 *   the second points (X,Y) it needs to skip 20 bytes ( 4*(2+3)
                 */
                floatBuffer.position(0);
                glVertexAttribPointer(pointsVarId, 2, GL_FLOAT, false, (2 + 3) * 4, floatBuffer);
                glEnableVertexAttribArray(pointsVarId);

                /**
                 * Now we are assigning for color values
                 * we need to give the color id -> colorVarId
                 * First we need to shift the float buffer 2 points ( i.e.
                 * going beyond X,Y
                 * and total float points will be 3 ( R,G,B)
                 * and it's stride is also (2+3)*4
                 * Index: 0  1   2  3  4
                 *      [X1][Y1][R1][G1][B1]
                 *        5  6   7  8  9
                 *      [X2][Y2][R2][G2][B2]
                 *
                 *    So starting point is 2 and to read next item
                 *    which is on index 7
                 *    (2+3) = 5,  2+5 = 7
                 *    This is how it goes to next point
                 */
                floatBuffer.position(2);
                glVertexAttribPointer(colorVarId, 3, GL_FLOAT, false, (2 + 3) * 4, floatBuffer);
                glEnableVertexAttribArray(colorVarId);

                /**
                 * Even though we have done this we need to properly iterate the array
                 * Say our array length is
                 * 15
                 *  x1,y1,R1,G1,B1
                 *  x2,y2,R2,G2,B2
                 *  x3,y3,R3,G3,B3
                 *  as per the position we have given to the glVertexAttribPointer
                 *  for both points and colors
                 *  we can only loop 3 times
                 *  glDrawArrays(GL_TRIANGLE_FAN, 0, circlePoints.length / (2 + 3));
                 *  15/(2+3) = 15/5 = 3
                 */


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
        colorVarId = glGetAttribLocation(programId, VAR_COLOR);
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
        glDrawArrays(GL_TRIANGLE_FAN, 0, circlePoints.length / (2 + 3));

    }
}
