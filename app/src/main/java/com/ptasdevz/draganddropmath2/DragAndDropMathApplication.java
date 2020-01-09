package com.ptasdevz.draganddropmath2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

import static com.ptasdevz.draganddropmath2.MathElement.APP_ID;
import static com.ptasdevz.draganddropmath2.MathElement.ELE_NAME;
import static com.ptasdevz.draganddropmath2.MathElement.ELE_POS_LIST;
import static com.ptasdevz.draganddropmath2.MathElement.TRASH;
import static com.ptasdevz.draganddropmath2.MathElement.eventQueue;

public class DragAndDropMathApplication {

    public static final String TAG = "ptasdevz";
//    public static final String ENDPOINT_WEBSOCKET = "ws://192.168.137.1:8181/endpoint/websocket";
    public static final String ENDPOINT_WEBSOCKET = "ws://35.239.154.165:8181/endpoint/websocket";
    public static final String TOPIC_PATH = "/topic/math-element-message";
    public static StompClient mStompClient;
    public static String APPLICATION_ID;
    public static Object remoteEventAction = new Object();
    private static DragAndDropMathApplication instance;
    private Gson gson = new Gson();

    private DragAndDropMathApplication() { }

    public static DragAndDropMathApplication getInstance() {
        if (instance == null) instance = new DragAndDropMathApplication();
        return instance;
    }

    @SuppressLint("CheckResult")
    public void setupStompConnection() {
        try {
            DragAndDropMathApplication.mStompClient = Stomp.over(
                    Stomp.ConnectionProvider.OKHTTP, ENDPOINT_WEBSOCKET);
            DragAndDropMathApplication.mStompClient.connect();
            DragAndDropMathApplication.mStompClient.topic(TOPIC_PATH)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        String payload = topicMessage.getPayload();

                        Type mapType = new TypeToken<HashMap<String, Object>>() {
                        }.getType();
                        HashMap<String, Object> remoteDataMap = gson.fromJson(payload, mapType);
                        eventQueue.add(remoteDataMap);
                        synchronized (remoteEventAction) {
                            remoteEventAction.notify();
                        }

                    }, throwable -> {
                        Log.e(DragAndDropMathApplication.TAG, "Error on subscribe topic", throwable);
                    });

            DragAndDropMathApplication.mStompClient.lifecycle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(lifecycleEvent -> {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                Log.d(DragAndDropMathApplication.TAG, "Stomp connection opened");
                                break;
                            case CLOSED:
                                Log.d(DragAndDropMathApplication.TAG, "Stomp connection closed");
                                break;
                            case ERROR:
                                Log.e(DragAndDropMathApplication.TAG, "Stomp connection error",
                                        lifecycleEvent.getException());
                                break;
                        }
                    }, throwable -> {
                        Log.e(DragAndDropMathApplication.TAG, "Error on subscribe lifecycle",
                                throwable);
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupRemoteMotion(Context context) {
        Thread thread = new Thread(new MathEleMotionEvent(context));
        thread.start();
    }

    //=====================================Inner Classes============================================
    class MathEleMotionEvent implements Runnable {
        protected Context context;
        private Activity activity;


        public MathEleMotionEvent(Context context) {
            this.context = context;
            this.activity = (Activity) context;
        }

        @Override
        public void run() {
            try {
                Type elePosType = new TypeToken<ArrayList<EleActionPos>>() {
                }.getType();

                while (true) {

                    HashMap<String, Object> eventReceived = eventQueue.poll();
                    if (eventReceived != null) {
                        String appId = (String) eventReceived.get(APP_ID);
                        if (!DragAndDropMathApplication.APPLICATION_ID.equalsIgnoreCase(appId)) {

                            String elementPosLisStr = (String) eventReceived.get(ELE_POS_LIST);

                            ArrayList<EleActionPos> eleActionPosArrayList =
                                    gson.fromJson(elementPosLisStr, elePosType);

                            String eleName = (String) eventReceived.get(ELE_NAME);
                            MathElement mathElement = MathElement.mathEleList.get(eleName);
                            if (mathElement != null) {

                                for (EleActionPos eleActionPos : eleActionPosArrayList) {
                                    activity.runOnUiThread(new MathEleMotionEventActions(mathElement,
                                            eleActionPos, eventReceived));
                                }
                            }
                        }
                    } else {
                        synchronized (remoteEventAction) {
                            remoteEventAction.wait();
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "run: remote event thread had an error: " + Log.getStackTraceString(e));
            }
        }
    }

    class MathEleMotionEventActions implements Runnable {


        private final MathElement mathElement;
        private final EleActionPos eleActionPos;
        private final HashMap<String, Object> eventReceived;

        public MathEleMotionEventActions(MathElement mathElement,
                                         EleActionPos eleActionPos, HashMap<String, Object> eventReceived) {
            this.mathElement = mathElement;
            this.eleActionPos = eleActionPos;
            this.eventReceived = eventReceived;
        }

        @Override
        public void run() {
//            mathElement.isRemoteExecution = true;
            switch (eleActionPos.action) {
                case MathElement.MathMotionEvent.PRESS_DOWN: {
                    String name = (String) eventReceived.get(ELE_NAME);
                    if (!name.contains("COPY") && !name.equalsIgnoreCase(TRASH))
                        mathElement.triggerDragShadowEvents(0f, 0f, eleActionPos.action
                                , eleActionPos.hBias, eleActionPos.vBias, true);

                }
                break;
                case MathElement.MathMotionEvent.MOVE_AROUND: {
                    String name = (String) eventReceived.get(ELE_NAME);

                    if (name.contains("COPY") || name.equalsIgnoreCase(TRASH)) {
                        mathElement.rePositionMathEle((ConstraintLayout)
                                mathElement.getEleImg().getParent(), eleActionPos.hBias,
                                eleActionPos.vBias);
                    } else {
                        mathElement.triggerDragShadowEvents(0f, 0f, eleActionPos.action,
                                eleActionPos.hBias, eleActionPos.vBias, true);
                    }
                }
                break;
                case MathElement.MathMotionEvent.LIFT_UP: {
                    if (mathElement.getName().contains("COPY")
                            || mathElement.getName().contains(TRASH)) {
                        mathElement.magPositionElement();
                        mathElement.learnNeighbouringElements();
                    } else {
                        mathElement.setEventReceived(eventReceived);
                        mathElement.triggerDragShadowEvents(0f, 0f, eleActionPos.action,
                                eleActionPos.hBias, eleActionPos.vBias, true);
                    }

//                    mathElement.isRemoteExecution = false;
                }
                break;
                case MathElement.MathMotionEvent.SINGLE_CLICK: {

                }
                break;
                case MathElement.MathMotionEvent.DOUBLE_CLICK: {
                    String name = mathElement.getName();
                    if (name.equalsIgnoreCase(TRASH)) {
                        mathElement.removeAllGeneratedElements();
                    }
                }
                break;
            }

        }
    }
}
