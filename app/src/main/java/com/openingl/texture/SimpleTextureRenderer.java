package com.openingl.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

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
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/12/17.
 */

public class SimpleTextureRenderer implements GLSurfaceView.Renderer {
    private Context context;
    //inverted side
    private float[] vertices = {
            //X, Y  , texture coordinate X,y = U,V
            -0.5f, -0.5f, 0f, 0f,
            0.5f, -0.5f, 1f, 0f,
            0.5f, 0.5f, 1f, 1f,
            -0.5f, 0.5f, 0f, 1f
    };

//    //normal side
//    private float[] vertices = {
//            //X, Y  , texture coordinate X,y = U,V
//            -0.5f, -0.5f, 1f, 1f,
//            0.5f, -0.5f, 0f, 1f,
//            0.5f, 0.5f, 0f, 0f,
//            -0.5f, 0.5f, 1f, 0f
//    };

    private FloatBuffer floatBuffer;

    int colorId, positionId, matrixId;
    int textureCoordinateId, textureUnitId;
    float[] aspectMatrix;
    int textureId;

    private static final int POSITION_COUNT = 2;
    private static final int TEXTURE_COORDINATE_COUNT = 2;
    private static final int ROW_COUNT = POSITION_COUNT + TEXTURE_COORDINATE_COUNT;

    public SimpleTextureRenderer(Context context) {
        this.context = context;
        floatBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices);
        aspectMatrix = new float[16];
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 1f, 1f, 1f);
        //String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.simple_texture);
        //if you want the normal orientation of the image, do the following
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, R.raw.simple_texture_inverted);
        int shaderVertexId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int shaderFragmentId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        int programId = ProgramCreator.createProgram(shaderVertexId, shaderFragmentId);
        glUseProgram(programId);
        colorId = glGetUniformLocation(programId, "u_color");
        positionId = glGetAttribLocation(programId, "a_position");
        matrixId = glGetUniformLocation(programId, "u_matrix");
        textureCoordinateId = glGetAttribLocation(programId, "a_textureCoordinate");
        textureUnitId = glGetUniformLocation(programId, "u_textureUnit");
        Timber.d("colorId = %d, positionId = %d, matrixId = %d, textureCoordinate = %d",
                colorId, positionId, matrixId, textureCoordinateId);

        textureId = generateTexture();


        floatBuffer.position(0);
        glVertexAttribPointer(positionId, 2, GL_FLOAT, false, ROW_COUNT * 4, floatBuffer);
        glEnableVertexAttribArray(positionId);
        floatBuffer.position(2);
        glVertexAttribPointer(textureCoordinateId, 2, GL_FLOAT, false, ROW_COUNT * 4,
                floatBuffer);
        glEnableVertexAttribArray(textureCoordinateId);
        //floatBuffer.position(0);

    }

    /**
     * 1. Generate textureID
     * 2. Create bitmap
     * 3. Bind textureId
     * 4. Set Filtering for min and mag
     * 5. Load the bitmap to OpenGL
     * 5. Generate the mipmap or the image
     * 6. Recycle bitmap and unbind
     */
    private int generateTexture() {
        final int[] textureId = new int[1];
        glGenTextures(1, textureId, 0);
        if (textureId[0] == 0) {
            Timber.e("Error generating texture id");
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pirate,
                options);
        if (bitmap == null) {
            Timber.e("Error retrieving the bitmap");
            glDeleteTextures(1, textureId, 0);
            return 0;
        }
        glBindTexture(GL_TEXTURE_2D, textureId[0]);

        // Set filtering: a default must be set, or the texture will be
        // black.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //generating the bitmap
        //seems like by this the bitmap goes to VRAM
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        // Note: Following code may cause an error to be reported in the
        // ADB log as follows: E/IMGSRV(20095): :0: HardwareMipGen:
        // Failed to generate texture mipmap levels (error=3)
        // No OpenGL error will be encountered (glGetError() will return
        // 0). If this happens, just squash the source image to be
        // square. It will look the same because of texture coordinates,
        // and mipmap generation will work.
        glGenerateMipmap(GL_TEXTURE_2D);

        // Recycle the bitmap, since its data has been loaded into
        // OpenGL.
        bitmap.recycle();
        //unbind from texture
        glBindTexture(GL_TEXTURE_2D, 0);

        Timber.d("TextureId  = %d", textureId[0]);
        return textureId[0];

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLHelper.performOrthographicProjection(width, height, aspectMatrix);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUniformMatrix4fv(matrixId, 1, false, aspectMatrix, 0);
        // Set the active texture unit to texture unit 0.
        //GLError.clearError();
        glActiveTexture(GL_TEXTURE0);
        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId);
        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(textureUnitId, 0);

        glDrawArrays(GL_TRIANGLE_FAN, 0, vertices.length / ROW_COUNT);

    }
}
