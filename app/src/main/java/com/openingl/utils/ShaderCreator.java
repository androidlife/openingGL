package com.openingl.utils;


import timber.log.Timber;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glShaderSource;

/**
 * Created by laaptu on 12/3/17.
 * In OpenGL, if value = 0 ( in most of the cases )
 * then it is some kind of error
 */

public class ShaderCreator {


    public static int createVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int createFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * Steps:
     * 1. Create Shader: glCreateShader
     * 2. Provide input to shader : glShaderSource
     * 3. Compile shader: glCompileShader
     */
    private static int compileShader(int type, String shaderCode) {
        String shaderType = getShaderType(type);
        Timber.d("--------Create %s----------\nfor code:\n%s\n", shaderType, shaderCode);
        //create shader
        int shaderId = glCreateShader(type);
        if (shaderId == GLError.OPEN_GL) {
            Timber.e("GLError creating %s", shaderType);
            return GLError.OPEN_GL;
        }
        Timber.d("Successfully created %s with shader id = %d", shaderType, shaderId);
        //provide input or source to shader
        glShaderSource(shaderId, shaderCode);
        //compile shader
        glCompileShader(shaderId);
        shaderId = checkShaderCompileStatus(shaderId);
        return shaderId;
    }

    /**
     * Specially for log purpose the method name ends with iv
     * here glGetShaderiv
     * and to get status, we need to pass an int[] or something where
     * the method will write the status of compilation
     * and if we need message of what that status say
     * we need
     * glGetShaderInfoLog(shaderId)
     */
    private static int checkShaderCompileStatus(int shaderId) {
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == GLError.OPEN_GL) {
            final String errorCause = glGetShaderInfoLog(shaderId);
            Timber.e("GLError compiling shader with id =%d, due to  = %s ", shaderId, errorCause);
            glDeleteShader(shaderId);
            return GLError.OPEN_GL;
        }
        Timber.d("Successfully compiled shader with id = %d", shaderId);
        return shaderId;
    }


    private static String getShaderType(int type) {
        switch (type) {
            case GL_FRAGMENT_SHADER:
                return "Fragment Shader";
            case GL_VERTEX_SHADER:
                return "Vertex Shader";
        }
        return null;
    }
}
