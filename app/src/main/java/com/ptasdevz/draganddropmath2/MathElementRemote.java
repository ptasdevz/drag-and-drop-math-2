package com.ptasdevz.draganddropmath2;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Facilitates the remote controlling of MathElement object by sending and receiving necessary
 * math element data.
 */
public class MathElementRemote extends TutorMyPeerElement {

    public static GsonBuilder gsonBilder;

    public String name;
    public int remoteAction;
    public float motionPositionX;
    public float motionPositionY;
    public int pointerIndex;
    public int deviceScreenDensityX;
    public int deviceScreenDensityY;
    public float parentWidth;
    public float parentHeight;

    public MathElementRemote(Context context) {
        deviceScreenDensityY = context.getResources().getDisplayMetrics().heightPixels;
        deviceScreenDensityX = context.getResources().getDisplayMetrics().widthPixels;
    }

    public int getDeviceScreenDensityX() {
        return deviceScreenDensityX;
    }

    public int getDeviceScreenDensityY() {
        return deviceScreenDensityY;
    }

    private int getNavigationBarHeight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }


    public static String toJsonString(TutorMyPeerElement tutorMyPeerElement) {
        Gson gson = getGson();
        return gson.toJson(tutorMyPeerElement);
    }

    public static MathElementRemote fromJsonString(String tutotMyPeerEleJsonStr){
        Gson gson = getGson();
        return gson.fromJson(tutotMyPeerEleJsonStr,MathElementRemote.class);
    }

    private static Gson getGson() {
        if (gsonBilder == null) {
            gsonBilder = new GsonBuilder();
            gsonBilder.registerTypeAdapter(TutorMyPeerElement.class,
                    new TutorMyPeerEleModelAbstractAdapter());
        }
        return gsonBilder.create();
    }

    @Override
    public String toString() {
        return "MathElementRemote{" +
                "name='" + name + '\'' +
                ", remoteAction=" + remoteAction +
                ", motionPositionX=" + motionPositionX +
                ", motionPositionY=" + motionPositionY +
                ", pointerIndex=" + pointerIndex +
                ", deviceScreenDensityX=" + deviceScreenDensityX +
                ", deviceScreenDensityY=" + deviceScreenDensityY +
                ", parentWidth=" + parentWidth +
                ", parentHeight=" + parentHeight +
                ", appId='" + appId + '\'' +
                '}';
    }
}
