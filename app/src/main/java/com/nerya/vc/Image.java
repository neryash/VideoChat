package com.nerya.vc;

public class Image {
    private String pixArr;
    private int width, height;
    public Image(String pixArr, int width, int height){
        this.pixArr = pixArr;
        this.width = width;
        this.height = height;
    }

    public String getPixArr() {
        return pixArr;
    }

    public void setPixArr(String pixArr) {
        this.pixArr = pixArr;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
