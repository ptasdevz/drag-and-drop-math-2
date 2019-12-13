package com.ptasdevz.draganddropmath2;

import androidx.annotation.Nullable;

public class MathEleLastTouch {
    public float x;
    public float y;
    public String name;

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)return false;
        if (obj == this) return true;
        if (obj instanceof MathEleLastTouch){
            MathEleLastTouch mathEleLastTouch = (MathEleLastTouch) obj;
            return mathEleLastTouch.hashCode() == this.hashCode();
        }
        return false;
    }
}
