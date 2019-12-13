package com.ptasdevz.draganddropmath2;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;
import java.util.Iterator;

import static com.ptasdevz.draganddropmath2.MainActivity.TAG;
import static com.ptasdevz.draganddropmath2.MainActivity.getMathEleList;
import static com.ptasdevz.draganddropmath2.MainActivity.getOppositeElePos;
import static com.ptasdevz.draganddropmath2.MainActivity.getUniqueId;
import static com.ptasdevz.draganddropmath2.MainActivity.mathElePosList;


public class MathElement {

    private ImageView eleImg;
    private ConstraintLayout eleImgLayout;
    private SparseArray<MathElement> focusedMathEleList;
    private ElementPos lastPos;
    private ElementPos currPos;
    private long id;
    public static MathElement draggedElement;
    private boolean waitDouble = true;
    private static final int DOUBLE_CLICK_TIME = 250; // double click timer

    public static int getEleBoxScaleFactor() {
        return eleBoxScaleFactor;
    }

    private static int eleBoxScaleFactor = 2;
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


    public  static HashMap<String,Integer> MathElementsNameRes = new HashMap<>();
    static {
        MathElementsNameRes.put(NUMBER_0,R.id.number0Img);
        MathElementsNameRes.put(NUMBER_1,R.id.number1Img);
        MathElementsNameRes.put(NUMBER_2,R.id.number2Img);
        MathElementsNameRes.put(NUMBER_3,R.id.number3Img);
        MathElementsNameRes.put(NUMBER_4,R.id.number4Img);
        MathElementsNameRes.put(NUMBER_5,R.id.number5Img);
        MathElementsNameRes.put(NUMBER_6,R.id.number6Img);
        MathElementsNameRes.put(NUMBER_7,R.id.number7Img);
        MathElementsNameRes.put(NUMBER_8,R.id.number8Img);
        MathElementsNameRes.put(NUMBER_9,R.id.number9Img);
        MathElementsNameRes.put(MINUS,R.id.minusImg);
        MathElementsNameRes.put(PLUS,R.id.plusImg);
        MathElementsNameRes.put(EQUAL,R.id.equalImg);
        MathElementsNameRes.put(MULTIPLY,R.id.multiplyImg);
        MathElementsNameRes.put(DIVIDE,R.id.divideImg);
    }


