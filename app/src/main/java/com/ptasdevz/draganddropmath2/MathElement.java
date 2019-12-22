package com.ptasdevz.draganddropmath2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.ptasdevz.draganddropmath2.MainActivity.TAG;

public class MathElement {

    public static HashMap<String, MathElement> mathEleList = new HashMap<>();
    public static HashMap<String, Integer> MathElementsNameRes = new HashMap<>();
    public static String NUMBER_0 = "number0";
    public static String NUMBER_1 = "number1";
    public static String NUMBER_2 = "number2";
    public static String NUMBER_3 = "number3";
    public static String NUMBER_4 = "number4";
    public static String NUMBER_5 = "number5";
    public static String NUMBER_6 = "number6";
    public static String NUMBER_7 = "number7";
    public static String NUMBER_8 = "number8";
    public static String NUMBER_9 = "number9";
    public static String PLUS = "plus";
    public static String MINUS = "minus";
    public static String MULTIPLY = "multiply";
    public static String DIVIDE = "divide";
    public static String EQUAL = "equal";
    public static String TRASH = "trash";

    static {
        MathElementsNameRes.put(NUMBER_0, R.id.number0Img);
        MathElementsNameRes.put(NUMBER_1, R.id.number1Img);
        MathElementsNameRes.put(NUMBER_2, R.id.number2Img);
        MathElementsNameRes.put(NUMBER_3, R.id.number3Img);
        MathElementsNameRes.put(NUMBER_4, R.id.number4Img);
        MathElementsNameRes.put(NUMBER_5, R.id.number5Img);
        MathElementsNameRes.put(NUMBER_6, R.id.number6Img);
        MathElementsNameRes.put(NUMBER_7, R.id.number7Img);
        MathElementsNameRes.put(NUMBER_8, R.id.number8Img);
        MathElementsNameRes.put(NUMBER_9, R.id.number9Img);
        MathElementsNameRes.put(MINUS, R.id.minusImg);
        MathElementsNameRes.put(PLUS, R.id.plusImg);
        MathElementsNameRes.put(EQUAL, R.id.equalImg);
        MathElementsNameRes.put(MULTIPLY, R.id.multiplyImg);
        MathElementsNameRes.put(DIVIDE, R.id.divideImg);
    }

    private static final int DOUBLE_CLICK_TIME = 250; // double click timer

    private ImageView eleImg;
    private ConstraintLayout eleImgLayout;
    private SparseArray<MathElement> neighbouringMathEleList;
    private ElementPos lastPos;
    private ElementPos currPos;
    private long id;
    private boolean waitDouble = true;
    private float lastPtrPosX;
    private float lastPtrPosY;
    private float posX;
    private float posY;
    private String name;
    private float initialElePosX;
    private float initialElePosY;
    private float viewPosX;
    private float viewPosY;
    private Context context;

    public MathElement(final Context context, ImageView eleImageView,
                       String eleName, int parentId,
                       ConstraintLayout layout, boolean isCopy) {

        Drawable eleImgDrawable = eleImageView.getDrawable();
        float srcImgWidth = eleImgDrawable.getIntrinsicWidth();
        float srcImgHeight = eleImgDrawable.getIntrinsicHeight();
        float elePosX = eleImageView.getX();
        float elePosY = eleImageView.getY();
        eleImg = eleImageView;
        setupElement(context, eleImgDrawable, elePosX, elePosY, srcImgWidth, srcImgHeight, eleName,
                parentId, layout, isCopy);
    }

    @SuppressLint("ClickableViewAccessibility")
    public MathElement(final Context context, Drawable eleImgDrawable, float elePosX, float elePosY,
                       float srcImgWidth, float srcImgHeight, String eleName, int parentId,
                       ConstraintLayout layout, boolean isCopy) {
        setupElement(context, eleImgDrawable, elePosX, elePosY, srcImgWidth, srcImgHeight, eleName,
                parentId, layout, isCopy);
    }

    public ImageView getEleImg() {
        return eleImg;
    }

    public ConstraintLayout getEleImgLayout() {
        return eleImgLayout;
    }

    public MathElement dropElement(ConstraintLayout layout) {
        float x = this.getViewPosX();
        float y = this.getViewPosY();

        // Gets the text data from the item.
        String name = this.getName();
        ImageView eleImg;
        MathElement mathEleCopy;

        if (!name.contains("COPY")) { //drop copy

            mathEleCopy = getMathEleCopy(x, y, layout.getId(), layout);
            eleImg = mathEleCopy.getEleImg();
            layout.addView(eleImg);
            positionInLayout(mathEleCopy, x, y, false);
            return mathEleCopy;
        }

        return null;
    }

