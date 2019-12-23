package com.ptasdevz.draganddropmath2;

public class MathElementRemote extends TutorMyPeerElement {

    public MathELeRemoteLayout mathELeRemoteLayout = new MathELeRemoteLayout();
    public MathEleRemoteImage mathEleRemoteImage = new MathEleRemoteImage();

    public String name;
    public int remoteAction;
    public float changeOfPosX;
    public float changeOfPosY;

    @Override
    public String toString() {
        return "MathElementRemote{" +
                "mathELeRemoteLayout=" + mathELeRemoteLayout +
                ", mathEleRemoteImage=" + mathEleRemoteImage +
                ", name='" + name + '\'' +
                ", remoteAction=" + remoteAction +
                ", changeOfPosX=" + changeOfPosX +
                ", changeOfPosY=" + changeOfPosY +
                '}';
    }
}
