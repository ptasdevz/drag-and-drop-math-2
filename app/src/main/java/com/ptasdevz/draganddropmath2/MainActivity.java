package com.ptasdevz.draganddropmath2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tech.gusavila92.websocketclient.WebSocketClient;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

import static android.view.MotionEvent.INVALID_POINTER_ID;
import static com.ptasdevz.draganddropmath2.MathElement.MathElementsNameRes;

public class MainActivity extends AppCompatActivity {

    public HashMap<String, ImageView> mathEleSrcImgList = new HashMap<>();
    public static HashMap<String, MathElePos> mathElePosList = new HashMap<>();
    public static HashMap<String, MathElement> mathEleList = new HashMap<>();
    public static int mActivePointerId = INVALID_POINTER_ID;
    ConstraintLayout workspace, cLayoutMain;
    boolean isConfig = false;
    String lastDragDataStr;
    ImageView trash;
    WebSocketClient webSocketClient;
    private StompClient mStompClient;

    static String TAG = "ptasdevz";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupStompConnection();
        workspace = findViewById(R.id.workspace);
        cLayoutMain = findViewById(R.id.cLayoutMain);

           /*
        Set up all maths elements with drag and drop capabilities.
         */
        for (HashMap.Entry<String, Integer> entry : MathElementsNameRes.entrySet()) {

            String name = entry.getKey();
            int resId = entry.getValue();
            final ImageView mathEleSrcImg = findViewById(resId);
            mathEleSrcImg.setTag(name);
            final MathElePos mathElePos = new MathElePos();
            mathEleSrcImgList.put(name, mathEleSrcImg);
            mathElePosList.put(name, mathElePos);
            mathElePos.name = name;

            mathEleSrcImg.setOnTouchListener((view, ev) -> {
                final int action = ev.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        setupActionDownOptions(mathElePos, view, ev);
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        setupActionMoveOptions(mathElePos, view, ev);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {

                        int x = (int) view.getX();
                        int y = (int) view.getY();
                        Rect workspaceBounds = new Rect();
                        workspace.getHitRect(workspaceBounds);

                        //get view coordinates with reference to workspace
                        if (workspaceBounds.contains(x, y)) {

                            int workspaceX = (int) workspace.getX();
                            int workspaceY = (int) workspace.getY();
                            int viewXConverted = x - workspaceX;
                            int viewYConverted = y - workspaceY;

                            mathElePos.viewPosX = viewXConverted + view.getWidth()/2;
                            mathElePos.viewPosY =viewYConverted + view.getHeight()/2;
                            String lastDraggedEle = dropImageView(workspace, mathElePos);
                            MathElement mathElement = mathEleList.get(lastDraggedEle);
                            repositionElement(mathElement);
                            Log.d(TAG, "on action up : in workspace view");
                        } else {
                            Log.d(TAG, "on action up : not in workspace view");
                        }
                        resetMathElePosition(mathElePos, view);
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL: {
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_POINTER_UP: {

                        final int pointerIndex = ev.getActionIndex();
                        final int pointerId = ev.getPointerId(pointerIndex);

                        if (pointerId == mActivePointerId) {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                            resetMathElePosition(mathElePos, view);
                        }
                        break;
                    }
                }
                return true;
            });
        }
    }

