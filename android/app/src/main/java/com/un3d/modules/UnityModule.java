package com.un3d.modules;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.un3d.views.UnityEventListener;
import com.un3d.views.UnityUtils;
import com.unity3d.player.UnityPlayerActivity;

public class UnityModule extends ReactContextBaseJavaModule implements UnityEventListener {

    private static ReactApplicationContext reactContext;


    public UnityModule(@Nullable ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext=reactContext;
        UnityUtils.addUnityEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return "UnityNativeModule";
    }



    @ReactMethod
    public void showUnity(){
        Intent intent = new Intent(this.reactContext.getCurrentActivity(), UnityPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        reactContext.getCurrentActivity().startActivityForResult(intent, 1);
    }


    @ReactMethod
    public void isReady(Promise promise) {
        promise.resolve(UnityUtils.isUnityReady());
    }

    @ReactMethod
    public void createUnity(final Promise promise) {
        UnityUtils.createPlayer(getCurrentActivity(), new UnityUtils.CreateCallback() {
            @Override
            public void onReady() {
                promise.resolve(true);
            }
        });
    }

    @ReactMethod
    public void postMessage(String gameObject, String methodName, String message) {
        UnityUtils.postMessage(gameObject, methodName, message);
    }

    @ReactMethod
    public void pause() {
        UnityUtils.pause();
    }

    @ReactMethod
    public void resume() {
        UnityUtils.resume();
    }

    @Override
    public void onMessage(String message) {
        ReactContext context = getReactApplicationContext();
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onUnityMessage", message);
    }




}
