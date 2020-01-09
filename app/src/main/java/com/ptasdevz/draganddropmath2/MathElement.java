package com.ptasdevz.draganddropmath2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.content.Context.WINDOW_SERVICE;
import static android.view.MotionEvent.INVALID_POINTER_ID;

public class MathElement extends TutorMyPeerElement {

    public static final String TAG = "ptasdevz";
    public static final String ELE_NAME = "eleName";
    public static final String REMOTE_COPY_NAME = "eventReceived";
    public static final String ELE_POS_LIST = "elePosList";
    public static final String APP_ID = "appId";
    public static ArrayMap<String, MathElement> mathEleList = new ArrayMap<>();
    public static LinkedHashMap<String, Integer> mathEleNameRes = new LinkedHashMap<>();
    public static final String sendMathElementURI = "/app/send-math-element-message";
    public static ArrayList<EleActionPos> eleActionPosQueue = new ArrayList<>();
    public static HashMap<String, Object> eventToSend = new HashMap<>();
    public static Queue<HashMap<String, Object>> eventQueue = new ConcurrentLinkedQueue<>();
    public static int mActivePointerId = INVALID_POINTER_ID;
    public static final String NUMBER_0 = "number0";
    public static final String NUMBER_1 = "number1";
    public static final String NUMBER_2 = "number2";
    public static final String NUMBER_3 = "number3";
    public static final String NUMBER_4 = "number4";
    public static final String NUMBER_5 = "number5";
    public static final String NUMBER_6 = "number6";
    public static final String NUMBER_7 = "number7";
    public static final String NUMBER_8 = "number8";
    public static final String NUMBER_9 = "number9";
    public static final String PLUS = "plus";
    public static final String MINUS = "minus";
    public static final String MULTIPLY = "multiply";
    public static final String DIVIDE = "divide";
    public static final String EQUAL = "equal";
    public static final String TRASH = "trash";
    public static final String TRASH_SHADOW = "trash_shadow";
    private static final int DOUBLE_CLICK_TIME = 250; // double click timer
    private static int statusBarHeight = 0;
    private static MathElementConstraintLayout mainLayout;
    private static MathElementConstraintLayout workspaceLayout;


    static {
        mathEleNameRes.put(TRASH_SHADOW, R.id.trash_shadow);
        mathEleNameRes.put(NUMBER_0, R.id.number0Img);
        mathEleNameRes.put(NUMBER_1, R.id.number1Img);
        mathEleNameRes.put(NUMBER_2, R.id.number2Img);
        mathEleNameRes.put(NUMBER_3, R.id.number3Img);
        mathEleNameRes.put(NUMBER_4, R.id.number4Img);
        mathEleNameRes.put(NUMBER_5, R.id.number5Img);
        mathEleNameRes.put(NUMBER_6, R.id.number6Img);
        mathEleNameRes.put(NUMBER_7, R.id.number7Img);
        mathEleNameRes.put(NUMBER_8, R.id.number8Img);
        mathEleNameRes.put(NUMBER_9, R.id.number9Img);
        mathEleNameRes.put(MINUS, R.id.minusImg);
        mathEleNameRes.put(PLUS, R.id.plusImg);
        mathEleNameRes.put(EQUAL, R.id.equalImg);
        mathEleNameRes.put(MULTIPLY, R.id.multiplyImg);
        mathEleNameRes.put(DIVIDE, R.id.divideImg);
        mathEleNameRes.put(TRASH, R.id.trash);
    }

    private ImageView eleImg;
    private SparseArray<MathElement> neighbouringMathEleList;
    private long id;
    private boolean waitDouble = true;
    private String name;
    private float initialElePosX;
    private float initialElePosY;
    private Context context;
    private boolean isDropped;
    private float horizontalBias;
    private float verticalBias;

    //shadow
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private int[] mainLayoutAbsPos;
    private int[] workspaceAbsPos;
    private HashMap<String, Object> eventReceived;
    private String shadowName = name + "_" + "Shadow";
    public boolean isRemoteExecution;
    public Object eventAction = new Object();


