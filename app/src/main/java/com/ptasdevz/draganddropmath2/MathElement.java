package com.ptasdevz.draganddropmath2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class MathElement extends TutorMyPeerElement {

    public static HashMap<String, MathElement> mathEleList = new HashMap<>();
    public static HashMap<String, Integer> MathElementsNameRes = new HashMap<>();
    public static String sendMathElementURI = "/app/send-math-element-message";
    public static ArrayList<ElementPos> elementPosArrayList = new ArrayList<>();
    public static HashMap<String, Object> remoteDataToSend = new HashMap<>();
    public static HashMap<String, Object> remoteDataRecieved = new HashMap<>();
    public static int mActivePointerId = INVALID_POINTER_ID;
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
        MathElementsNameRes.put(TRASH, R.id.trash);
    }

    private static final int DOUBLE_CLICK_TIME = 250; // double click timer

    protected ImageView eleImg;
    private ConstraintLayout mainLayout;
    private ConstraintLayout workspaceLayout;
    private SparseArray<MathElement> neighbouringMathEleList;
    private long id;
    private boolean waitDouble = true;
    private float lastPtrPosX;
    private float lastPtrPosY;
    protected float changeOfPosX;
    protected float changeOfPosY;
    private String name;
    private float initialElePosX;
    private float initialElePosY;
    private float dropPosX;
    private float dropPosY;
    private Context context;
    public int remoteAction;
    private boolean isDropped;

    public MathElement(final Context context, ImageView eleImageView,
                       String eleName, int parentId,
                       ConstraintLayout workspaceLayout, ConstraintLayout mainLayout, boolean isCopy,
                       boolean isAlsoRemote) {

        Drawable eleImgDrawable = eleImageView.getDrawable();
        float srcImgWidth = eleImgDrawable.getIntrinsicWidth();
        float srcImgHeight = eleImgDrawable.getIntrinsicHeight();
        float elePosX = eleImageView.getX();
        float elePosY = eleImageView.getY();
        eleImg = eleImageView;
        setupElement(context, eleImgDrawable, elePosX, elePosY, srcImgWidth, srcImgHeight, eleName,
                parentId, workspaceLayout, mainLayout, isCopy, isAlsoRemote);

    }

    @SuppressLint("ClickableViewAccessibility")
    public MathElement(final Context context, Drawable eleImgDrawable, float elePosX, float elePosY,
                       float srcImgWidth, float srcImgHeight, String eleName, int parentId,
                       ConstraintLayout workspaceLayout, ConstraintLayout mainLayout, boolean isCopy, boolean isAlsoRemote) {
        setupElement(context, eleImgDrawable, elePosX, elePosY, srcImgWidth, srcImgHeight, eleName,
                parentId, workspaceLayout, mainLayout, isCopy, isAlsoRemote);
    }

    public ImageView getEleImg() {
        return eleImg;
    }

    public static HashMap<String, MathElement> getMathEleList() {
        return mathEleList;
    }

    public ConstraintLayout getMainLayout() {
        return mainLayout;
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

    public float getChangeOfPosX() {
        return changeOfPosX;
    }

    public void setChangeOfPosX(float changeOfPosX) {
        this.changeOfPosX = changeOfPosX;
    }

    public float getChangeOfPosY() {
        return changeOfPosY;
    }

    public void setChangeOfPosY(float changeOfPosY) {
        this.changeOfPosY = changeOfPosY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.eleImg.setTag(name);
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

    public float getDropPosX() {
        return dropPosX;
    }

    public void setDropPosX(float dropPosX) {
        this.dropPosX = dropPosX;
    }

    public float getDropPosY() {
        return dropPosY;
    }

    public void setDropPosY(float dropPosY) {
        this.dropPosY = dropPosY;
    }

    public boolean isDropped() {
        return isDropped;
    }

    @Override
    public String toString() {
        return "MathElement{" +
                "eleImg=" + eleImg +
                ", mainLayout=" + mainLayout +
                ", neighbouringMathEleList=" + neighbouringMathEleList +
                ", id=" + id +
                ", waitDouble=" + waitDouble +
                ", lastPtrPosX=" + lastPtrPosX +
                ", lastPtrPosY=" + lastPtrPosY +
                ", changeOfPosX=" + changeOfPosX +
                ", changeOfPosY=" + changeOfPosY +
                ", name='" + name + '\'' +
                ", initialElePosX=" + initialElePosX +
                ", initialElePosY=" + initialElePosY +
                ", dropPosX=" + dropPosX +
                ", dropPosY=" + dropPosY +
                ", context=" + context +
                ", remoteAction=" + remoteAction +
                ", isDropped=" + isDropped +
                '}';
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

    //=========================================Helpers==============================================
    @SuppressLint("ClickableViewAccessibility")
    private void setupElement(Context context, Drawable eleImgDrawable, float elePosX, float elePosY,
                              float srcImgWidth, float srcImgHeight, String eleName, int parentId,
                              ConstraintLayout workspaceLayout, ConstraintLayout mainLayout,
                              boolean isCopy, boolean isAlsoRemote) {
        this.context = context;
        if (isCopy) {
            eleImg = new ImageView(this.context);
            if (isAlsoRemote) {
                eleName = eleName + "_COPY_" + parentId + "_" + getUniqueId();
                remoteDataToSend.put("remoteCopyName", eleName); // used for renaming elements created remotely
            } else {
                String eleNameCopy = (String) remoteDataRecieved.get("remoteCopyName");
                eleName = eleNameCopy;
            }
            eleImg.setTag(eleName);
        }

        eleImg.setBackground(context.getDrawable(R.drawable.custom_border));
        eleImg.setImageDrawable(eleImgDrawable);
        eleImg.setMaxHeight((int) srcImgHeight);
        eleImg.setMaxWidth((int) srcImgWidth);
        this.name = eleName;

        //set up click options for trash element
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
                }

                private void doubleClick() {
                    Iterator iterator = getMathEleList().entrySet().iterator();
                    while (iterator.hasNext()) {
                        HashMap.Entry<String, MathElement>
                                element = (HashMap.Entry<String, MathElement>) iterator.next();
                        String elementKey = element.getKey();
                        if (elementKey.contains("COPY")) {
                            MathElement mathElement = element.getValue();
                            MathElement.this.workspaceLayout.removeView(mathElement.getEleImg());
                            iterator.remove();
                        }
                    }
                }
            });
        }

        //setup drag and drop options on image
        eleImg.setOnTouchListener((view, motionEvent) -> {

            boolean isRemoteEvent = "remote".equalsIgnoreCase((String) view.getTag());

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mActivePointerId =
                            motionEvent.getPointerId(0);
                    float evY = motionEvent.getY(motionEvent.getActionIndex());
                    float evX = motionEvent.getX(motionEvent.getActionIndex());
                    actionDownLocalOptions(this, evX, evY);
                    if (!isRemoteEvent) saveElementPos(0, 0,
                            MotionEvent.ACTION_DOWN, this, false);

                }

                break;
                case MotionEvent.ACTION_MOVE: {
                    int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    float movingPtrPosX = motionEvent.getX(pointerIndex);
                    float movingPtrPosY = motionEvent.getY(pointerIndex);
                    actionMoveLocalOptions(this, pointerIndex, movingPtrPosX, movingPtrPosY);
                    this.positionMathEle(false);
                    if (!isRemoteEvent) saveElementPos(this.getChangeOfPosX(), this.getChangeOfPosY(),
                                MotionEvent.ACTION_MOVE, this, false);
                }
                break;
                case MotionEvent.ACTION_UP: {
                    if (isCopy || name.equalsIgnoreCase(TRASH)) {
                        resetFocusedMathEleList();
                        repositionElement();
                        learnNeighbouringElements();
                    }else {
                        actionUpLocalOptionsEleOriginal(isRemoteEvent);
                        resetMathElePosition();
                        saveElementPos(0, 0, MotionEvent.ACTION_UP, this,
                                isRemoteEvent);

                        //send math element position data remotely
                        if (!isRemoteEvent) sendElementPosData();
                    }
                    if (name.equalsIgnoreCase(TRASH))view.performClick(); //enable click functions as well on element
                }
                break;
                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    elementPosArrayList.clear();
                }
                break;
                case MotionEvent.ACTION_POINTER_UP: {

                    final int pointerIndex = motionEvent.getActionIndex();
                    final int pointerId = motionEvent.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        //final int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                        //this.resetMathElePosition();
                    }
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
        this.mainLayout = mainLayout;
        this.workspaceLayout = workspaceLayout;
        neighbouringMathEleList = new SparseArray<>();
        id = getUniqueId();
        mathEleList.put(name,this);
    }

    public MathElement dropElement(boolean isAlsoRemote) {
        float x = this.getDropPosX();
        float y = this.getDropPosY();

        // Gets the text data from the item.
        String name = this.getName();
        ImageView eleImg;
        MathElement mathEleCopy;

        if (!name.contains("COPY")) { //drop copy

            mathEleCopy = getMathEleCopy(x, y, isAlsoRemote);
            eleImg = mathEleCopy.getEleImg();
            this.workspaceLayout.addView(eleImg);
            this.rePositionMathEle(mathEleCopy, x, y);
            return mathEleCopy;
        }

        return null;
    }

    /**
     * Repositions a MathElement object at a given drop position coordinate.
     *
     * @param mathEle  The MathElement to be repositioned.
     * @param dropPosX The X drop position. The value is relative to parent of the MathElement.
     * @param dropPosY The Y drop position. The value is relative to the parent of the MathElement.
     */
    private void rePositionMathEle(MathElement mathEle, float dropPosX, float dropPosY) {

        ConstraintLayout layout = workspaceLayout;
        ImageView dropEleImg = mathEle.getEleImg();

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

    /**
     * Places this MathElement object at specific x and y coordinates of the current change of x and
     * y positions.
     *
     * @param isRevert True indicates element should be reverted to its original coordinates
     *                 if it cannot be placed.
     */
    public void positionMathEle(boolean isRevert) {

        View view = this.eleImg;
        float dx, dy;
        if (isRevert) {
            //calculate change of distance from initial position
            dx = this.getInitialElePosX() - this.eleImg.getX();
            dy = this.getInitialElePosY() - this.eleImg.getY();

            //use distance calculated to go back to initial position.
        } else {
            dx = this.changeOfPosX;
            dy = this.changeOfPosY;
        }
        ConstraintLayout layout = (ConstraintLayout) view.getParent();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.connect(view.getId(), ConstraintSet.RIGHT, layout.getId(),
                ConstraintSet.RIGHT, 0);
        constraintSet.connect(view.getId(), ConstraintSet.TOP, layout.getId(),
                ConstraintSet.TOP, 0);
        constraintSet.connect(view.getId(), ConstraintSet.LEFT, layout.getId(),
                ConstraintSet.LEFT, 0);
        constraintSet.connect(view.getId(), ConstraintSet.BOTTOM, layout.getId(),
                ConstraintSet.BOTTOM, 0);

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

    public void resetMathElePosition() {


        positionMathEle(true);
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
                        else
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.LEFT);
                    }

                    //bottom-left side of neighbouringMathEle rect is intersected
                    else if (thisRect.left == neighbouringMathEleRect.left
                            && thisRect.bottom == neighbouringMathEleRect.bottom) {

                        //place at bottom
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.BOTTOM);
                        }
                        //place at left
                        else
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.LEFT);
                    }

                    //top-right side of stationary rect is intersected
                    else if (thisRect.right == neighbouringMathEleRect.right
                            && thisRect.top == neighbouringMathEleRect.top) {

                        //place at top
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.TOP);
                        }
                        //place at right
                        else
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.RIGHT);
                    }

                    //bottom-right side of neighbouringMathEle rect is intersected.
                    else if (thisRect.right == neighbouringMathEleRect.right
                            && thisRect.bottom == neighbouringMathEleRect.bottom) {

                        //place at bottom
                        if (intersectHeight < intersectWidth) {
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.BOTTOM);
                        }
                        // place at right
                        else
                            placeWherePossible(eleImg, neighbouringMathEle, neighbouringMathEleRect, Constant.RIGHT);
                    }

                    //place in next available position starting from left
                    else placeElement(eleImg, neighbouringMathEle, neighbouringMathEleRect);
                }
            }
        }
        this.isDropped = true;
    }

    public static void actionDownLocalOptions(MathElement mathElement, float motionEventX,
                                              float motionEventY) {
        // Remember where we started (for dragging)
        mathElement.setLastPtrPosX(motionEventX);
        mathElement.setLastPtrPosY(motionEventY);

        // Save initial element position with respect to element's parent
        mathElement.setInitialElePosX(mathElement.getEleImg().getX());
        mathElement.setInitialElePosY(mathElement.getEleImg().getY());

    }
    public static void actionMoveLocalOptions(MathElement mathElement, final int pointerIndex,
                                              final float movingPtrPosX, final float movingPtrPosY) {
        if (pointerIndex != -1) {
            //calculate change of distance from lastPtrPos
            final float dx = movingPtrPosX - mathElement.getLastPtrPosX();
            final float dy = movingPtrPosY - mathElement.getLastPtrPosY();
            ImageView view = mathElement.getEleImg();

            //calculate  the future lastPtrPosX and lastPtrPosY of view
            float futurePosYdown = dy + view.getY() + view.getHeight();
            float futurePosYup = dy + view.getY();
            float futurePosXright = dx + view.getX() + view.getWidth();
            float futurePosXleft = dx + view.getX();

        /*
        only update if view position remains within the limits of the layout which is known
        from the future values.
        */
            ConstraintLayout parent = (ConstraintLayout) view.getParent();

            if (futurePosYdown < parent.getHeight() && futurePosYup > 0) {
                mathElement.setChangeOfPosY(mathElement.getChangeOfPosY() + dy);
                mathElement.setLastPtrPosY(movingPtrPosY);// Remember this touch position for the next move event
            }
            if (futurePosXright < parent.getWidth() && futurePosXleft > 0) {

                mathElement.setChangeOfPosX(mathElement.getChangeOfPosX() + dx);
                mathElement.setLastPtrPosX(movingPtrPosX);// Remember this touch position for the next move event
            }

            view.bringToFront();

        }

    }

    public static void saveElementPos(float dx, float dy, int action, MathElement mathElement,
                                      boolean isRemoteEvent) {
        if (!isRemoteEvent) {
            ElementPos elementPos = new ElementPos();
            elementPos.action = action;
            elementPos.X = dx;
            elementPos.Y = dy;
            ConstraintLayout parent = (ConstraintLayout) mathElement.getEleImg().getParent();
            remoteDataToSend.put("parentWidth", parent.getWidth());
            remoteDataToSend.put("parentHeight", parent.getHeight());
            remoteDataToSend.put("eleName", mathElement.getName());
            elementPosArrayList.add(elementPos);
        }
    }

    private void actionUpLocalOptionsEleOriginal(boolean isRemoteEvent) {
        int x = (int) eleImg.getX();
        int y = (int) eleImg.getY();
        Rect workspaceBounds = new Rect();
        workspaceLayout.getHitRect(workspaceBounds);

        //get view coordinates with reference to workspaceLayout
        if (workspaceBounds.contains(x, y)) {

            //x and y pos are converted to that with respect to workspaceLayout
            int workspaceX = (int) workspaceLayout.getX();
            int workspaceY = (int) workspaceLayout.getY();
            int viewXConverted = x - workspaceX;
            int viewYConverted = y - workspaceY;

            // add extra so that x and y coordinates are at the center of element
            this.setDropPosX(viewXConverted + eleImg.getWidth() / 2);
            this.setDropPosY(viewYConverted + eleImg.getHeight() / 2);
            //place math element within workspaceLayout
            MathElement mathEleCopy = dropElement(!isRemoteEvent);

            if (mathEleCopy != null) {
                mathEleCopy.repositionElement();
                mathEleCopy.learnNeighbouringElements();
            }
        }
    }

    public static void sendElementPosData() {
        Gson gson = new Gson();
        String ptrPosArrayListString = gson.toJson(elementPosArrayList,
                new TypeToken<ArrayList<ElementPos>>() {
                }.getType());
        remoteDataToSend.put("ptrPosList", ptrPosArrayListString);
        remoteDataToSend.put("appId", DragAndDropMathApplication.APPLICATION_ID);

        String remoteDataString = gson.toJson(remoteDataToSend,
                new TypeToken<HashMap<String, Object>>() {}.getRawType());

        DragAndDropMathApplication.mStompClient.send(sendMathElementURI, remoteDataString).subscribe();
        elementPosArrayList.clear();
    }

    private MathElement getMathEleCopy(float x, float y, boolean isAlsoRemote) {

        Drawable drawable = eleImg.getDrawable();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        MathElement mathElement = MathElementFactory.getNewInstance(context, drawable, x, y,
                (float) width, (float) height, this.name, this.mainLayout.getId(), this.workspaceLayout,
                this.mainLayout, true, isAlsoRemote);
        mathEleList.put(mathElement.getName(), mathElement);
        return mathElement;
    }

    private void removeMathElement() {
        this.workspaceLayout.removeView(this.getEleImg());
        mathEleList.remove(this.name);
    }

    private void resetFocusedMathEleList() {
        int size = neighbouringMathEleList.size();

        for (int i = 0; i < size; i++) {
            MathElement mathElement = this.neighbouringMathEleList.valueAt(i);
            if (mathElement != null) {
                SparseArray<MathElement> neighbouringMathEleList = mathElement.getNeighbouringMathEleList();
                int index = neighbouringMathEleList.indexOfValue(this);
                if (index > -1) {
                    int key = neighbouringMathEleList.keyAt(index);
                    neighbouringMathEleList.remove(key);
                }
            }
        }

        this.neighbouringMathEleList = new SparseArray<>();
    }

    private void placeElement(ImageView eleImg, MathElement focusedEle,
                              Rect stationaryRect) {

        //Figure out where to position element
        SparseArray<MathElement> focusedMathEleList = focusedEle.getNeighbouringMathEleList();
        boolean isPositioned = false;
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
                isPositioned = true;
                break;
            }
        }
        if (!isPositioned) {
            //revert to last position
            if (this.isDropped) {
                this.positionMathEle(true);
                Toast.makeText(context, "Element cannot be re-positioned.", Toast.LENGTH_SHORT).show();
            }
            //remove if element has not been previously dropped.
            else {
                removeMathElement();
                Toast.makeText(context, "Element cannot be positioned.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void placeWherePossible(ImageView eleImg, MathElement focusedEle
            , Rect focusedEleRect, int preferredPlacementPos) {

        switch (preferredPlacementPos) {

            case Constant.LEFT: {
                if (focusedEle.getFocusedMathEle(Constant.LEFT) == null) placeToLeft(
                        focusedEle, focusedEleRect);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            case Constant.TOP: {
                if (focusedEle.getFocusedMathEle(Constant.TOP) == null) placeToTop(
                        focusedEle, focusedEleRect);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            case Constant.RIGHT: {
                if (focusedEle.getFocusedMathEle(Constant.RIGHT) == null) placeToRight(
                        focusedEle, focusedEleRect);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            case Constant.BOTTOM: {
                if (focusedEle.getFocusedMathEle(Constant.BOTTOM) == null)
                    placeToBottom(
                            focusedEle, focusedEleRect);
                else placeElement(eleImg, focusedEle, focusedEleRect);
            }
            break;
            default: {

            }
        }

    }

    private void placeToRight(MathElement focusedEle, Rect focusedEleRect) {

        int width = focusedEle.getEleImg().getWidth();
        int xloc = focusedEleRect.left + width + width / 4;
        int yLoc = focusedEleRect.top + focusedEle.getEleImg().getHeight() / 2;
        this.rePositionMathEle(this, xloc, yLoc);
    }

    private void placeToBottom(MathElement focusedEle, Rect focusedEleRect) {

        int xloc = focusedEleRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = focusedEleRect.bottom + focusedEle.getEleImg().getHeight() / 4;
        this.rePositionMathEle(this, xloc, yLoc);
    }

    private void placeToTop(MathElement focusedEle, Rect focusedEleRect) {

        int xloc = focusedEleRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = focusedEleRect.top - focusedEle.getEleImg().getHeight() / 4;
        this.rePositionMathEle(this, xloc, yLoc);
    }

    private void placeToLeft(MathElement focusedEle, Rect focusedEleRect) {

        int width = focusedEle.getEleImg().getWidth();
        int xloc = focusedEleRect.left - width / 4;
        int yLoc = focusedEleRect.top + focusedEle.getEleImg().getHeight() / 2;
        this.rePositionMathEle(this, xloc, yLoc);
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
     *
     * @param pos                 the neighbour's position
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
                            addNeighbouringMathEle(Constant.RIGHT, mathElement);
                        }

                        //left element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.top == mathEleRect.top
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.LEFT, mathElement);
                        }

                        //top element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.left == mathEleRect.left
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.TOP, mathElement);
                        }

                        //top-left element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.TOP_LEFT, mathElement);
                        }

                        //top-right element is being considered
                        else if (thisRect.left == mathEleRect.left
                                && thisRect.bottom == mathEleRect.bottom) {
                            addNeighbouringMathEle(Constant.TOP_RIGHT, mathElement);
                        }

                        //bottom element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.left == mathEleRect.left
                                && thisRect.top == mathEleRect.top) {
                            addNeighbouringMathEle(Constant.BOTTOM, mathElement);
                        }

                        //bottom-left element is being considered
                        else if (thisRect.right == mathEleRect.right
                                && thisRect.top == mathEleRect.top) {
                            addNeighbouringMathEle(Constant.BOTTOM_LEFT, mathElement);

                        }
                        //bottom-right element is being considered
                        else if (thisRect.left == mathEleRect.left
                                && thisRect.top == mathEleRect.top) {
                            addNeighbouringMathEle(Constant.BOTTOM_RIGHT, mathElement);
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