    public void positionInLayout(MathElement mathEle, float dropPosX, float dropPosY, boolean isReverted) {

        ConstraintLayout layout = mathEle.getEleImgLayout();
        ImageView dropEleImg = mathEle.getEleImg();

        //update last dropped position of element
        ElementPos lastPos = mathEle.getLastPos();
        ElementPos currPos = mathEle.getCurrPos();

        lastPos.setLeft(currPos.getLeft());
        lastPos.setTop(currPos.getTop());
        lastPos.setRight(currPos.getRight());
        lastPos.setBottom(currPos.getBottom());

        currPos.setLeft(dropPosX);
        currPos.setTop(dropPosY);
        currPos.setRight(dropPosX + dropEleImg.getWidth());
        currPos.setBottom(dropPosY + dropEleImg.getHeight());

        if (isReverted) {

            lastPos.setLeft(currPos.getLeft());
            lastPos.setTop(currPos.getTop());
            lastPos.setRight(currPos.getRight());
            lastPos.setBottom(currPos.getBottom());

        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM, 0);

        //adjust element within the constraint layout considering a half width and height
        //adjustment so that element will be centered on drop point.
        int eleImgWidth = dropEleImg.getWidth();
        int eleImgHeight = dropEleImg.getHeight();
        int eleImgHalfHeight = eleImgHeight / 2;
        int eleImgHalfWidth = eleImgWidth / 2;
        int layoutConstraintWidth = layout.getWidth() - dropEleImg.getWidth();
        int layoutConstraintHeight = layout.getHeight() - dropEleImg.getHeight();

        float horizontalBias = (dropPosX - eleImgHalfWidth) / layoutConstraintWidth;
        if (horizontalBias < 0) horizontalBias = 0;
        else if (horizontalBias > 1) horizontalBias = 1;
        constraintSet.setHorizontalBias(dropEleImg.getId(), horizontalBias);
        float verticalBias = (dropPosY - eleImgHalfHeight) / layoutConstraintHeight;
        if (verticalBias < 0) verticalBias = 0;
        else if (verticalBias > 1) verticalBias = 1;
        constraintSet.setVerticalBias(dropEleImg.getId(), verticalBias);
        constraintSet.applyTo(layout);

        //offset drop element image coordinates by half the width  and height to compensate for
        // half width and height adjustment which takes place above to center element on the point
        // on which it drops.
        dropEleImg.setLeft(dropEleImg.getLeft() - eleImgHalfWidth);
        dropEleImg.setTop(dropEleImg.getTop() - eleImgHalfHeight);
        dropEleImg.setRight(dropEleImg.getRight() - eleImgHalfWidth);
        dropEleImg.setBottom(dropEleImg.getBottom() - eleImgHalfHeight);
    }

