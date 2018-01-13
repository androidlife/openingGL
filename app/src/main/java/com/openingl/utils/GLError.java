package com.openingl.utils;

import timber.log.Timber;

import static android.opengl.GLES20.GL_INVALID_ENUM;
import static android.opengl.GLES20.GL_INVALID_FRAMEBUFFER_OPERATION;
import static android.opengl.GLES20.GL_INVALID_OPERATION;
import static android.opengl.GLES20.GL_INVALID_VALUE;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.GL_OUT_OF_MEMORY;
import static android.opengl.GLES20.glGetError;

/**
 * Created by laaptu on 12/3/17.
 */

public class GLError {
    public static final int OPEN_GL = 0;
    public static final int VAR_NOT_FOUND = -1;

    public static void clearError() {
        while (glGetError() != GL_NO_ERROR) ;
    }

    public static void checkError(String label) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            final String errorMessage = String.format("GLError code = %d , error value = %s, for label = %s",
                    error,getErrorValue(error),label);
            Timber.e(errorMessage);
            throw  new RuntimeException(errorMessage);
        }
    }

    private static String getErrorValue(int errorCode) {
        switch (errorCode) {
            case GL_INVALID_OPERATION:
                return "INVALID_OPERATION";
            case GL_INVALID_ENUM:
                return "INVALID_ENUM";
            case GL_INVALID_VALUE:
                return "INVALID_VALUE";
            case GL_OUT_OF_MEMORY:
                return "OUT_OF_MEMORY";
            case GL_INVALID_FRAMEBUFFER_OPERATION:
                return "INVALID_FRAMEBUFFER_OPERATION";
            default:
                return "Not Found";
        }
    }

}
