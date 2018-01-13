package com.openingl.texture;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


import com.openinggl.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laaptu on 12/12/17.
 */

public class SimpleTextureActivity extends AppCompatActivity {
    @BindView(R.id.gl_surface_view)
    GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_texture);
        ButterKnife.bind(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new SimpleTextureRenderer(this));
    }
}
