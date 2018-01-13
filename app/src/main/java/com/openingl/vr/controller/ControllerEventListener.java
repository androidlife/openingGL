package com.openingl.vr.controller;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

/**
 * Created by laaptu on 1/13/18.
 */

public class ControllerEventListener extends Controller.EventListener implements
        ControllerManager.EventListener {
    @Override
    public void onApiStatusChanged(int i) {

    }

    @Override
    public void onRecentered() {

    }
}
