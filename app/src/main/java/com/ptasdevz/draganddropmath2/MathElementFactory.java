package com.ptasdevz.draganddropmath2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import java.util.HashMap;

/**
 * Creates a specific instance of a MathElement object.
 */
public class MathElementFactory {

    private MathElementFactory(){}

    public static MathElement getNewInstance(Context context, Drawable drawable,
                                             Float elePosX, Float elePosY, Float srcImgWidth,
                                             Float srcImgHeight, String eleName, int parentId,
                                             MathElementConstraintLayout workspaceLayout,
                                             MathElementConstraintLayout mainLayout,
                                             boolean isCopy, @Nullable HashMap<String, Object> remoteData) {
        return  new MathElement(context,drawable,elePosX,elePosY,srcImgWidth,srcImgHeight,
                eleName,parentId, workspaceLayout, mainLayout,isCopy, remoteData);
    }

    public static MathElement getNewInstance(Context context, ImageView imageView,
                                             String eleName, int parentId,
                                             MathElementConstraintLayout workspaceLayout,
                                             MathElementConstraintLayout mainLayout,
                                             boolean isCopy, @Nullable HashMap<String, Object> mathEleEvent) {

        return  new MathElement(context,imageView, eleName,parentId,
                workspaceLayout, mainLayout,isCopy, mathEleEvent);
    }
}
