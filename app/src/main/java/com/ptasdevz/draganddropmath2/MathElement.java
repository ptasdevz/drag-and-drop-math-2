package com.ptasdevz.draganddropmath2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
    private ImageView eleImg;
    private ConstraintLayout eleImgLayout;
    private SparseArray<MathElement> focusedMathEleList;
    private ElementPos lastPos;
    private ElementPos currPos;
    private long id;
    private boolean waitDouble = true;
    private static final int DOUBLE_CLICK_TIME = 250; // double click timer
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


    public static HashMap<String, Integer> MathElementsNameRes = new HashMap<>();

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
        int layoutConstraintWidth = layout.getWidth() - dropEleImg.getWidth() + 1;
        int layoutConstraintHeight = layout.getHeight() - dropEleImg.getHeight() + 1;

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
        dropEleImg.setLeft(dropEleImg.getLeft() - eleImgHalfWidth - 1);
        dropEleImg.setTop(dropEleImg.getTop() - eleImgHalfHeight);
        dropEleImg.setRight(dropEleImg.getRight() - eleImgHalfWidth -1);
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
        int layoutConstraintWidth = layout.getWidth() - view.getWidth() + 1;
        int layoutConstraintHeight = layout.getHeight() - view.getHeight() + 1;

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
        MathElement focusedEle = getClosestStationaryCopiedMathElement();

        if (focusedEle != null) {

            Rect stationaryRect = new Rect();
            stationaryRect.top = focusedEle.getEleImg().getTop();
            stationaryRect.left = focusedEle.getEleImg().getLeft();
            stationaryRect.bottom = focusedEle.getEleImg().getBottom();
            stationaryRect.right = focusedEle.getEleImg().getRight();

            Rect dropRect = new Rect();
            dropRect.top = eleImg.getTop();
            dropRect.left = eleImg.getLeft();
            dropRect.bottom = eleImg.getBottom();
            dropRect.right = eleImg.getRight();

            if (dropRect.intersect(stationaryRect)) {

                //remove element focused element is trash can
                String focusedElementName = focusedEle.getName();
                String mathElementName = this.getName();

                //removes the element if intersects with trash
                if (focusedElementName.contains(MathElement.TRASH)) {
                    removeMathElement();
                } else if (mathElementName.contains(MathElement.TRASH)) {
                    removeMathElement();
                }

                //place element in a fix position: left, top, right or bottom
                else {
                    //drop rect intersected sides with stationary rect are updated to
                    // those of stationary rect
                    int intersectHeight = dropRect.bottom - dropRect.top;
                    int intersectWidth = dropRect.right - dropRect.left;

                    //all sides of stationary rect is intersected. cannot be placed
                    if (dropRect.left == stationaryRect.left
                            && dropRect.top == stationaryRect.top
                            && dropRect.right == stationaryRect.right
                            && dropRect.bottom == stationaryRect.bottom) {
                    }

                    //top-left side of stationary rect is intersected.
                    else if (dropRect.left == stationaryRect.left
                            && dropRect.top == stationaryRect.top) {
                        //place at top
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.TOP);
                        }
                        //place at left
                        else placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.LEFT);
                    }

                    //bottom-left side of stationary rect is intersected
                    else if (dropRect.left == stationaryRect.left
                            && dropRect.bottom == stationaryRect.bottom) {

                        //place at bottom
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.BOTTOM);
                        }
                        //place at left
                        else placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.LEFT);
                    }

                    //top-right side of stationary rect is intersected
                    else if (dropRect.right == stationaryRect.right
                            && dropRect.top == stationaryRect.top) {

                        //place at top
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.TOP);
                        }
                        //place at right
                        else placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.RIGHT);
                    }

                    //bottom-right side of stationary rect is intersected.
                    else if (dropRect.right == stationaryRect.right
                            && dropRect.bottom == stationaryRect.bottom) {

                        //place at bottom
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.BOTTOM);
                        }
                        // place at right
                        else placeWherePossible(eleImg, focusedEle, stationaryRect, Constant.RIGHT);
                    }

                    //place in next available position starting from left
                    else placeElement(eleImg, focusedEle, stationaryRect);
                }
            }
        }
    }

    public MathElement getFocusedMathEle(int pos) {
        try {
            return focusedMathEleList.get(pos);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public SparseArray<MathElement> getFocusedMathEleList() {
        return focusedMathEleList;
    }

    public void resetFocusedMathEleList() {
        focusedMathEleList = new SparseArray<>();
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
                    repositionElement();
                    view.performClick();
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
        focusedMathEleList = new SparseArray<>();
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

    private void placeElement(ImageView eleImg, MathElement focusedEle,
                              Rect stationaryRect) {
        /**
         * Figure out where to place element
         */
        SparseArray<MathElement> focusedMathEleList = focusedEle.getFocusedMathEleList();
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
                Toast.makeText(context, "element was removed", Toast.LENGTH_SHORT).show();
                removeMathElement();
            }
        }
    }

    private void placeWherePossible(ImageView eleImg, MathElement focusedEle
            , Rect stationaryRect, int preferredPlacementPos) {

        switch (preferredPlacementPos) {

            case Constant.LEFT: {
                if (focusedEle.getFocusedMathEle(Constant.LEFT) == null) placeToLeft(
                        focusedEle, stationaryRect, true);
                else placeElement(eleImg, focusedEle, stationaryRect);
            }
            break;
            case Constant.TOP: {
                if (focusedEle.getFocusedMathEle(Constant.TOP) == null) placeToTop(
                        focusedEle, stationaryRect, true);
                else placeElement(eleImg, focusedEle, stationaryRect);
            }
            break;
            case Constant.RIGHT: {
                if (focusedEle.getFocusedMathEle(Constant.RIGHT) == null) placeToRight(
                        focusedEle, stationaryRect, true);
                else placeElement(eleImg, focusedEle, stationaryRect);
            }
            break;
            case Constant.BOTTOM: {
                if (focusedEle.getFocusedMathEle(Constant.BOTTOM) == null)
                    placeToBottom(
                            focusedEle, stationaryRect, true);
                else placeElement(eleImg, focusedEle, stationaryRect);
            }
            break;
            default: {

            }
        }

    }

    private void placeToRight(MathElement focusedEle, Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addFocusedMathEle(Constant.LEFT, focusedEle);
        }

        int width = focusedEle.getEleImg().getWidth();
        int xloc = stationaryRect.left + width;
        int yLoc = stationaryRect.top + focusedEle.getEleImg().getHeight() / 2;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private void placeToBottom(MathElement focusedEle, Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addFocusedMathEle(Constant.TOP, focusedEle);
        }
        int xloc = stationaryRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = stationaryRect.bottom + focusedEle.getEleImg().getHeight() / 4;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private void placeToTop(MathElement focusedEle, Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addFocusedMathEle(Constant.BOTTOM, focusedEle);
        }
        int xloc = stationaryRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = stationaryRect.top - focusedEle.getEleImg().getHeight() / 4;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private void placeToLeft(MathElement focusedEle, Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            this.addFocusedMathEle(Constant.RIGHT, focusedEle);
        }
        int xloc = stationaryRect.left;
        int yLoc = stationaryRect.top + focusedEle.getEleImg().getHeight() / 2;
        this.positionInLayout(this, xloc, yLoc, false);
    }

    private MathElement getClosestStationaryCopiedMathElement() {

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
                if (focusedEle != null) Log.d(TAG, "getClosestStationaryCopiedMathElement: "
                        + this.getName() + " distance: " + distance + "from ele: "
                        + focusedEle.getName());

                if (distance < shortestDis) {

                    shortestDis = distance;
                    focusedEle = focusedMathEle;
                }
            }
        }
        return focusedEle;
    }

    private void addFocusedMathEle(int pos, MathElement focusElement) {
        SparseArray<MathElement> focusedMathEleList = focusElement.getFocusedMathEleList();

        //check to see if both math elements are currently focused elements of each other. if so remove.
        int index = focusedMathEleList.indexOfValue(this);
        if (index >= 0) focusedMathEleList.removeAt(index);
        int index1 = this.focusedMathEleList.indexOfValue(focusElement);
        if (index1 >= 0) this.focusedMathEleList.removeAt(index1);

        //Add each element as a focus element of each other in opposite positions
        focusedMathEleList.put(getOppositeElePos(pos), this);
        this.focusedMathEleList.put(pos, focusElement);

        twoStepCheck(focusElement, pos);
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

    private void twoStepCheck(MathElement focusElement, int pos) {
        Log.d(TAG, "twoStepCheck before: " + focusedMathEleList.toString());

        learnAdjacentElements(focusElement, pos, Constant.LEFT);
        learnAdjacentElements(focusElement, pos, Constant.TOP_LEFT);
        learnAdjacentElements(focusElement, pos, Constant.TOP);
        learnAdjacentElements(focusElement, pos, Constant.TOP_RIGHT);
        learnAdjacentElements(focusElement, pos, Constant.RIGHT);
        learnAdjacentElements(focusElement, pos, Constant.BOTTOM_RIGHT);
        learnAdjacentElements(focusElement, pos, Constant.BOTTOM);
        learnAdjacentElements(focusElement, pos, Constant.BOTTOM_LEFT);
        Log.d(TAG, "twoStepCheck after: " + focusedMathEleList.toString());

//        MathElement topEle = focusElement.getFocusedMathEle(Constant.TOP);
//        if (topEle != null){
//            MathElement destinationEle = topEle.getFocusedMathEle(getOppositeElePos(pos));
//            if (destinationEle != null) this.focusedMathEleList.put(Constant.TOP,destinationEle);
//        }

    }

    //    private void learnAdjacentElements(MathElement focusElement,int startPos, int posToLearn) {
//        if (startPos != posToLearn) {
//            MathElement element = this.getFocusedMathEle(posToLearn);
//            if (element != null ) {
//                MathElement destinationEle = element.getFocusedMathEle(getOppositeElePos(startPos));
//                if (destinationEle != null) {
//                    this.focusedMathEleList.put(posToLearn, destinationEle);
//                    destinationEle.focusedMathEleList.put(getOppositeElePos(posToLearn), this);
//                }
//            }
//        }
//    }
    private void learnAdjacentElements(MathElement lastFocusedEle, int lastFocusedElePos, int posToLearn) {
        //1. using last focused element
        if (lastFocusedElePos != posToLearn) {
            MathElement element = lastFocusedEle.getFocusedMathEle(posToLearn);
            if (element != null && !element.equals(this)) {
                MathElement destinationEle = element.getFocusedMathEle(getOppositeElePos(lastFocusedElePos));
                if (destinationEle != null) {
                    this.focusedMathEleList.put(posToLearn, destinationEle);
                    destinationEle.focusedMathEleList.put(getOppositeElePos(posToLearn), this);
                }
            }
        }
    }


}
