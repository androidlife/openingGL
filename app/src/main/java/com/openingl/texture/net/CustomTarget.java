package com.openingl.texture.net;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

/**
 * Created by laaptu on 12/15/17.
 */

public class CustomTarget<T> implements Target<T> {

    public int id;

    public CustomTarget(int id) {
        this.id = id;
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {

    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {

    }

    @Override
    public void onResourceReady(T resource, Transition<? super T> transition) {

    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {

    }

    @Override
    public void getSize(SizeReadyCallback cb) {
        cb.onSizeReady(256, 256);
    }

    @Override
    public void removeCallback(SizeReadyCallback cb) {

    }

    @Override
    public void setRequest(@Nullable Request request) {

    }

    @Nullable
    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }
}
