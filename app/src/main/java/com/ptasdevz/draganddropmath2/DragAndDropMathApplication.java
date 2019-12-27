package com.ptasdevz.draganddropmath2;

import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

import ua.naiksoftware.stomp.StompClient;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class DragAndDropMathApplication {

    public static int mActivePointerId = INVALID_POINTER_ID;
    public static StompClient mStompClient;
    public static String APPLICATION_ID;
//    public static boolean isRemoteMotionEvent;
    public static final String TAG = "ptasdevz";

    private static MathElementRemote elementRemote;


    public static MathElementRemote getElementRemote() {
        return elementRemote;
    }

    public static void actionMoveRemoteOptions1(MathElement mathEleOriginal, int pointerIndex,
                                                float evX, float evY) {

        elementRemote.remoteAction = MotionEvent.ACTION_MOVE;
        elementRemote.motionPositionX = evX;
        elementRemote.motionPositionY = evY;
        elementRemote.pointerIndex = pointerIndex;
        elementRemote.name = mathEleOriginal.getName();
        ConstraintLayout parent = (ConstraintLayout) mathEleOriginal.getEleImg().getParent();
        elementRemote.parentWidth = parent.getWidth();
        elementRemote.parentHeight = parent.getHeight();

        String payload = MathElementRemote.toJsonString(elementRemote);
//        Log.d(TAG, "onCreate: payload move " + payload);
        mStompClient.send(MathElement.sendMathElementURI, payload).subscribe();
    }

    public static void actionDownRemoteOptions1(MathElement mathEleOriginal, float evX, float evY) {

        elementRemote.remoteAction = MotionEvent.ACTION_DOWN;
        elementRemote.name = mathEleOriginal.getName();
        elementRemote.appId = APPLICATION_ID;
        elementRemote.motionPositionX = evX;
        elementRemote.motionPositionY = evY;
        ConstraintLayout parent = (ConstraintLayout) mathEleOriginal.getEleImg().getParent();
        elementRemote.parentWidth = parent.getWidth();
        elementRemote.parentHeight = parent.getHeight();

        String payload = MathElementRemote.toJsonString(elementRemote);
//        Log.d(TAG, "onCreate: payload down" + payload);
        mStompClient.send(MathElement.sendMathElementURI, payload).subscribe();
    }

    public static void actionUpRemoteOptions(MathElement mathEleOriginal) {
        elementRemote.remoteAction = MotionEvent.ACTION_UP;
        elementRemote.name = mathEleOriginal.getName();
        String payload = MathElementRemote.toJsonString(elementRemote);
        mStompClient.send(MathElement.sendMathElementURI, payload).subscribe();
    }

}
