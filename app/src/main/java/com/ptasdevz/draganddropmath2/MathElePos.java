package com.ptasdevz.draganddropmath2;

import androidx.annotation.Nullable;

public class MathElePos {
    public float lastPtrPosX;
    public float lastPtrPosY;
    public float posX;
    public float posY;
    public String name;
    public float initialElePosX;
    public float initialElePosY;
    public float viewPosX;
    public float viewPosY;

    public void reset (){
        this.lastPtrPosX =0;
        this.lastPtrPosY = 0;
        this.posX = 0;
        this.posY = 0;
        this.initialElePosX = 0;
        this.initialElePosY = 0;
    }



    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)return false;
        if (obj == this) return true;
        if (obj instanceof MathElePos){
            MathElePos mathElePos = (MathElePos) obj;
            return mathElePos.hashCode() == this.hashCode();
        }
        return false;
    }
}