    public void positionViewInCLayout(float dx, float dy, View view, ConstraintLayout layout) {

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.connect(view.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.connect(view.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(view.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.connect(view.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM, 0);

        /*
        calculate vertical and horizontal bias to position element
         */
        int layoutConstraintWidth = layout.getWidth() - view.getWidth();
        int layoutConstraintHeight = layout.getHeight() - view.getHeight();

        float horizontalBias = (dx + view.getX()) / layoutConstraintWidth;

        //place view horizontal bias at the minimum or maximum of the constraint
        if (horizontalBias < 0) {
            horizontalBias = 0;
        } else if (horizontalBias > 1) {
            horizontalBias = 1;
        }
        constraintSet.setHorizontalBias(view.getId(), horizontalBias);

        float verticalBias = (dy + view.getY()) / layoutConstraintHeight;
        //place view vertical bias at the minimum or maximum of the constraint
        if (verticalBias < 0) {
            verticalBias = 0;
        } else if (verticalBias > 1) {
            verticalBias = 1;
        }
        constraintSet.setVerticalBias(view.getId(), verticalBias);

        constraintSet.applyTo(layout);
    }

    public static HashMap<String, MathElement> getMathEleList() {
        return mathEleList;
    }

    public void resetMathElePosition() {
        float x = this.getInitialElePosX() - this.getEleImg().getX();
        float y = this.getInitialElePosY() - this.getEleImg().getY();
        positionViewInCLayout(x, y, this.eleImg, (ConstraintLayout) this.getEleImg().getParent());
    }

    /**
     * Re-positions this math element from freely positioned location by user to a fixed appropriate
     * position determined by the system.
     */
    public void repositionElement() {
        ImageView eleImg = this.getEleImg();
        MathElement neighbouringMathEle = getClosestNeighbouringMathEle();

        if (neighbouringMathEle != null) {

            Rect neighbouringMathEleRect = new Rect();
            neighbouringMathEleRect.top = neighbouringMathEle.getEleImg().getTop();
            neighbouringMathEleRect.left = neighbouringMathEle.getEleImg().getLeft();
            neighbouringMathEleRect.bottom = neighbouringMathEle.getEleImg().getBottom();
            neighbouringMathEleRect.right = neighbouringMathEle.getEleImg().getRight();

            Rect thisRect = new Rect();
            thisRect.top = eleImg.getTop();
            thisRect.left = eleImg.getLeft();
            thisRect.bottom = eleImg.getBottom();
            thisRect.right = eleImg.getRight();

            if (thisRect.intersect(neighbouringMathEleRect)) {

                //remove
                String neighbouringMathEleName = neighbouringMathEle.getName();
                String mathElementName = this.getName();

                //removes the element if intersects with trash
                if (neighbouringMathEleName.contains(MathElement.TRASH)) {
                    removeMathElement();
                } else if (mathElementName.contains(MathElement.TRASH)) {
                    neighbouringMathEle.removeMathElement();
                }

                //place element in a fix position: left, top, right or bottom
                else {
                    //drop rect intersected sides with stationary rect are updated to
                    // those of stationary rect
                    int intersectHeight = thisRect.bottom - thisRect.top;
                    int intersectWidth = thisRect.right - thisRect.left;

                    //all sides of neighbouringMathEle rect is intersected. cannot be placed
                    if (thisRect.left == neighbouringMathEleRect.left
                            && thisRect.top == neighbouringMathEleRect.top
                            && thisRect.right == neighbouringMathEleRect.right
                            && thisRect.bottom == neighbouringMathEleRect.bottom) {
                    }

                    //top-left side of neighbouringMathEle rect is intersected.
                    else if (thisRect.left == neighbouringMathEleRect.left
                            && thisRect.top == neighbouringMathEleRect.top) {
                        //place at top
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.TOP);
                        }
                        //place at left
                        else placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.LEFT);
                    }

                    //bottom-left side of neighbouringMathEle rect is intersected
                    else if (thisRect.left == neighbouringMathEleRect.left
                            && thisRect.bottom == neighbouringMathEleRect.bottom) {

                        //place at bottom
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.BOTTOM);
                        }
                        //place at left
                        else placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.LEFT);
                    }

                    //top-right side of stationary rect is intersected
                    else if (thisRect.right == neighbouringMathEleRect.right
                            && thisRect.top == neighbouringMathEleRect.top) {

                        //place at top
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.TOP);
                        }
                        //place at right
                        else placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.RIGHT);
                    }

                    //bottom-right side of neighbouringMathEle rect is intersected.
                    else if (thisRect.right == neighbouringMathEleRect.right
                            && thisRect.bottom == neighbouringMathEleRect.bottom) {

                        //place at bottom
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.BOTTOM);
                        }
                        // place at right
                        else placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.RIGHT);
                    }

                    //place in next available position starting from left
                    else placeElement(eleImg, neighbouringMathEle, neighbouringMathEleRect);
                }
            }
        }
    }

    public MathElement getFocusedMathEle(int pos) {
        try {
            return neighbouringMathEleList.get(pos);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public SparseArray<MathElement> getNeighbouringMathEleList() {
        return neighbouringMathEleList;
    }

    public ElementPos getLastPos() {
        return lastPos;
    }

    public ElementPos getCurrPos() {
        return currPos;
    }

    public float getLastPtrPosX() {
        return lastPtrPosX;
    }

    public void setLastPtrPosX(float lastPtrPosX) {
        this.lastPtrPosX = lastPtrPosX;
    }

    public float getLastPtrPosY() {
        return lastPtrPosY;
    }

    public void setLastPtrPosY(float lastPtrPosY) {
        this.lastPtrPosY = lastPtrPosY;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public String getName() {
        return name;
    }

    public float getInitialElePosX() {
        return initialElePosX;
    }

    public void setInitialElePosX(float initialElePosX) {
        this.initialElePosX = initialElePosX;
    }

    public float getInitialElePosY() {
        return initialElePosY;
    }

    public void setInitialElePosY(float initialElePosY) {
        this.initialElePosY = initialElePosY;
    }

    public float getViewPosX() {
        return viewPosX;
    }

    public void setViewPosX(float viewPosX) {
        this.viewPosX = viewPosX;
    }

    public float getViewPosY() {
        return viewPosY;
    }

    public void setViewPosY(float viewPosY) {
        this.viewPosY = viewPosY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof MathElement) {
            MathElement mathElement = (MathElement) obj;
            return mathElement.hashCode() == this.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    //================================Helpers=============================================
    @SuppressLint("ClickableViewAccessibility")
    private void setupElement(Context context, Drawable eleImgDrawable, float elePosX, float elePosY,
                              float srcImgWidth, float srcImgHeight, String eleName, int parentId,
                              ConstraintLayout layout, boolean isCopy) {
        this.context = context;
        if (isCopy) {
            eleName = eleName + "_COPY_" + parentId + "_" + getUniqueId();
            eleImg = new ImageView(this.context);

        }

        eleImg.setBackground(context.getDrawable(R.drawable.custom_border));
        eleImg.setImageDrawable(eleImgDrawable);
        eleImg.setMaxHeight((int) srcImgHeight);
        eleImg.setMaxWidth((int) srcImgWidth);
        this.name = eleName;
        if (eleName.equalsIgnoreCase(TRASH)) {
            eleImg.setOnLongClickListener(view -> true);
            eleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    if (waitDouble == true) {

                        waitDouble = false;
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    sleep(DOUBLE_CLICK_TIME);
                                    if (waitDouble == false) {
                                        waitDouble = true;
                                        singleClick(view.getContext());
                                        Toast.makeText(view.getContext(), "drag item to bin or double click to clear worksheet", Toast.LENGTH_SHORT).show();

                                    }
                                } catch (Exception e) {

                                }
                            }
                        };
                        thread.start();

                    } else {
                        waitDouble = true;
                        doubleClick();
                    }
                }

                public void singleClick(Context c) {
                    Log.d(TAG, "Single click");

                }

                private void doubleClick() {
                    Log.d(TAG, "Double click");
                    Iterator iterator = getMathEleList().entrySet().iterator();
                    while (iterator.hasNext()) {
                        HashMap.Entry<String, MathElement>
                                element = (HashMap.Entry<String, MathElement>) iterator.next();
                        String elementKey = element.getKey();
                        if (!elementKey.contains(TRASH)) {
                            MathElement mathElement = element.getValue();
                            MathElement.this.getEleImgLayout().removeView(mathElement.getEleImg());
                            iterator.remove();
                        }
                    }
                }
            });
        }
        eleImg.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    MainActivity.setupActionDownOptions(this, view, motionEvent);
                }
                break;
                case MotionEvent.ACTION_MOVE: {
                    MainActivity.setupActionMoveOptions(this, view, motionEvent);
                }
                break;
                case MotionEvent.ACTION_UP: {
                    resetFocusedMathEleList();
                    repositionElement();
                    learnNeighbouringElements();
                    view.performClick(); //enable click functions as well on element
                    Log.d(TAG, "touch is released.");
                }
                break;
            }
            return true;
        });

        eleImg.setLeft((int) elePosX);
        eleImg.setTop((int) elePosY);
        float elePosX1 = elePosX + srcImgWidth;
        eleImg.setRight((int) elePosX1);
        float elePosY1 = elePosY + srcImgHeight;
        eleImg.setBottom((int) elePosY1);
        eleImg.setId((int) getUniqueId());
        eleImgLayout = layout;
        neighbouringMathEleList = new SparseArray<>();
        id = getUniqueId();
