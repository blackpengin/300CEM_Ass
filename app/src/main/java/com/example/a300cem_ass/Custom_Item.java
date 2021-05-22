package com.example.a300cem_ass;

import java.io.Serializable;

public class Custom_Item implements Serializable {
    private int mImageResource;
    private String mText1, mText2;

    public Custom_Item(int imageResource, String text1, String text2){
        mImageResource = imageResource;
        mText1 = text1;
        mText2 = text2;
    }

    public int getImageResource() {
        return mImageResource;
    }

    public String getText1() {
        return mText1;
    }

    public String getText2() {
        return mText2;
    }


}
