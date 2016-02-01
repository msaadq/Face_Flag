package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Saad on 2/1/2016.
 */
class FaceBoundaryDetector {

    static private int DEFAULT_RED_LOW = 0;
    static private int DEFAULT_RED_HIGH = 1;
    static private int DEFAULT_GREEN_LOW = 2;
    static private int DEFAULT_GREEN_HIGH = 3;
    static private int DEFAULT_BLUE_LOW = 4;
    static private int DEFAULT_BLUE_HIGH = 5;


    private int[] cheekOne;
    private int[] cheekTwo;
    private Bitmap face;
    private Bitmap flag;

// @return: int[ rdMin, rdMax, greenMin, greenMax, blueMin, blueMax]

    private int[] getStandardColor() {

        return null;
    }

    private ArrayList<int[]> getBoundary(int[] standardColors) {
        int[] colorRange = getStandardColor();
        ArrayList<int[]> boundaryArray=new ArrayList<>();
        boolean isFacePart;
        boolean isTopBoundaryFound;
        boolean isBottomBoundaryFound;
        Color tempColor;

        int tempCount = 0;

        for(int i = 0; i < 100; i++) {
            isTopBoundaryFound = false;
            isBottomBoundaryFound = false;

            for(int j = 0; j < 100; j++) {
                isFacePart = getIsFacePart(standardColors,new int[]{i, j},colorRange);

                if(isFacePart && !isTopBoundaryFound) {
                    tempCount = 0;

                    for(int k = j; k <= j + 5; k++) {
                        if(getIsFacePart(standardColors,new int[]{i, k},colorRange)) {
                            tempCount ++;
                        }
                    }

                    if(tempCount >= 3) {
                        isTopBoundaryFound = true;
                        boundaryArray.add(new int[]{i, j});
                    }
                }

                if(!isFacePart && !isTopBoundaryFound && isTopBoundaryFound) {
                    tempCount = 0;
                    for(int k = j; k <= j + 5; k++) {
                        if(!getIsFacePart(standardColors, new int[]{i, k},colorRange)) {
                            tempCount ++;
                        }
                    }

                    if(tempCount >= 3) {
                        isTopBoundaryFound = true;
                        boundaryArray.add(new int[]{i, j});
                    }
                }
            }
        }

        return boundaryArray;
    }

    private boolean getIsFacePart(int[] standardColors, int[] coordinates,int[] colorRange) {
        boolean isFacePart = true;

        int tempColor = this.face.getPixel(coordinates[0], coordinates[1]);
        int tempRed = Color.red(tempColor);
        int tempGreen = Color.green(tempColor);
        int tempBlue = Color.blue(tempColor);

        if(tempRed > colorRange[DEFAULT_RED_HIGH] || tempRed < colorRange[DEFAULT_RED_LOW]) {
            isFacePart = false;
        }
        if(tempGreen > colorRange[DEFAULT_GREEN_HIGH] || tempGreen < colorRange[DEFAULT_GREEN_LOW]) {
            isFacePart = false;
        }
        if(tempBlue > colorRange[DEFAULT_BLUE_HIGH] || tempBlue < colorRange[DEFAULT_BLUE_LOW]) {
            isFacePart = false;
        }

        return isFacePart;
    }


}