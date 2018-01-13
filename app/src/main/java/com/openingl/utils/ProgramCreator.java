package com.openingl.utils;

import android.content.Context;
import android.text.TextUtils;

import timber.log.Timber;

import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glValidateProgram;

/**
 * Created by laaptu on 12/3/17.
 */

public class ProgramCreator {


    public static int createProgram(Context context, int shaderCodeRawId) {
        String[] shaderCodes = TextReader.readShaderFromRawFile(context, shaderCodeRawId);
        if (shaderCodes == null || TextUtils.isEmpty(shaderCodes[0])
                || TextUtils.isEmpty(shaderCodes[1])) {
            Timber.e("Error getting shader codes from raw resource file");
            return GLError.OPEN_GL;
        }
        int shaderVertexId = ShaderCreator.createVertexShader(shaderCodes[0]);
        int shaderFragmentId = ShaderCreator.createFragmentShader(shaderCodes[1]);
        if (shaderVertexId == GLError.OPEN_GL || shaderFragmentId == GLError.OPEN_GL) {
            if (shaderVertexId == GLError.OPEN_GL)
                Timber.e("Error creating Vertex Shader");
            else
                Timber.e("Error creating fragment Shader");
            return GLError.OPEN_GL;
        }
        int programId = createProgram(shaderVertexId, shaderFragmentId);
        if (programId == GLError.OPEN_GL) {
            Timber.e("Error creating program");
            //return GLError.OPEN_GL;
            throw new RuntimeException("Error creating program");
        }
        return programId;
    }

    /**
     * Program creation steps
     * 1. Create Program : glCreateProgram
     * 2. attach shader to program, glAttachShader
     * 3. link program ( which is actually linking vertex and fragment shader):
     * glLinkProgram
     */
    public static int createProgram(int shaderVertexId, int shaderFragmentId) {
        //create program
        Timber.d("------createProgram with shaderVertexId=%d, shaderFragmentId=%d", shaderVertexId, shaderFragmentId);
        int programId = glCreateProgram();
        if (programId == GLError.OPEN_GL) {
            Timber.e("Unable to create new program");
            return GLError.OPEN_GL;
        }
        Timber.d("Successfully created program with id = %d", programId);
        //attach program with shader
        glAttachShader(programId, shaderVertexId);
        glAttachShader(programId, shaderFragmentId);
        //link program
        glLinkProgram(programId);
        //this step is not essential
        //but gives us what is the error that came during linking
        //the program with the shader id's
        return checkProgramLinkStatus(programId);
    }

    /**
     * Steps
     * validateProgram
     * glGetProgramiv( ...,status,)
     */
    private static int checkProgramLinkStatus(int programId) {
        glValidateProgram(programId);
        final int[] programLinkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, programLinkStatus, 0);
        if (programLinkStatus[0] == GLError.OPEN_GL) {
            String errorStatus = glGetProgramInfoLog(programId);
            Timber.e("Unable to link the shaders to program %d due to %s", programId, errorStatus);
            glDeleteProgram(programId);
            return GLError.OPEN_GL;
        }
        Timber.d("Successfully linked the shaders to program %d ", programId);
        return programId;
    }
}
