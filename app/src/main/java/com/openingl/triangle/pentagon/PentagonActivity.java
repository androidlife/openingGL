package com.openingl.triangle.pentagon;

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

public class PentagonActivity extends AppCompatActivity {
    @BindView(R.id.surface_view)
    GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new PentagonRenderer(this));
    }
}