    public void repositionElement(MathElement mathElement) {

        ImageView eleImg = mathElement.getEleImg();
        MathElement focusedEle = getStationaryMathElement(mathElement);


        if (focusedEle != null) {
            Rect stationaryRect = new Rect();
            focusedEle.getEleImg().getHitRect(stationaryRect);
//            stationaryRect.top = focusedEle.getEleImg().getTop();
//            stationaryRect.left = focusedEle.getEleImg().getLeft();
//            stationaryRect.bottom = focusedEle.getEleImg().getBottom();
//            stationaryRect.right = focusedEle.getEleImg().getRight();

            Rect dropRect = new Rect();
            eleImg.getHitRect(dropRect);
//            dropRect.top = eleImg.getTop();
//            dropRect.left = eleImg.getLeft();
//            dropRect.bottom = eleImg.getBottom();
//            dropRect.right = eleImg.getRight();

            if (dropRect.intersect(stationaryRect)) {

                //Toast.makeText(this, "Images are intersecting", Toast.LENGTH_SHORT).show();

                //remove element focused element is trash can
                String focusedElementName = focusedEle.getEleImg().getTag().toString();
                String mathElementName = mathElement.getEleImg().getTag().toString();
                Log.d(TAG, "focused element " + focusedElementName);

                if (focusedElementName.contains(MathElement.TRASH)) {
                    removeMathElement(mathElement);
//                    mathElement.getEleImgLayout().removeView(mathElement.getEleImg());
//                    this.mathEleList.remove(mathElement.getEleImg().getTag());
                } else if (mathElementName.contains(MathElement.TRASH)) {
                    removeMathElement(focusedEle);
//                    focusedEle.getEleImgLayout().removeView(focusedEle.getEleImg());
//                    this.mathEleList.remove(focusedEle.getEleImg().getTag());

                } else {

                    int y1 = dropRect.bottom - dropRect.top;
                    int x1 = dropRect.right - dropRect.left;
                    if (dropRect.left == stationaryRect.left && dropRect.top == stationaryRect.top && dropRect.right == stationaryRect.right && dropRect.bottom == stationaryRect.bottom) {
                        //Toast.makeText(this, "cannot place image", Toast.LENGTH_SHORT).show();
                    } else if (dropRect.left == stationaryRect.left && dropRect.top == stationaryRect.top) {
                        //Toast.makeText(this, "left -top of stationaryRect is being intersected", Toast.LENGTH_SHORT).show();
                        if (y1 > x1) {
                            //Toast.makeText(this, "place image on left", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.LEFT);
                        } else {
                            //Toast.makeText(this, "place image on top", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.TOP);
                        }
                    } else if (dropRect.left == stationaryRect.left && dropRect.bottom == stationaryRect.bottom) {
                        //Toast.makeText(this, "left-bottom of stationaryRect is being intersected", Toast.LENGTH_SHORT).show();
                        if (y1 > x1) {
                            //Toast.makeText(this, "place image on left", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.LEFT);
                        } else {
                            //Toast.makeText(this, "place image on bottom", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.BOTTOM);
                        }
                    } else if (dropRect.right == stationaryRect.right && dropRect.top == stationaryRect.top) {
                        //Toast.makeText(this, "right-top of stationaryRect is being intersected", Toast.LENGTH_SHORT).show();
                        if (y1 > x1) {
                            //Toast.makeText(this, "place image on right", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.RIGHT);

                        } else {
                            //Toast.makeText(this, "place image on top", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.TOP);
                        }
                    } else if (dropRect.right == stationaryRect.right && dropRect.bottom == stationaryRect.bottom) {
                        //Toast.makeText(this, "right-bottom of stationaryRect is being intersected", Toast.LENGTH_SHORT).show();
                        if (y1 > x1) {
                            //Toast.makeText(this, "place image on right", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.RIGHT);
                        } else {
                            //Toast.makeText(this, "place image on bottom", Toast.LENGTH_SHORT).show();
                            placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, Constant.BOTTOM);
                        }
                    } else {
                        //Toast.makeText(this, "default:  place image where possible", Toast.LENGTH_SHORT).show();
                        placeElement(mathElement, eleImg, focusedEle, stationaryRect);

                    }
                }

            } else {

                //Toast.makeText(this, "Images are not intersecting", Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(this, imgV1.getHeight() + " onWindowFocusChanged", Toast.LENGTH_SHORT).show();
            ;

        }
    }

