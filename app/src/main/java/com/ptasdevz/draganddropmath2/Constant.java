package com.ptasdevz.draganddropmath2;

import java.util.ArrayList;

public class Constant {
    public static final int LEFT = 1;
    public static final int TOP_LEFT = 2;
    public static final int TOP = 3;
    public static final int TOP_RIGHT = 4;
    public static final int RIGHT = 5;
    public static final int BOTTOM_RIGHT = 6;
    public static final int BOTTOM = 7;
    public static final int BOTTOM_LEFT = 8;

    enum LayoutTypes  {
        MAIN,WORKSPACE
    }

    public static ArrayList<Integer> POSITIONS = new ArrayList();
    public static ArrayList<Integer> PLACEMENT_POSITIONS = new ArrayList();

    static {
        POSITIONS.add(Constant.LEFT);
        POSITIONS.add(Constant.TOP);
        POSITIONS.add(Constant.RIGHT);
        POSITIONS.add(Constant.BOTTOM);
        POSITIONS.add(Constant.TOP_LEFT);
        POSITIONS.add(Constant.TOP_RIGHT);
        POSITIONS.add(Constant.BOTTOM_RIGHT);
        POSITIONS.add(Constant.BOTTOM_LEFT);
    }
    static {
        PLACEMENT_POSITIONS.add(Constant.LEFT);
        PLACEMENT_POSITIONS.add(Constant.TOP);
        PLACEMENT_POSITIONS.add(Constant.RIGHT);
        PLACEMENT_POSITIONS.add(Constant.BOTTOM);

    }
}
