package com.openingl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.openinggl.R;
import com.openingl.gallery.Utils;
import com.openingl.utils.GLHelper;
import com.openingl.vr.controller.SimpleControllerActivity;
import com.openingl.vr.rectangle.ControllerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        goToOtherActivity();
        //glideTest();
        //loadBitmap();
//        circleTest();
    }

    private void circleTest() {
    }

    private void loadBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 256;
        options.outWidth = 512;
        options.inScaled = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading, options);
        Bitmap textBitmap = Utils.getBitmapWithTitle(bitmap, "This is just a test and I am loving it and doing more and more");
        imageView.setImageBitmap(textBitmap);
    }


    private Bitmap loadBitmap(String url) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.override(300, 300);
        requestOptions.centerInside();

        FutureTarget<Bitmap> futureTarget = Glide.with(this).asBitmap().load(url).apply(requestOptions).submit();
        futureTarget.cancel(true);
        try {
            return Glide.with(this).asBitmap().load(url).apply(requestOptions).submit().get();
        } catch (Exception e) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.pirate);
        }
    }

    private void goToOtherActivity() {
        startActivity(new Intent(this,SimpleControllerActivity.class));
        finish();
    }

    private void someMatrixTest() {
        float[] firstMatrix = new float[16];
        Matrix.setIdentityM(firstMatrix, 0);
        printMatrix(firstMatrix);
        Matrix.scaleM(firstMatrix, 0, 2.0f, 1.0f, 1.0f);
        printMatrix(firstMatrix);
    }

    private void printMatrix(float[] matrix) {
        if (matrix.length != 16)
            return;
        Timber.d("Matrix-------------");
        for (int i = 0; i < 4; ++i) {
            int index = 4 * i - 1;
            index = 4 * (i + 1);
            Timber.d("[%.2f %.2f %.2f %.2f]", matrix[++index],
                    matrix[++index], matrix[++index], matrix[++index]);
        }
    }

    private void matrixValues() {
        float[] model = new float[16];
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, 10, 0, 0);
        GLHelper.printMatrixByRow(model, "Result");
    }


}
