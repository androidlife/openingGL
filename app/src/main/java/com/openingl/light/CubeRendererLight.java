package com.openingl.light;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.openinggl.R;
import com.openingl.utils.GLHelper;
import com.openingl.utils.ProgramCreator;

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
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/3/17.
 */

public class CubeRendererLight implements GLSurfaceView.Renderer {

    private Context context;
    //1byte = 8 bit
    //1float = 32 bit = 4 * 8 bit = 4 byte
    //clockwise backface
    float[] cubeVertex = {
            //front face
            // X  Y Z R G B
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,

            -0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            //side face 1
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,

            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            //back face
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,

            -0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            //side face 2
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,

            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            //top face
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,

            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            //bottom face
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,

            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f
    };


    private float[] colors = {
            //front face
            1f, 0f, 0f, 1f,
            //side face 1
            0f, 1f, 0f, 1f,
            //back face
            0f, 0f, 1f, 1f,
            //side face 2
            1f, 1f, 0f, 1f,
            //top face
            1f, 0f, 1f, 1f,
            //bottom face
            0f, 1f, 1f, 1f
    };
    //don't know why these normals are used
    // and how are these constructed
    public static final float[] CUBE_NORMALS_FACES = new float[]{
            // Front face
            0.0f, 0.0f, 1.0f,
            // Right face
            1.0f, 0.0f, 0.0f,
            // Back face
            0.0f, 0.0f, -1.0f,
            // Left face
            -1.0f, 0.0f, 0.0f,
            // Top face
            0.0f, 1.0f, 0.0f,
            // Bottom face
            0.0f, -1.0f, 0.0f,
    };
    FloatBuffer floatBufferCube, colorBufferCube, normalBufferCube;

    private int colorVarIdCube, pointsVarIdCube, matrixIdCube;
    private int colorVarIdLight, pointsVarIdLight, matrixIdLight;

    private int programIdCube, programIdLight;


    private float[] mvpCube, mvpLight;
    private float[] modelCube, modelLight;
    private float[] cubeView,lightView;
    private float[] camera;
    float[] perspective;

    public CubeRendererLight(Context context) {
        this.context = context;
        floatBufferCube = ByteBuffer.allocateDirect(cubeVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(cubeVertex);

        float[] colorsOther = cubeFacesToArray(colors, 4);


        colorBufferCube = ByteBuffer.allocateDirect(colorsOther.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(colorsOther);

        float[] normalBufferValues = cubeFacesToArray(CUBE_NORMALS_FACES, 3);
        normalBufferCube = ByteBuffer.allocateDirect(normalBufferValues.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(normalBufferValues);


        mvpCube = new float[16];
        mvpLight = new float[16];
        modelCube = new float[16];
        modelLight = new float[16];
        camera = new float[16];
        cubeView = new float[16];
        lightView = new float[16];
        perspective = new float[16];

        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, 0, -0.5f, -5.0f);
        Matrix.rotateM(modelCube, 0, -45, 0.5f, 0.5f, 0.0f);


        Matrix.setIdentityM(modelLight, 0);
        Matrix.translateM(modelLight, 0, 1.0f, 1.0f, -6.0f);
        Matrix.scaleM(modelLight,0,0.5f,0.5f,0.5f);
        Matrix.rotateM(modelLight, 0, -45, 0.5f, 0.5f, 0.0f);



        Matrix.setLookAtM(camera, 0,
                0f, 0f, 0.0f,
                0f, 0f, -1.0f,
                0f, 1.0f, 0f);


        Matrix.multiplyMM(cubeView, 0, camera, 0, modelCube, 0);
        Matrix.multiplyMM(lightView, 0, camera, 0, modelLight, 0);

    }

    private float[] cubeFacesToArray(float[] model, int coordsPerVertex) {
        float[] coords = new float[6 * 6 * coordsPerVertex];
        int index = 0;
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                for (int k = 0; k < coordsPerVertex; ++k) {
                    int otherIndex = i * coordsPerVertex + k;
                    coords[index] = model[otherIndex];
                    ++index;
                }
            }
        }
        return coords;
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
        //glClearColor(1f, 1f, 1f, 1f);
        programIdCube = ProgramCreator.createProgram(context, R.raw.cube_light);
        getIdsOfVariables(programIdCube);

        programIdLight = ProgramCreator.createProgram(context, R.raw.cube_light_source);
        colorVarIdLight = GLHelper.getIdLocationForUniform(programIdLight, "u_color");
        pointsVarIdLight = GLHelper.getIdLocationForAttribute(programIdLight, "a_position");
        matrixIdLight = GLHelper.getIdLocationForUniform(programIdLight, "u_matrix");


    }

    /**
     * To get the id's of variable two things are important
     * 1. var name
     * 2. var type
     * in our glsl, attribute vec4 points;
     * points type is attribute and it's name is "points"
     * that's why we use glGetAttribLocation(programIdCube,"points")
     * Q) Since both the id value is 0 ( colorVarIdCube and pointsVarIdCube)
     * how does OpenGL differentiate these two
     */
    private void getIdsOfVariables(int programId) {
        Timber.d("----------GET ID OF VAR-------------------");
        colorVarIdCube = GLHelper.getIdLocationForAttribute(programId, "a_color");
        pointsVarIdCube = GLHelper.getIdLocationForAttribute(programId, "a_position");
        matrixIdCube = GLHelper.getIdLocationForUniform(programId, "u_matrix");
        //


    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Matrix.perspectiveM(perspective, 0, 45,
                (float) width / (float) height, 0.1f, 100.0f);
        Matrix.multiplyMM(mvpCube, 0, perspective, 0, cubeView, 0);
        Matrix.multiplyMM(mvpLight, 0, perspective, 0, lightView, 0);

    }




    @Override
    public void onDrawFrame(GL10 gl) {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        drawCube();
        drawLight();

    }

    private void drawLight() {
        glUseProgram(programIdLight);

        floatBufferCube.position(0);
        glVertexAttribPointer(pointsVarIdLight, 3, GL_FLOAT, false, 0, floatBufferCube);
        glEnableVertexAttribArray(pointsVarIdLight);

        glUniform4f(colorVarIdLight, 1f, 1f, 1f, 1f);
        glUniformMatrix4fv(matrixIdLight, 1, false, mvpLight, 0);
        glDrawArrays(GL_TRIANGLES, 0, cubeVertex.length / 3);

        glDisableVertexAttribArray(pointsVarIdCube);
        glDisableVertexAttribArray(colorVarIdLight);

    }

    private void drawCube() {
        //draw main cube
        glUseProgram(programIdCube);

        floatBufferCube.position(0);
        glVertexAttribPointer(pointsVarIdCube, 3, GL_FLOAT, false, 0, floatBufferCube);
        glEnableVertexAttribArray(pointsVarIdCube);

        colorBufferCube.position(0);
        glVertexAttribPointer(colorVarIdCube, 4, GL_FLOAT, false, 0, colorBufferCube);
        glEnableVertexAttribArray(colorVarIdCube);

        glUniformMatrix4fv(matrixIdCube, 1, false, mvpCube, 0);
        glDrawArrays(GL_TRIANGLES, 0, cubeVertex.length / 3);

        glDisableVertexAttribArray(pointsVarIdCube);
        glDisableVertexAttribArray(colorVarIdCube);
    }
}
