package com.openingl.utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import timber.log.Timber;

/**
 * Created by laaptu on 12/3/17.
 */

public class TextReader {
    private static final String SHADER = "Shader", VERTEX = "Vertex", FRAGMENT = "Fragment";
    private static final int INDEX_VERTEX = 0, INDEX_FRAGMENT = 1;

    public static String[] readShaderFromRawFile(Context context, int resourceId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            return parseFromInputStream(inputStream);
        } catch (Exception e) {
            Timber.e("readShaderFromRawFile failed: %s", e.getMessage());
        }
        return null;
    }

    private static String[] parseFromInputStream(InputStream inputStream) throws Exception {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder[] stringBuilders = initStringBuilders();
        String line;
        int index = -1;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains(SHADER)) {
                if (line.contains(VERTEX))
                    index = INDEX_VERTEX;
                else if (line.contains(FRAGMENT))
                    index = INDEX_FRAGMENT;
            } else if (index >= 0) {
                stringBuilders[index].append(line).append("\n");
            }
        }
        String[] strings = new String[2];
        String vertexCode = stringBuilders[INDEX_VERTEX].toString().trim();
        String fragmentCode = stringBuilders[INDEX_FRAGMENT].toString().trim();
        boolean isValid = true;
        if (TextUtils.isEmpty(vertexCode)) {
            Timber.e("Error parsing vertex code");
            isValid = false;
        }
        if (TextUtils.isEmpty(fragmentCode)) {
            Timber.e("Error parsing fragment code");
            isValid = false;
        }
        if (!isValid)
            return null;

        strings[INDEX_VERTEX] = stringBuilders[INDEX_VERTEX].toString();
        strings[INDEX_FRAGMENT] = stringBuilders[INDEX_FRAGMENT].toString();
        Timber.d("Vertex code:\n%s\n---------------------", vertexCode);
        Timber.d("Fragment code:\n%s\n--------------------", fragmentCode);
        return strings;
    }

    public static StringBuilder[] initStringBuilders() {
        StringBuilder[] stringBuilders = new StringBuilder[2];
        stringBuilders[INDEX_VERTEX] = new StringBuilder();
        stringBuilders[INDEX_FRAGMENT] = new StringBuilder();
        return stringBuilders;
    }
}
