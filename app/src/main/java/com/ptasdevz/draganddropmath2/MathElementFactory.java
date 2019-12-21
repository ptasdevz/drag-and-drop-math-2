package com.ptasdevz.draganddropmath2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MathElementFactory {

    private MathElementFactory(){}

    public static MathElement getNewInstance(Context context, Drawable drawable,
                                             Float elePosX, Float elePosY, Float srcImgWidth,
                                             Float srcImgHeight, String eleName, int parentId,
                                             ConstraintLayout constraintLayout, boolean isCopy){
        return  new MathElement(context,drawable,elePosX,elePosY,srcImgWidth,srcImgHeight,
                eleName,parentId,constraintLayout, isCopy);
    }

    public static MathElement getNewInstance(Context context, ImageView imageView,
                                             String eleName, int parentId,
                                             ConstraintLayout constraintLayout, boolean isCopy){
        return  new MathElement(context,imageView, eleName,parentId,
                constraintLayout, isCopy);
    }
}