    public MathElement(final Context context, ImageView eleImageView,
                       String eleName, int parentId,
                       MathElementConstraintLayout workspaceLayout,
                       MathElementConstraintLayout mainLayout, boolean isCopy,
                       @Nullable HashMap<String, Object> mathEleEvent) {

        Drawable eleImgDrawable = eleImageView.getDrawable();
        float srcImgWidth = eleImgDrawable.getIntrinsicWidth();
        float srcImgHeight = eleImgDrawable.getIntrinsicHeight();
        float elePosX = eleImageView.getX();
        float elePosY = eleImageView.getY();
        eleImg = eleImageView;
        setupElement(context, eleImgDrawable, elePosX, elePosY, srcImgWidth, srcImgHeight, eleName,
                parentId, workspaceLayout, mainLayout, isCopy, mathEleEvent);

    }

    @SuppressLint("ClickableViewAccessibility")
    public MathElement(final Context context, Drawable eleImgDrawable, float elePosX, float elePosY,
                       float srcImgWidth, float srcImgHeight, String eleName, int parentId,
                       MathElementConstraintLayout workspaceLayout,
                       MathElementConstraintLayout mainLayout, boolean isCopy,
                       @Nullable HashMap<String, Object> mathEleEvent) {
        setupElement(context, eleImgDrawable, elePosX, elePosY, srcImgWidth, srcImgHeight, eleName,
                parentId, workspaceLayout, mainLayout, isCopy, mathEleEvent);
    }

    //====================================Getters & Setters=========================================

    public static MathElementConstraintLayout getMainLayout() {
        return mainLayout;
    }

    public static void setMainLayout(MathElementConstraintLayout mainLayout) {
        MathElement.mainLayout = mainLayout;
    }

    public static void setWorkspaceLayout(MathElementConstraintLayout workspaceLayout) {
        MathElement.workspaceLayout = workspaceLayout;
    }


    public ImageView getEleImg() {
        return eleImg;
    }

