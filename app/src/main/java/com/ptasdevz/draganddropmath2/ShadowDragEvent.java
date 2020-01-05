package com.ptasdevz.draganddropmath2;

class ShadowDragEvent {
    private float x;
    private float y;
    private Object data;
    private int action;
    private String name;

    public ShadowDragEvent(float x, float y, Object data, int action) {
        this.x = x;
        this.y = y;
        this.data = data;
        this.action = action;
    }

    public ShadowDragEvent(float x, float y, int action) {
        this.x = x;
        this.y = y;
        this.action = action;
    }

    public ShadowDragEvent() { }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "ShadowDragEvent{" +
                "x=" + x +
                ", y=" + y +
                ", data=" + data +
                ", action=" + action +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
