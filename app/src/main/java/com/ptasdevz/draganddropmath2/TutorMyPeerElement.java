package com.ptasdevz.draganddropmath2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class TutorMyPeerElement {

    public String appId;

    public static String toJsonString(TutorMyPeerElement tutorMyPeerElement) {
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(TutorMyPeerElement.class, new TutorMyPeerEleModelAbstractAdapter());
        Gson gson = gsonBilder.create();
        return gson.toJson(tutorMyPeerElement);
    }

    public static TutorMyPeerElement fromJsonString(String tutotMyPeerEleJsonStr){
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(TutorMyPeerElement.class, new TutorMyPeerEleModelAbstractAdapter());
        Gson gson = gsonBilder.create();
        return gson.fromJson(tutotMyPeerEleJsonStr,MathElementRemote.class);
    }
}
