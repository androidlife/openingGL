package com.openingl.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import timber.log.Timber;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by laaptu on 12/8/17.
 */

public class GLHelper {

    public static void performOrthographicProjection(int width, int height, float[] projectionMatrix) {
        final float aspectRatio = width > height ? (float) width / (float) height
                : (float) height / (float) width;
        if (width > height) {
            Matrix.orthoM(projectionMatrix, 0,
                    -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f,
                    -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    public static void printMatrixByRow(float[] matrix, String label) {
        if (matrix.length != 16)
            return;
        Timber.d(" -------%s Matrix-------------", label);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; ++i) {
            stringBuilder.append(String.format("|%.2f %.2f %.2f %.2f|", matrix[i],
                    matrix[4 + i], matrix[8 + i], matrix[12 + i]));
            stringBuilder.append("\n");
        }
        Timber.d(stringBuilder.toString());
    }

    public static void printMatrixNormalOrder(float[] matrix, String label) {
        if (matrix.length != 16)
            return;
        Timber.d(" -------%s Matrix-------------", label);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; ++i) {
            int index = 4 * i - 1;
            stringBuilder.append(String.format("|%.2f %.2f %.2f %.2f|", matrix[++index],
                    matrix[++index], matrix[++index], matrix[++index]));
            stringBuilder.append("\n");
        }
        Timber.d(stringBuilder.toString());
    }


    public static int getIdLocationForUniform(int programId, String varName) {
        int uniformId = GLES20.glGetUniformLocation(programId, varName);
        printId(uniformId, varName, true);
        return uniformId;
    }

    public static int getIdLocationForAttribute(int programId, String varName) {
        int uniformId = GLES20.glGetAttribLocation(programId, varName);
        printId(uniformId, varName, false);
        return uniformId;
    }

    private static void printId(int id, String varName, boolean isUniform) {
        if (id == -1)
            Timber.e("Error getting %s varname = %s and it's id = %d",
                    isUniform ? "UNIFORM" : "ATTRIBUTE",
                    varName, id);
        else
            Timber.d("Success getting %s varname = %s and it's id = %d",
                    isUniform ? "UNIFORM" : "ATTRIBUTE",
                    varName, id);
    }

    public static int generateTextureId(Bitmap bitmap, boolean recycle) {
        int[] textureIds = new int[1];
        glGenTextures(1, textureIds, 0);
        if (textureIds[0] == GLError.OPEN_GL) {
            Timber.e("Error generating the textureId");
            if (bitmap != null)
                bitmap.recycle();
            return GLError.OPEN_GL;
        }
        glBindTexture(GL_TEXTURE_2D, textureIds[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        glGenerateMipmap(GL_TEXTURE_2D);

        if (recycle)
            bitmap.recycle();
        glBindTexture(GL_TEXTURE_2D, 0);
        Timber.d("Successfully creating texture id = %d and binding it to the bitmap", textureIds[0]);
        return textureIds[0];
    }

    public static void enableTexture(int textureId, int aTexturePosId, int uTextureUnit, FloatBuffer textPosBuffer) {
        textPosBuffer.position(0);
        glVertexAttribPointer(aTexturePosId, 2, GL_FLOAT, false, 0, textPosBuffer);
        glEnableVertexAttribArray(aTexturePosId);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnit, 0);
    }

    public static Bitmap getBitmapFrom(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }

    private static final int BYTES_PER_FLOAT = 4;

    public static FloatBuffer allocateFloatBuffer(float[] vertices, int startPosition) {
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices);
        floatBuffer.position(startPosition);
        return floatBuffer;
    }

    public static ShortBuffer allocateShortBuffer(short[] shorts, int startPosition) {
        ShortBuffer shortBuffer
                = ByteBuffer.allocateDirect(shorts.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(shorts);
        shortBuffer.position(0);
        return shortBuffer;
    }

    public static void orthoM(float[] m, int mOffset,
                              float left, float right, float bottom, float top,
                              float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        }
        if (bottom == top) {
            throw new IllegalArgumentException("bottom == top");
        }
        if (near == far) {
            throw new IllegalArgumentException("near == far");
        }

        final float r_width = 1.0f / (right - left);
        final float r_height = 1.0f / (top - bottom);
        final float r_depth = 1.0f / (far - near);
        final float x = 3.6f * (r_width);
        final float y = 2.0f * (r_height);
        final float z = -2.0f * (r_depth);
        final float tx = -(1.8f * (right + left)) * r_width;
        final float ty = -(top + bottom) * r_height;
        final float tz = -(far + near) * r_depth;
        m[mOffset + 0] = x;
        m[mOffset + 5] = y;
        m[mOffset + 10] = z;
        m[mOffset + 12] = tx;
        m[mOffset + 13] = ty;
        m[mOffset + 14] = tz;
        m[mOffset + 15] = 1.0f;
        m[mOffset + 1] = 0.0f;
        m[mOffset + 2] = 0.0f;
        m[mOffset + 3] = 0.0f;
        m[mOffset + 4] = 0.0f;
        m[mOffset + 6] = 0.0f;
        m[mOffset + 7] = 0.0f;
        m[mOffset + 8] = 0.0f;
        m[mOffset + 9] = 0.0f;
        m[mOffset + 11] = 0.0f;
    }

    public static void orthoM(float[] m, int mOffset,
                              float left, float right, float bottom, float top,
                              float near, float far, float xRatio, float yRatio) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        }
        if (bottom == top) {
            throw new IllegalArgumentException("bottom == top");
        }
        if (near == far) {
            throw new IllegalArgumentException("near == far");
        }

