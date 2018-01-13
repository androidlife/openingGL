package com.openingl.camera.cube;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


import com.openinggl.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laaptu on 12/3/17.
 */

public class CubeActivity extends AppCompatActivity {
    @BindView(R.id.surface_view)
    GLSurfaceView glSurfaceView;
    private CubeRenderer cubeRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initView() {
        glSurfaceView.setEGLContextClientVersion(2);
        cubeRenderer = new CubeRenderer(this);
        glSurfaceView.setRenderer(cubeRenderer);
    }
}
