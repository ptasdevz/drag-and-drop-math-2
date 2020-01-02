package com.ptasdevz.draganddropmath2;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;

import static com.ptasdevz.draganddropmath2.MathElement.mathEleNameRes;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout workspaceLayout, mainLayout;
    public static final String TAG = "ptasdevz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DragAndDropMathApplication.APPLICATION_ID = Installation.id(MainActivity.this);
        DragAndDropMathApplication.getInstance().setupStompConnection();

        workspaceLayout = findViewById(R.id.workspace);
        mainLayout = findViewById(R.id.canvas);
        DragAndDropMathApplication.getInstance().setupRemoteMotion(this);

        //Set up all maths elements with drag and drop capabilities.
        /*
        for (HashMap.Entry<String, Integer> entry : mathEleNameRes.entrySet()) {

            String name = entry.getKey();
            int resId = entry.getValue();
            final ImageView mathEleSrcImg = findViewById(resId);

            MathElement mathElement = MathElementFactory.getNewInstance(this,
                    mathEleSrcImg, name, 0, workspaceLayout, mainLayout, false,
                    null);

            //wait for measurements to be ready
            mathEleSrcImg.post(() -> {
                mathElement.setInitialElePosX(mathEleSrcImg.getX());
                mathElement.setInitialElePosY(mathEleSrcImg.getY());
            });
        }
        
         */
    }
}