        final float r_width = 1.0f / (right - left);
        final float r_height = 1.0f / (top - bottom);
        final float r_depth = 1.0f / (far - near);
        final float x = (xRatio * 2f) * (r_width);
        final float y = (yRatio * 2.0f) * (r_height);
//        final float z = -2.0f * (r_depth);
        final float z = 1f;
        final float tx = -(xRatio * (right + left)) * r_width;
        final float ty = -(yRatio * (top + bottom)) * r_height;
//        final float tz = -(far + near) * r_depth;
        final float tz = 0f;
        m[mOffset + 0] = x;
        m[mOffset + 5] = y;
        m[mOffset + 10] = z;
        m[mOffset + 12] = tx;
        m[mOffset + 13] = ty;
        m[mOffset + 14] = tz;
        m[mOffset + 15] = 1.0f;
        m[mOffset + 1] = 0.0f;
        m[mOffset + 2] = 0.0f;
        m[mOffset + 3] = 0.0f;
        m[mOffset + 4] = 0.0f;
        m[mOffset + 6] = 0.0f;
        m[mOffset + 7] = 0.0f;
        m[mOffset + 8] = 0.0f;
        m[mOffset + 9] = 0.0f;
        m[mOffset + 11] = 0.0f;
    }

    //we are not intereseted in z axis right now
    public static void orthoM1(float[] m, int mOffset,
                              float left, float right, float bottom, float top,
                              float xRatio, float yRatio) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        }
        if (bottom == top) {
            throw new IllegalArgumentException("bottom == top");
        }
//        if (near == far) {
//            throw new IllegalArgumentException("near == far");
//        }

        final float r_width = 1.0f / (right - left);
        final float r_height = 1.0f / (top - bottom);
        //final float r_depth = 1.0f / (far - near);
        final float x = (xRatio * 2f) * (r_width);
        final float y = (yRatio * 2.0f) * (r_height);
        //final float z = -2.0f * (r_depth);
        final float z = 1f;
        final float tx = -(xRatio * (right + left)) * r_width;
        final float ty = -(yRatio * (top + bottom)) * r_height;
        //final float tz = -(far + near) * r_depth;
        final float tz = 0f;
        m[mOffset + 0] = x;
        m[mOffset + 5] = y;
        m[mOffset + 10] = z;
        m[mOffset + 12] = tx;
        m[mOffset + 13] = ty;
        m[mOffset + 14] = tz;
        m[mOffset + 15] = 1.0f;
        m[mOffset + 1] = 0.0f;
        m[mOffset + 2] = 0.0f;
        m[mOffset + 3] = 0.0f;
        m[mOffset + 4] = 0.0f;
        m[mOffset + 6] = 0.0f;
        m[mOffset + 7] = 0.0f;
        m[mOffset + 8] = 0.0f;
        m[mOffset + 9] = 0.0f;
        m[mOffset + 11] = 0.0f;
    }

    public static float[] drawCircle(float cx, float cy, float r, float z, int numSegments) {
        float theta = (float) (2 * Math.PI) / (float) numSegments;
        float c = (float) Math.cos(theta);//precalculate the sine and cosine
        float s = (float) Math.sin(theta);

        float t;

        float x = r;//we start at angle = 0
        float y = 0;

        float[] positions = new float[numSegments * 3];
        int index = -1;
        for (int i = 0; i < numSegments; i++) {
            positions[++index] = x + cx;
            positions[++index] = y + cy;
            positions[++index] = z;
            //apply the rotation matrix
            t = x;
            x = c * x - s * y;
            y = s * t + c * y;
        }
        return positions;
    }


}