//        currPos = new ElementPos(elePosX,elePosY,elePosX1,elePosY1);
        currPos = new ElementPos();
        lastPos = new ElementPos();
    }

    private MathElement getMathEleCopy(float x, float y, int parentId, ConstraintLayout layout) {

        Drawable drawable = eleImg.getDrawable();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        MathElement mathElement = MathElementFactory.getNewInstance(context, drawable, x, y,
                (float) width, (float) height, this.name, parentId, layout, true);
        mathEleList.put(mathElement.getName(), mathElement);
        return mathElement;
    }

    private void removeMathElement() {
        this.getEleImgLayout().removeView(this.getEleImg());
        mathEleList.remove(this.name);
    }

    private void resetFocusedMathEleList() {
        int size = neighbouringMathEleList.size();

        for (int i = 0; i < size; i++) {
            MathElement mathElement = this.neighbouringMathEleList.valueAt(i);
            if (mathElement != null) {
                SparseArray<MathElement> focusedMathEleList = mathElement.getNeighbouringMathEleList();
                int key = focusedMathEleList.keyAt(focusedMathEleList.indexOfValue(this));
                focusedMathEleList.remove(key);
            }
        }

        this.neighbouringMathEleList = new SparseArray<>();
    }

    private void placeElement(ImageView eleImg, MathElement focusedEle,
                              Rect stationaryRect) {
        /**
         * Figure out where to place element
         */
        SparseArray<MathElement> focusedMathEleList = focusedEle.getNeighbouringMathEleList();
        boolean isPlaced = false;
        ArrayList<Integer> positions = Constant.PLACEMENT_POSITIONS;
        for (int i = 0; i < positions.size(); i++) {
            int pos = positions.get(i);
            boolean isPosFilled = false;
            for (int j = i; j < focusedMathEleList.size(); j++) {
                int storedPos = focusedMathEleList.keyAt(j);
                if (pos == storedPos) {
                    isPosFilled = true;
                    break;
                }
            }
            if (!isPosFilled) {
                placeWherePossible(eleImg, focusedEle, stationaryRect, pos);
                isPlaced = true;
                break;
            }
        }
        if (!isPlaced) {
            //revert to last position
            ElementPos lastPos = this.getLastPos();
            if (lastPos.getLeft() != 0) {
                //Toast.makeText(this, "element reverted", Toast.LENGTH_SHORT).show();
                this.positionInLayout(this, lastPos.getLeft(), lastPos.getTop(), true);
            } else {
                Toast.makeText(context, "Element cannot be placed.", Toast.LENGTH_SHORT).show();
                removeMathElement();
            }
        }
    }

    private void placeWherePossible(ImageView eleImg, MathElement focusedEle
            , Rect focusedEleRect, int preferredPlacementPos) {

        switch (preferredPlacementPos) {

            case Constant.LEFT: {
                if (focusedEle.getFocusedMathEle(Constant.LEFT) == null) placeToLeft(
                        focusedEle, focusedEleRect, false);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            case Constant.TOP: {
                if (focusedEle.getFocusedMathEle(Constant.TOP) == null) placeToTop(
                        focusedEle, focusedEleRect, false);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            case Constant.RIGHT: {
                if (focusedEle.getFocusedMathEle(Constant.RIGHT) == null) placeToRight(
                        focusedEle, focusedEleRect, false);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            case Constant.BOTTOM: {
                if (focusedEle.getFocusedMathEle(Constant.BOTTOM) == null)
                    placeToBottom(
                            focusedEle, focusedEleRect, false);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            default: {

            }
        }

    }

    private void placeToRight(MathElement focusedEle, Rect focusedEleRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addNeighbouringMathEle(Constant.LEFT, focusedEle);
        }

        int width = focusedEle.getEleImg().getWidth();
        int xloc = focusedEleRect.left + width + width / 4;
        int yLoc = focusedEleRect.top + focusedEle.getEleImg().getHeight() / 2;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private void placeToBottom(MathElement focusedEle, Rect focusedEleRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addNeighbouringMathEle(Constant.TOP, focusedEle);
        }
        int xloc = focusedEleRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = focusedEleRect.bottom + focusedEle.getEleImg().getHeight() / 4;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private void placeToTop(MathElement focusedEle, Rect focusedEleRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addNeighbouringMathEle(Constant.BOTTOM, focusedEle);
        }
        int xloc = focusedEleRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = focusedEleRect.top - focusedEle.getEleImg().getHeight() / 4;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private void placeToLeft(MathElement focusedEle, Rect focusedEleRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addNeighbouringMathEle(Constant.RIGHT, focusedEle);
        }
        int width = focusedEle.getEleImg().getWidth();
        int xloc = focusedEleRect.left - width / 4;
        int yLoc = focusedEleRect.top + focusedEle.getEleImg().getHeight() / 2;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private MathElement getClosestNeighbouringMathEle() {

        double shortestDis = Integer.MAX_VALUE;
        MathElement focusedEle = null;

        int dropEleLeft = this.getEleImg().getLeft();
        int dropEleTop = this.getEleImg().getTop();

        for (HashMap.Entry<String, MathElement> entry : mathEleList.entrySet()) {
            MathElement focusedMathEle = entry.getValue();
            if (!focusedMathEle.equals(this)
                    && (focusedMathEle.getName().contains("COPY")
                    || focusedMathEle.getName().contains(MathElement.TRASH))) {
                ImageView focusedMathEleEleImg = focusedMathEle.getEleImg();

                int stationaryEleLeft = focusedMathEleEleImg.getLeft();
                int stationaryEleTop = focusedMathEleEleImg.getTop();

                int xVal = dropEleLeft - stationaryEleLeft;
                int yVal = dropEleTop - stationaryEleTop;

                double distance = Math.sqrt((xVal * xVal) + (yVal * yVal));
                if (focusedEle != null) Log.d(TAG, "getClosestNeighbouringMathEle: "
                        + this.getName() + " is : " + distance + "from ele: "
                        + focusedEle.getName());

                if (distance < shortestDis) {

                    shortestDis = distance;
                    focusedEle = focusedMathEle;
                }
            }
        }
        return focusedEle;
    }

    /**
     * Adds a math element as a neighbour of this math element.
     * @param pos the neighbour's position
     * @param neighbouringMathEle the neighbouring math element
     */
    private void addNeighbouringMathEle(int pos, MathElement neighbouringMathEle) {
        SparseArray<MathElement> neighbouringMathEleList = neighbouringMathEle.getNeighbouringMathEleList();

        //check to see if both math elements are currently neighbouring elements of each other. if so remove.
        int index = neighbouringMathEleList.indexOfValue(this);
        if (index >= 0) neighbouringMathEleList.removeAt(index);
        int index1 = this.neighbouringMathEleList.indexOfValue(neighbouringMathEle);
        if (index1 >= 0) this.neighbouringMathEleList.removeAt(index1);

        //Add each element as a focus element of each other in opposite positions
        neighbouringMathEleList.put(getOppositeElePos(pos), this);
        this.neighbouringMathEleList.put(pos, neighbouringMathEle);

    }

    public void learnNeighbouringElements() {

        //Delay execution until repositioning finishes
        new Thread(() -> {
            SystemClock.sleep(500);
            for (HashMap.Entry<String, MathElement> entry : mathEleList.entrySet()) {
                MathElement mathElement = entry.getValue();
                if (!mathElement.equals(this)
                        && (mathElement.getName().contains("COPY"))) {

                    Rect mathEleRect = new Rect();
                    mathEleRect.top = mathElement.getEleImg().getTop();
                    mathEleRect.left = mathElement.getEleImg().getLeft();
                    mathEleRect.bottom = mathElement.getEleImg().getBottom();
                    mathEleRect.right = mathElement.getEleImg().getRight();

                    Rect thisRect = new Rect();
                    thisRect.top = eleImg.getTop();
                    thisRect.left = eleImg.getLeft();
                    thisRect.bottom = eleImg.getBottom();
                    thisRect.right = eleImg.getRight();

                    if (thisRect.intersect(mathEleRect)) {

                        //right element is being considered
                        if (thisRect.left == mathEleRect.left
                                && thisRect.top == mathEleRect.top
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.RIGHT,mathElement);
                        }

                        //left element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.top == mathEleRect.top
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.LEFT,mathElement);
                        }

                        //top element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.left == mathEleRect.left
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.TOP,mathElement);
                        }

                        //top-left element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.TOP_LEFT,mathElement);
                        }

                        //top-right element is being considered
                        else if (thisRect.left == mathEleRect.left
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.TOP_RIGHT,mathElement);
                        }

                        //bottom element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.left == mathEleRect.left
                                && thisRect.top == mathEleRect.top) {
                            addNeighbouringMathEle(Constant.BOTTOM,mathElement);
                        }

                        //bottom-left element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.top == mathEleRect.top) {
                            addNeighbouringMathEle(Constant.BOTTOM_LEFT,mathElement);

                        }
                        //bottom-right element is being considered
                        else if (thisRect.left == mathEleRect.left
                                && thisRect.top == mathEleRect.top) {
                            addNeighbouringMathEle(Constant.BOTTOM_RIGHT,mathElement);
                        }
                    }
                }
            }
        }).start();
    }

    private int getOppositeElePos(int pos) {
        if (pos <= 4) return pos + 4;
        return pos - 4;
    }

    private long getUniqueId() {
        try {
            Thread.sleep(0, 10);
        } catch (InterruptedException e) {
        }
        return new Timestamp(System.nanoTime()).getTime();
    }
}