    public static ArrayMap<String, MathElement> getMathEleList() {
        return mathEleList;
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

    public Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "MathElement{" +
                "eleImg=" + eleImg +
                ", neighbouringMathEleList=" + neighbouringMathEleList +
                ", id=" + id +
                ", waitDouble=" + waitDouble +
                ", name='" + name + '\'' +
                ", initialElePosX=" + initialElePosX +
                ", initialElePosY=" + initialElePosY +
                ", context=" + context +
                ", isDropped=" + isDropped +
                ", horizontalBias=" + horizontalBias +
                ", verticalBias=" + verticalBias +
                ", initialTouchX=" + initialTouchX +
                ", initialTouchY=" + initialTouchY +
                ", params=" + params +
                ", windowManager=" + windowManager +
                ", mainLayoutAbsPos=" + Arrays.toString(mainLayoutAbsPos) +
                ", workspaceAbsPos=" + Arrays.toString(workspaceAbsPos) +
                ", eventReceived=" + eventReceived +
                ", shadowName='" + shadowName + '\'' +
                ", isRemoteExecution=" + isRemoteExecution +
                ", eventAction=" + eventAction +
                ", appId='" + appId + '\'' +
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
    //===================================End of Getters & Setters===================================

    //=========================================Helpers==============================================
    @SuppressLint("ClickableViewAccessibility")
    private void setupElement(Context context, Drawable eleImgDrawable, float elePosX, float elePosY,
                              float srcImgWidth, float srcImgHeight, String eleName, int parentId,
                              MathElementConstraintLayout workspaceLayout, MathElementConstraintLayout mainLayout,
                              boolean isCopy, @Nullable HashMap<String, Object> mathEleEvent) {

        this.context = context;
        if (isCopy) {
            MathElement mathElement = mathEleList.get(eleName);
            eleImg = new ImageView(this.context);
            ConstraintLayout.LayoutParams lp =
                    new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT);
            lp.width = mathElement.getEleImg().getWidth();
            lp.height = mathElement.getEleImg().getHeight();
            eleImg.setLayoutParams(lp);
            if (mathEleEvent != null) {
                String eleNameCopy = (String) mathEleEvent.get(REMOTE_COPY_NAME);
                if (eleNameCopy != null) eleName = eleNameCopy;
                else try {
                    throw new Exception("remote copy name is missing from remote data");
                } catch (Exception e) {
                    Log.e(TAG, "setupElement: error: " + Log.getStackTraceString(e));
                }
            } else {
                eleName = eleName + "_COPY_" + parentId + "_" + getUniqueId();
                eventToSend.put(REMOTE_COPY_NAME, eleName); // used for renaming elements created remotely
            }
            eleImg.setTag(eleName);
        }
        eleImg.setBackground(context.getDrawable(R.drawable.custom_border));
        eleImg.setImageDrawable(eleImgDrawable);
        this.name = eleName;

        //set up click options for trash element
        if (eleName.equalsIgnoreCase(TRASH)) {

            MathElement mathElement = mathEleList.get(TRASH_SHADOW);
            mathElement.getEleImg().post(() -> {
                ConstraintLayout.LayoutParams lp =
                        new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT);
                lp.width = mathElement.getEleImg().getWidth()*2;
                lp.height = mathElement.getEleImg().getHeight()*2;
                eleImg.setLayoutParams(lp);
                rePositionMathEle(workspaceLayout,1f,1f);
            });

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
                    removeAllGeneratedElements();
                    dispatchRemoteEvent(MathMotionEvent.DOUBLE_CLICK);
                }
            });
        }

        //setup drag and drop options on image
        eleImg.setOnTouchListener((view, motionEvent) -> {

            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            int action = motionEvent.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mActivePointerId = motionEvent.getPointerId(0);
                    int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (pointerIndex != INVALID_POINTER_ID) {
                        if (!isCopy && !name.equalsIgnoreCase(TRASH)) {
                            triggerDragShadowEvents(rawX, rawY, MathMotionEvent.PRESS_DOWN, 0f, 0f,
                                    false);
                        }
                        dispatchRemoteEvent(MathMotionEvent.PRESS_DOWN);
                    }
                }

                break;
                case MotionEvent.ACTION_MOVE: {

                    int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (pointerIndex != INVALID_POINTER_ID) {
                        if (isCopy || name.equalsIgnoreCase(TRASH)) {
                            float x = rawX - workspaceAbsPos[0];
                            float y = rawY - workspaceAbsPos[1];
                            rePositionMathEle(this, x, y, workspaceLayout);
                        } else {
                            triggerDragShadowEvents(rawX, rawY, MathMotionEvent.MOVE_AROUND, 0f, 0f,
                                    false);
                        }
                        dispatchRemoteEvent(MathMotionEvent.MOVE_AROUND);
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    if (isCopy || name.equalsIgnoreCase(TRASH)) {
                        resetFocusedMathEleList();
                        magPositionElement();
                        learnNeighbouringElements();
                        dispatchRemoteEvent(MathMotionEvent.LIFT_UP);
                    } else {

                        triggerDragShadowEvents(rawX, rawY, MathMotionEvent.LIFT_UP, 0f, 0f,
                                false);
                        new Thread(() -> {
                            try {
                                synchronized (eventAction) {
                                    eventAction.wait(); //wait until the event is finished.
                                    dispatchRemoteEvent(MathMotionEvent.LIFT_UP);
                                }
                            } catch (InterruptedException e) {
                                Log.e(TAG, "setupElement: move action error: "
                                        + Log.getStackTraceString(e));
                            }
                        }).start();
                    }
                    if (name.equalsIgnoreCase(TRASH))
                        view.performClick(); //enable click functions as well on element
                }
                break;
                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    eleActionPosQueue.clear();
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
        MathElement.mainLayout = mainLayout;
        MathElement.workspaceLayout = workspaceLayout;
        neighbouringMathEleList = new SparseArray<>();
        id = getUniqueId();
        mainLayoutAbsPos = new int[2];
        workspaceAbsPos = new int[2];
        workspaceLayout.post(() -> workspaceLayout.getLocationInWindow(workspaceAbsPos));
        mainLayout.post(() -> mainLayout.getLocationInWindow(mainLayoutAbsPos));
        mathEleList.put(name, this);
    }


    @SuppressLint("ClickableViewAccessibility")
    public void triggerDragShadowEvents(float rawX, float rawY, int action, Float hbias, Float vbias,
                                        boolean isRemoteEvent) {
//        boolean doExecute;
//       if (isRemoteExecution&& !isRemoteEvent) {
//           doExecute = false;
//           Log.d(TAG, "triggerDragShadowEvents: doExecute" + doExecute);
//       }
//       else {
//           doExecute = true;
//           Log.d(TAG, "triggerDragShadowEvents: doExecute" + doExecute);
//
//       }
//
//        if (doExecute) {
        switch (action) {
            case MathMotionEvent.PRESS_DOWN: {

                //create shadow
                if (statusBarHeight == 0) {
                    statusBarHeight = getStatusBarHeight();
                }

                ImageView shadowImage = new ImageView(context);
                shadowImage.setBackground(context.getDrawable(R.drawable.custom_border));
                shadowImage.setImageDrawable(eleImg.getDrawable());
                shadowImage.setImageAlpha(51);
                shadowImage.setId(View.generateViewId());
                ConstraintLayout.LayoutParams layoutParams =
                        new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.width = eleImg.getWidth();
                layoutParams.height = eleImg.getHeight();
                shadowImage.setLayoutParams(layoutParams);

                MathElement shadowElement = MathElementFactory.getNewInstance(this.context,
                        shadowImage, shadowName, 0, workspaceLayout, mainLayout,
                        false, null);
                mainLayout.addView(shadowImage);
                if (!isRemoteEvent) {
                    float x = rawX - mainLayoutAbsPos[0];
                    float y = rawY - mainLayoutAbsPos[1];
                    rePositionMathEle(shadowElement, x, y, mainLayout);
                    initialTouchX = rawX;
                    initialTouchY = rawY;
                } else {
                    rePositionMathEle(shadowElement, mainLayout, hbias, vbias);
                    Log.d(TAG, "triggerDragShadowEvents: down hbias: " + hbias + " vbias:" + vbias);
                    initialTouchX = eleImg.getX() + mainLayoutAbsPos[0];
                    initialTouchY = eleImg.getY() + mainLayoutAbsPos[1];
                }
                sendDragEvent((int) initialTouchX, (int) initialTouchY, MathMotionEvent.PRESS_DOWN,
                        this, isRemoteEvent);
            }

            break;

            case MathMotionEvent.LIFT_UP: {
                MathElement shadowElement = mathEleList.get(shadowName);
                ImageView shadowImage = shadowElement.getEleImg();
                shadowImage.setImageAlpha(255);
                if (isRemoteEvent) {
                    rawX = shadowImage.getX() + mainLayoutAbsPos[0] + shadowImage.getWidth() / 2;
                    rawY = shadowImage.getY() + mainLayoutAbsPos[1] + shadowImage.getHeight() / 2;
                }
                sendDragEvent((int) rawX, (int) rawY, MathMotionEvent.DROP, this, isRemoteEvent);
                sendDragEvent((int) rawX, (int) rawY, MathMotionEvent.LIFT_UP, this, isRemoteEvent);
                mainLayout.removeView(shadowImage);
                mathEleList.remove(shadowElement);
            }
            break;

            case MathMotionEvent.MOVE_AROUND: {
                //this code is helping the widget to move around the screen with fingers
                MathElement shadowElement = mathEleList.get(shadowName);
                if (!isRemoteEvent) {
                    float x = rawX - mainLayoutAbsPos[0];
                    float y = rawY - mainLayoutAbsPos[1];
                    rePositionMathEle(shadowElement, x, y, mainLayout);
                } else {
                    ImageView shadowImage = shadowElement.getEleImg();
                    rePositionMathEle(shadowElement, mainLayout, hbias, vbias);
                    rawX = shadowImage.getX() + mainLayoutAbsPos[0];
                    rawY = shadowImage.getY() + mainLayoutAbsPos[1];
                }
                sendDragEvent((int) rawX, (int) rawY, MathMotionEvent.MOVE_AROUND, this,
                        isRemoteEvent);
            }
            break;
            default:
        }
    }
//    }
    /*
    public ImageView processShadow1(MotionEvent motionEvent) {

        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                //create shadow
                shadowImage = new ImageView(this.context);
                shadowImage.setBackground(context.getDrawable(R.drawable.custom_border));
                shadowImage.setImageDrawable(eleImg.getDrawable());
                shadowImage.setImageAlpha(51);
                shadowImage.setId((int) getUniqueId());

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    params = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_PHONE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT);
                } else {
                    params = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT);
                }

                //getting windows services and adding the floating view to it
                windowManager = (WindowManager) this.context.getSystemService(WINDOW_SERVICE);
                params.gravity = Gravity.TOP | Gravity.LEFT;
                int[] coords = new int[2];
                if (statusBarHeight == 0) {
                    statusBarHeight = getStatusBarHeight();
                }
                eleImg.getLocationInWindow(coords);
                params.x = coords[0];
                params.y = coords[1] - statusBarHeight;
                windowManager.addView(shadowImage, params);

                initialX = params.x;
                initialY = params.y;
                initialTouchX = rawX;
                initialTouchY = rawY;
                sendDragEvent((int) initialTouchX, (int) initialTouchY,
                        MathMotionEvent.PRESS_DOWN, this, isRemoteEvent);

                break;

            case MotionEvent.ACTION_UP:
                sendDragEvent((int) rawX, (int) rawY, MathMotionEvent.DROP, this, isRemoteEvent);
                sendDragEvent((int) rawX, (int) rawY, MathMotionEvent.LIFT_UP, this, isRemoteEvent);
                shadowImage.setImageAlpha(255);
                windowManager.removeViewImmediate(shadowImage);
                break;

            case MotionEvent.ACTION_MOVE:
                //this code is helping the widget to move around the screen with fingers
                params.x = initialX + (int) (rawX - initialTouchX);
                params.y = initialY + (int) (rawY - initialTouchY);
                windowManager.updateViewLayout(shadowImage, params);
                sendDragEvent((int) rawX, (int) rawY, MathMotionEvent.MOVE_AROUND, this, isRemoteEvent);
                break;
            default:
        }
        return shadowImage;
    }

     */

    public static void setupCallBackOnWorkspace() {
        workspaceLayout.setOnShadowDragEventListener((view, shadowDragEvent) -> {

            MathElement mathElement = (MathElement) shadowDragEvent.getData();
            Activity activity = (Activity) mathElement.getContext();
            activity.runOnUiThread(() -> {
                switch (shadowDragEvent.getAction()) {

                    case DragEvent.ACTION_DROP:
                        MathElement mathEleCopy;
                        HashMap<String, Object> eventReceived =
                                shadowDragEvent.isRemoteEvent() ? mathElement.getEventReceived() : null;
                        mathEleCopy = mathElement.dropElement(
                                eventReceived,
                                shadowDragEvent.getX(),
                                shadowDragEvent.getY(),
                                workspaceLayout);
                        mathEleCopy.magPositionElement();
                        mathEleCopy.learnNeighbouringElements();
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        if (!shadowDragEvent.isRemoteEvent()) {

                            //notify the wait to send event data remotely on motion action up
                            synchronized (mathElement.eventAction) {
                                mathElement.eventAction.notify();
                            }
                        }
                        break;
                }
            });
        });
    }

    public int getStatusBarHeight() {
        Rect r = new Rect();
        Window w = ((MainActivity) context).getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        return r.top;
    }

    public int getTitleBarHeight() {
        int viewTop = ((MainActivity) context).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return (viewTop - getStatusBarHeight());
    }

    private void sendDragEvent(int x, int y, int action) {
        ShadowDragEvent shadowDragEvent = new ShadowDragEvent(x, y, action);
        sendDragEventHelper(shadowDragEvent);
    }

    private void sendDragEvent(int x, int y, int action, Object data, boolean isRemoteEvent) {
        ShadowDragEvent shadowDragEvent = new ShadowDragEvent(x, y, data, action, isRemoteEvent);
        sendDragEventHelper(shadowDragEvent);
    }

    private void sendDragEventHelper(ShadowDragEvent shadowDragEvent) {
        shadowDragEvent.setName(this.name);
        MathElementConstraintLayout.shadowDragEvents.add(shadowDragEvent);
        synchronized (MathElementConstraintLayout.eventAction) {
            MathElementConstraintLayout.eventAction.notify();
        }
    }

    public void removeAllGeneratedElements() {
        Iterator iterator = getMathEleList().entrySet().iterator();
        while (iterator.hasNext()) {
            ArrayMap.Entry<String, MathElement>
                    element = (ArrayMap.Entry<String, MathElement>) iterator.next();
            String elementKey = element.getKey();
            if (elementKey.contains("COPY")) {
                MathElement mathElement = element.getValue();
                MathElement.this.workspaceLayout.removeView(mathElement.getEleImg());
                iterator.remove();
            }
        }
    }

    public MathElement dropElement(float x, float y) {
        return dropElement(x, y, workspaceLayout);
    }

    public MathElement dropElement(float x, float y, ConstraintLayout layout) {
        return dropElement(null, x, y, layout);
    }

    public MathElement dropElement(@Nullable HashMap<String, Object> eventReceived, float x, float y,
                                   ConstraintLayout constraintLayout) {

        // Gets the text data from the item.
        String name = this.getName();
        ImageView eleImg;
        MathElement mathEleCopy;

        if (!name.contains("COPY")) { //drop copy

            mathEleCopy = getMathEleCopy(x, y, eventReceived);
            eleImg = mathEleCopy.getEleImg();
            constraintLayout.addView(eleImg);
            this.rePositionMathEle(mathEleCopy, x, y, workspaceLayout);
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
     * @param layout
     */
    public void rePositionMathEle(MathElement mathEle, float dropPosX, float dropPosY,
                                  ConstraintLayout layout) {
        rePositionMathEleHelper(mathEle, dropPosX, dropPosY, layout, null, null);
    }

    public void rePositionMathEle(ConstraintLayout layout, Float horizontalBias, Float verticalBias) {
        rePositionMathEleHelper(this, null, null, layout, horizontalBias, verticalBias);
    }

    public void rePositionMathEle(MathElement mathElement,
                                  ConstraintLayout layout, Float horizontalBias, Float verticalBias) {
        rePositionMathEleHelper(mathElement, null, null, layout, horizontalBias, verticalBias);
    }

    private void rePositionMathEleHelper(MathElement mathEle,
                                         @Nullable Float dropPosX,
                                         @Nullable Float dropPosY,
                                         ConstraintLayout layout,
                                         @Nullable Float horizontalBias,
                                         @Nullable Float verticalBias) {

        ImageView dropEleImg = mathEle.getEleImg();

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.RIGHT, layout.getId(),
                ConstraintSet.RIGHT, 0);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.TOP, layout.getId(),
                ConstraintSet.TOP, 0);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.LEFT, layout.getId(),
                ConstraintSet.LEFT, 0);
        constraintSet.connect(dropEleImg.getId(), ConstraintSet.BOTTOM, layout.getId(),
                ConstraintSet.BOTTOM, 0);

        //adjust element within the constraint layout considering a half width and height
        //adjustment so that element will be centered on drop point.

        int eleImgWidth = dropEleImg.getWidth();
        int eleImgHeight = dropEleImg.getHeight();
        int eleImgHalfHeight = eleImgHeight / 2;
        int eleImgHalfWidth = eleImgWidth / 2;
        int layoutConstraintWidth = layout.getWidth() - dropEleImg.getWidth();
        int layoutConstraintHeight = layout.getHeight() - dropEleImg.getHeight();

        dropPosX = dropPosX == null ? 0 : dropPosX;
        this.horizontalBias = horizontalBias == null ?
                (dropPosX - eleImgHalfWidth) / layoutConstraintWidth : horizontalBias;

        if (this.horizontalBias < 0) this.horizontalBias = 0;
        else if (this.horizontalBias > 1) this.horizontalBias = 1;
        constraintSet.setHorizontalBias(dropEleImg.getId(), this.horizontalBias);

        dropPosY = dropPosY == null ? 0 : dropPosY;
        this.verticalBias = verticalBias == null ?
                (dropPosY - eleImgHalfHeight) / layoutConstraintHeight : verticalBias;

        if (this.verticalBias < 0) this.verticalBias = 0;
        else if (this.verticalBias > 1) this.verticalBias = 1;
        constraintSet.setVerticalBias(dropEleImg.getId(), this.verticalBias);
        constraintSet.applyTo(layout);

        //offset drop element image coordinates by half the width  and height to compensate for
        // half width and height adjustment which takes place above to center element on the point
        // on which it drops.
        dropEleImg.setLeft(dropEleImg.getLeft() - eleImgHalfWidth);
        dropEleImg.setTop(dropEleImg.getTop() - eleImgHalfHeight);
        dropEleImg.setRight(dropEleImg.getRight() - eleImgHalfWidth);
        dropEleImg.setBottom(dropEleImg.getBottom() - eleImgHalfHeight);
    }

    //todo:Fix this method
    public void resetMathElePosition() {
        //calculate change of distance from initial position
        ImageView eleImg = this.eleImg;
        float x = this.getInitialElePosX() - eleImg.getX();
        float y = this.getInitialElePosY() - eleImg.getY();
        rePositionMathEle(this, x, y, (ConstraintLayout) eleImg.getParent());
    }

    /**
     * Re-positions this math element from freely positioned location by user to a fixed appropriate
     * position determined by the system.
     */
    public void magPositionElement() {
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


    public void dispatchRemoteEvent(int action) {

        EleActionPos eleActionPos = new EleActionPos();
        eleActionPos.hBias = this.horizontalBias;
        eleActionPos.vBias = this.verticalBias;
        eleActionPos.action = action;
        eventToSend.put(ELE_NAME, this.name);
        eleActionPosQueue.add(eleActionPos);
        sendElementPosData();
    }


    public static void sendElementPosData() {
        Gson gson = new Gson();
        String elePosList = gson.toJson(eleActionPosQueue,
                new TypeToken<ArrayList<EleActionPos>>() {
                }.getType());
        eventToSend.put(ELE_POS_LIST, elePosList);
        eventToSend.put(APP_ID, DragAndDropMathApplication.APPLICATION_ID);

        String remoteDataString = gson.toJson(eventToSend,
                new TypeToken<HashMap<String, Object>>() {
                }.getRawType());

        DragAndDropMathApplication.mStompClient.send(sendMathElementURI, remoteDataString).subscribe();
        eleActionPosQueue.clear();
    }

    private MathElement getMathEleCopy(float x, float y, @Nullable HashMap<String, Object> eventReceived) {

        Drawable drawable = eleImg.getDrawable();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        MathElement mathElement = MathElementFactory.getNewInstance(context, drawable, x, y,
                (float) width, (float) height, this.name, this.mainLayout.getId(), this.workspaceLayout,
                this.mainLayout, true, eventReceived);
        return mathElement;
    }

    private void removeMathElement() {
        this.workspaceLayout.removeView(this.getEleImg());
        mathEleList.remove(this.name);
    }

    public void resetFocusedMathEleList() {
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
//                this.positionMathEle(true);
                this.resetMathElePosition();
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
        this.rePositionMathEle(this, xloc, yLoc, workspaceLayout);
    }

    private void placeToBottom(MathElement focusedEle, Rect focusedEleRect) {

        int xloc = focusedEleRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = focusedEleRect.bottom + focusedEle.getEleImg().getHeight() / 4;
        this.rePositionMathEle(this, xloc, yLoc, workspaceLayout);
    }

    private void placeToTop(MathElement focusedEle, Rect focusedEleRect) {

        int xloc = focusedEleRect.left + focusedEle.getEleImg().getWidth() / 2;
        int yLoc = focusedEleRect.top - focusedEle.getEleImg().getHeight() / 4;
        this.rePositionMathEle(this, xloc, yLoc, workspaceLayout);
    }

    private void placeToLeft(MathElement focusedEle, Rect focusedEleRect) {

        int width = focusedEle.getEleImg().getWidth();
        int xloc = focusedEleRect.left - width / 4;
        int yLoc = focusedEleRect.top + focusedEle.getEleImg().getHeight() / 2;
        this.rePositionMathEle(this, xloc, yLoc, workspaceLayout);
    }

    private MathElement getClosestNeighbouringMathEle() {

        double shortestDis = Integer.MAX_VALUE;
        MathElement focusedEle = null;

        int dropEleLeft = this.getEleImg().getLeft();
        int dropEleTop = this.getEleImg().getTop();

        for (ArrayMap.Entry<String, MathElement> entry : mathEleList.entrySet()) {
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
            for (ArrayMap.Entry<String, MathElement> entry : mathEleList.entrySet()) {
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

    private int getNavigationBarHeight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    public void setEventReceived(HashMap<String, Object> eventReceived) {
        this.eventReceived = eventReceived;
    }

    public HashMap<String, Object> getEventReceived() {
        return eventReceived;
    }
    //========================================End of Helpers========================================

    //=====================================Inner Classes============================================
    class MathMotionEvent {

        public static final int PRESS_DOWN = 100;
        public static final int LIFT_UP = 101;
        public static final int MOVE_AROUND = 102;
        public static final int DOUBLE_CLICK = 103;
        public static final int SINGLE_CLICK = 104;
        public static final int DROP = 105;

    }
    //=====================================End of Inner Classes============================================
}