    private void placeElement(MathElement mathElement, ImageView eleImg, MathElement focusedEle,
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
                placeWherePossible(mathElement, eleImg, focusedEle, stationaryRect, pos);
                isPlaced = true;
                break;
            }
        }
        if (!isPlaced) {
            //revert to last position
            ElementPos lastPos = mathElement.getLastPos();
            if (lastPos.getLeft() != 0) {
                //Toast.makeText(this, "element reverted", Toast.LENGTH_SHORT).show();
                positionViewInLayout(mathElement, lastPos.getLeft(), lastPos.getTop(), true);
            } else {
                Toast.makeText(this, "element was removed", Toast.LENGTH_SHORT).show();
                removeMathElement(mathElement);
            }
        }
    }
    private void placeWherePossible(MathElement mathElement, ImageView eleImg, MathElement focusedEle
            , Rect stationaryRect, int preferredPlacementPos) {
        switch (preferredPlacementPos) {

            case Constant.LEFT: {
                if (focusedEle.getFocusedMathEle(Constant.LEFT) == null) placeToLeft(mathElement,
                        eleImg, focusedEle, stationaryRect, true);
                else placeElement(mathElement, eleImg, focusedEle, stationaryRect);
            }
            break;
            case Constant.TOP: {
                if (focusedEle.getFocusedMathEle(Constant.TOP) == null) placeToTop(mathElement,
                        eleImg, focusedEle, stationaryRect, true);
                else placeElement(mathElement, eleImg, focusedEle, stationaryRect);
            }
            break;
            case Constant.RIGHT: {
                if (focusedEle.getFocusedMathEle(Constant.RIGHT) == null) placeToRight(mathElement,
                        eleImg, focusedEle, stationaryRect, true);
                else placeElement(mathElement, eleImg, focusedEle, stationaryRect);
            }
            break;
            case Constant.BOTTOM: {
                if (focusedEle.getFocusedMathEle(Constant.BOTTOM) == null)
                    placeToBottom(mathElement,
                            eleImg, focusedEle, stationaryRect, true);
                else placeElement(mathElement, eleImg, focusedEle, stationaryRect);
            }
            break;
            default: {

            }
        }

    }
    private void placeToRight(MathElement mathElement, ImageView eleImg,
                              MathElement focusedEle, Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            mathElement.addFocusedMathEle(Constant.LEFT, focusedEle);
        }

        int width = focusedEle.getEleImg().getWidth();
        int xloc = stationaryRect.left + width;
        int yLoc = stationaryRect.top + focusedEle.getEleImg().getHeight() / 2;
        positionViewInLayout(mathElement, xloc, yLoc, false);
    }

    private void placeToBottom(MathElement mathElement, ImageView eleImg,
                               MathElement focusedEle, Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            mathElement.addFocusedMathEle(Constant.TOP, focusedEle);
        }
        int xloc = stationaryRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = stationaryRect.bottom + focusedEle.getEleImg().getHeight() / 4;
        positionViewInLayout(mathElement, xloc, yLoc, false);
    }

    private void placeToTop(MathElement mathElement, ImageView eleImg, MathElement focusedEle,
                            Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            mathElement.addFocusedMathEle(Constant.BOTTOM, focusedEle);
        }
        int xloc = stationaryRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = stationaryRect.top - focusedEle.getEleImg().getHeight() / 4;
        positionViewInLayout(mathElement, xloc, yLoc, false);
    }

    private void placeToLeft(MathElement mathElement, ImageView eleImg, MathElement focusedEle,
                             Rect stationaryRect, boolean isAddFocusEle) {
        if (isAddFocusEle) {
            mathElement.addFocusedMathEle(Constant.RIGHT, focusedEle);
        }
        int xloc = stationaryRect.left;
        int yLoc = stationaryRect.top + focusedEle.getEleImg().getHeight() / 2;
        positionViewInLayout(mathElement, xloc, yLoc, false);

    }
    private MathElement getStationaryMathElement(MathElement mathElement) {

        double shortestDis = Integer.MAX_VALUE;
        MathElement focusedEle = null;
        int dropEleLeft = mathElement.getEleImg().getLeft();
        int dropEleTop = mathElement.getEleImg().getTop();
        for (HashMap.Entry<String, MathElement> entry : mathEleList.entrySet()) {
            MathElement focusedMathEle = entry.getValue();
            if (!focusedMathEle.equals(mathElement)) {
                int stationaryEleLeft = focusedMathEle.getEleImg().getLeft();
                int stationaryEleTop = focusedMathEle.getEleImg().getTop();
                int xVal = dropEleLeft - stationaryEleLeft;
                int yVal = dropEleTop - stationaryEleTop;

                double distance = Math.sqrt((xVal * xVal) + (yVal * yVal));
                if (focusedEle != null ) Log.d(TAG, "getStationaryMathElement: "+mathElement.getImageTag()+ " distance: "+distance + "from ele: "+focusedEle.getImageTag());
                if (distance < shortestDis) {

                    shortestDis = distance;
                    focusedEle = focusedMathEle;
                }
            }
        }
        return focusedEle;
    }

    private void removeMathElement(MathElement mathElement) {
        mathElement.getEleImgLayout().removeView(mathElement.getEleImg());
        this.mathEleList.remove(mathElement.getEleImg().getTag());
    }

    public static void setupActionDownOptions(MathElePos mathElePos, View view, MotionEvent ev) {
        //get x and y coordinates of point with respect to element
        final int pointerIndex = ev.getActionIndex();
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);
        Log.d(TAG, "onTouch: img down: lastPtrPosX:" + x + "----lastPtrPosY:" + y);

        // Remember where we started (for dragging)
        mathElePos.lastPtrPosX = x;
        mathElePos.lastPtrPosY = y;
        // Save the ID of this pointer (for dragging)
        mActivePointerId = ev.getPointerId(0);

        // Save initial element position with respect to element's parent
        mathElePos.initialElePosX = view.getX();
        mathElePos.initialElePosY = view.getY();
    }

    public static void setupActionMoveOptions(MathElePos mathElePos, View view, MotionEvent ev) {
        // Find the index of the active pointer and fetch its position
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);

        final float movingPtrPosX = ev.getX(pointerIndex);
        final float movingPtrPosY = ev.getY(pointerIndex);

        //calculate distance moved
        final float dx = movingPtrPosX - mathElePos.lastPtrPosX;
        final float dy = movingPtrPosY - mathElePos.lastPtrPosY;

        //calculate  the future lastPtrPosX and lastPtrPosY of view
        float futurePosYdown = dy + view.getY() + view.getHeight();
        float futurePosYup = dy + view.getY();
        float futurePosXright = dx + view.getX() + view.getWidth();
        float futurePosXleft = dx + view.getX();

                        /*
                        only update if view position remains within the limits of the layout.
                         */
        ConstraintLayout parent = (ConstraintLayout) view.getParent();
        if (futurePosYdown < parent.getHeight() && futurePosYup > 0) {

            mathElePos.posY += dy;
            mathElePos.lastPtrPosY = movingPtrPosY;// Remember this touch position for the next move event
        }
        if (futurePosXright < parent.getWidth() && futurePosXleft > 0) {

            mathElePos.posX += dx;
            mathElePos.lastPtrPosX = movingPtrPosX;// Remember this touch position for the next move event
        }

        view.bringToFront();
        positionViewInCLayout(mathElePos.posX, mathElePos.posY, view,
                (ConstraintLayout) view.getParent());
    }

    private String dropImageView(ConstraintLayout view, MathElePos mathElePos) {
        float x = mathElePos.viewPosX;
        float y = mathElePos.viewPosY;

        // Gets the text data from the item.

        String name = mathElePos.name;
        ConstraintLayout layout = view;
        ImageView eleImg;
        MathElement mathElement;

        if (!name.contains("COPY")) { //drop copy

            mathElement = getMyMathElement(name, x, y, layout.getId(), layout);
            eleImg = mathElement.getEleImg();
            layout.addView(eleImg);

        } else if (!name.contains(String.valueOf(layout.getId()))) {// move copy between locations

            mathElement = getMyMathElement(name, x, y, layout.getId(), layout);
            eleImg = mathElement.getEleImg();
            layout.addView(eleImg);
            String[] dragDataArr = name.split("_");
            ConstraintLayout viewById = findViewById(Integer.parseInt(dragDataArr[2]));
            viewById.removeView(mathEleList.get(name).getEleImg());
            mathEleList.remove(name);

        } else { //move copy in its created location

            mathElement = mathEleList.get(name);
        }

        positionViewInLayout(mathElement, x, y, false);

        return mathElement.getEleImg().getTag().toString();

    }
    private void positionViewInLayout(MathElement mathElement, float dropPosX,
                                      float dropPosY, boolean isReverted) {

        ConstraintLayout layout = mathElement.getEleImgLayout();
        ImageView dropEleImg = mathElement.getEleImg();

        //update last dropped position of element
        ElementPos lastPos = mathElement.getLastPos();
        ElementPos currPos = mathElement.getCurrPos();

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
        int eleImgWidth = dropEleImg.getWidth();
        int eleImgHeight = dropEleImg.getHeight();
        int layoutConstraintWidth = layout.getWidth() - dropEleImg.getWidth() + 1;
        int layoutConstraintHeight = layout.getHeight() - dropEleImg.getHeight() + 1;
        float horizontalBias = (dropPosX - (eleImgWidth / 2)) / layoutConstraintWidth;
        if (horizontalBias < 0) horizontalBias = 0;
        else if (horizontalBias > 1) horizontalBias = 1;
        constraintSet.setHorizontalBias(dropEleImg.getId(), horizontalBias);
        float verticalBias = (dropPosY - (eleImgHeight / 2)) / layoutConstraintHeight;
        if (verticalBias < 0) verticalBias = 0;
        else if (verticalBias > 1) verticalBias = 1;
        constraintSet.setVerticalBias(dropEleImg.getId(), verticalBias);
        constraintSet.applyTo(layout);
    }

    public MathElement getMyMathElement(String imgTag, float x, float y, int parentId, ConstraintLayout layout) {

        String imgName = imgTag.split("_")[0];
        ImageView imageView = findViewById(MathElementsNameRes.get(imgName));
        Drawable drawable = imageView.getDrawable();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        MathElement mathElement = new MathElement(this, drawable, x, y,
                width, height, imgName, parentId, layout);
        mathEleList.put((String) mathElement.getEleImg().getTag(), mathElement);
        return mathElement;
    }

    private void resetMathElePosition(MathElePos mathElePos, View view) {
        float x = mathElePos.initialElePosX - view.getX();
        float y = mathElePos.initialElePosY - view.getY();

        positionViewInCLayout(x, y, view,
                (ConstraintLayout) view.getParent());
    }

    public static int getOppositeElePos(int pos) {
        if (pos <= 4) return pos + 4;
        return pos - 4;
    }

    public static HashMap<String, MathElement> getMathEleList() {
        return mathEleList;
    }

    public static long getUniqueId() {
        try {
            Thread.sleep(0, 10);
        } catch (InterruptedException e) {
        }
        return new Timestamp(System.nanoTime()).getTime();
    }

    private void setupStompConnection() {
        try {
            mStompClient = Stomp.over(
                    Stomp.ConnectionProvider.OKHTTP, "ws://192.168.137.1:8080/endpoint/websocket");
            mStompClient.connect();

            mStompClient.topic("/topic/send-remote-message")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        String payload = topicMessage.getPayload();
                        Log.d(TAG, "receiveStompConnectionMsg: " + payload);
                        final String msg = payload;
                        runOnUiThread(() -> {
                            try {

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }, throwable -> {
                        Log.e(TAG, "Error on subscribe topic", throwable);
                    });

            mStompClient.lifecycle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(lifecycleEvent -> {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                Log.d(TAG, "Stomp connection opened");
                                break;
                            case CLOSED:
                                Log.d(TAG, "Stomp connection closed");
                                break;
                            case ERROR:
                                Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                                break;
                        }
                    }, throwable -> {
                        Log.e(TAG, "Error on subscribe lifecycle", throwable);
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void positionViewInCLayout(float dx, float dy, View view, ConstraintLayout layout) {

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

}