    @SuppressLint("ClickableViewAccessibility")
    public MathElement(final Context context, Drawable eleImgDrawable, float elePosX, float elePosY,
                       float srcImgWidth, float srcImgHeight, String eleName, int parentId, ConstraintLayout layout) {

        eleImg = new ImageView(context);
        eleImg.setImageDrawable(eleImgDrawable);
        eleImg.setMaxHeight((int) srcImgHeight);
        eleImg.setMaxWidth((int) srcImgWidth);
        String tag = eleName + "_COPY_" + parentId + "_" + getUniqueId();
        eleImg.setTag(tag);
        final MathElePos mathElePos = new MathElePos();
        mathElePosList.put(tag, mathElePos);
        mathElePos.name = tag;
        if (eleName.equalsIgnoreCase(TRASH)){
            eleImg.setOnLongClickListener(view -> {
                MathElement.draggedElement = MathElement.this;
                return true;
            });
            eleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    if (waitDouble == true){

                        waitDouble = false;
                        Thread thread = new Thread(){
                            @Override
                            public void run() {
                                try {
                                    sleep(DOUBLE_CLICK_TIME);
                                    if (waitDouble == false){
                                        waitDouble = true;
                                        singleClick(view.getContext());
                                        Toast.makeText(view.getContext(), "drag item to bin or double click to clear worksheet", Toast.LENGTH_SHORT).show();

                                    }
                                }catch (Exception e){

                                }
                            }


                        };
                        thread.start();

                    }
                    else {
                        waitDouble = true;
                        doubleClick();
                    }
                }
                public void singleClick(Context c) {
                    Log.d(TAG,"Single click");

                }
                private void doubleClick() {
                    Log.d(TAG,"Double click");
                    Iterator iterator = getMathEleList().entrySet().iterator();
                    while (iterator.hasNext()){
                        HashMap.Entry<String,MathElement> element = (HashMap.Entry<String, MathElement>) iterator.next();
                        String elementKey = element.getKey();
                        if (!elementKey.contains(TRASH)) {
                            MathElement mathElement = element.getValue();
                            MathElement.this.getEleImgLayout().removeView(mathElement.getEleImg());
                            iterator.remove();
                        }
                    }
                }
            });
        }else {
            eleImg.setOnTouchListener((view, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        MainActivity.setupActionDownOptions(mathElePos,view,motionEvent);
                        MathElement.draggedElement = MathElement.this;
                    }
                    break;
                    case MotionEvent.ACTION_MOVE:{
                        MainActivity.setupActionMoveOptions(mathElePos, view, motionEvent);
                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        view.performClick();
                        Log.d(TAG, "touch is released.");
                    }
                    break;
                }
                return true;
            });
        }


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

    public ImageView getEleImg() {
        return eleImg;
    }

    public ConstraintLayout getEleImgLayout() {
        return eleImgLayout;
    }

    /**
     * Adds a MathElement as a focused element to this MathElement.
     * @param pos
     * @param focusElement
     */
    public void addFocusedMathEle(int pos, MathElement focusElement){
        SparseArray<MathElement> focusedMathEleList = focusElement.getFocusedMathEleList();

        //check to see if both math elements are currently focused elements of each other. if so remove.
        int index = focusedMathEleList.indexOfValue(this);
        if (index>=0) focusedMathEleList.removeAt(index);
        int index1 = this.focusedMathEleList.indexOfValue(focusElement);
        if (index1>=0) this.focusedMathEleList.removeAt(index1);

        //Add each element as a focus element of each other in opposite positions
        focusedMathEleList.put(getOppositeElePos(pos),this);
        this.focusedMathEleList.put(pos,focusElement);

        twoStepCheck(focusElement,pos);
    }

    private void twoStepCheck(MathElement focusElement, int pos) {
        Log.d(TAG, "twoStepCheck before: "+ focusedMathEleList.toString());

        learnAdjacentElements(focusElement, pos, Constant.LEFT);
        learnAdjacentElements(focusElement, pos, Constant.TOP_LEFT);
        learnAdjacentElements(focusElement, pos, Constant.TOP);
        learnAdjacentElements(focusElement, pos, Constant.TOP_RIGHT);
        learnAdjacentElements(focusElement, pos, Constant.RIGHT);
        learnAdjacentElements(focusElement, pos, Constant.BOTTOM_RIGHT);
        learnAdjacentElements(focusElement, pos, Constant.BOTTOM);
        learnAdjacentElements(focusElement, pos, Constant.BOTTOM_LEFT);
        Log.d(TAG, "twoStepCheck after: "+ focusedMathEleList.toString());

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

    public MathElement getFocusedMathEle(int pos) {
        try {
            return focusedMathEleList.get(pos);
        }catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public SparseArray<MathElement> getFocusedMathEleList() {
        return focusedMathEleList;
    }
    public void resetFocusedMathEleList(){
        focusedMathEleList = new SparseArray<>();
    }

    public String  getImageTag() {
        return (String) eleImg.getTag();
    }

    public static void setUpDragOptions(View view) {

        Log.d(TAG, "Element is being setup");

//        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());

//        ClipData clipData = new ClipData((CharSequence) view.getTag()
//                , new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

        View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(view);

        //start drag
//        view.startDragAndDrop(clipData, dragShadowBuilder, null, 0);
    }

    public ElementPos getLastPos() {
        return lastPos;
    }

    public ElementPos getCurrPos() {
        return currPos;
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

}
