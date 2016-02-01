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

    static private int DEFAULT_CHEECK_RANGE = 6;
    static private double DEFAULT_COLOR_RANGE = 1.0;


    private int[] cheekOne;
    private int[] cheekTwo;
    private int[] faceResolution;
    private double incrementalFactorX;
    private double incrementalFactorY;
    private Bitmap face;

    public FaceBoundaryDetector(Bitmap face, int[] cheekOne, int[] cheekTwo) {
        faceResolution = new int[]{face.getWidth(), face.getHeight()};

        this.face = face;
        this.cheekOne = cheekOne;
        this.cheekTwo = cheekTwo;

        incrementalFactorX = (double) faceResolution[0] / 100.0;
        incrementalFactorY = (double) faceResolution[1] / 100.0;
    }

// @return: int[ rdMin, rdMax, greenMin, greenMax, blueMin, blueMax]

    public int[] getStandardColor() {
        ArrayList<Integer> allReds = new ArrayList<>();
        ArrayList<Integer> allGreens = new ArrayList<>();
        ArrayList<Integer> allBlues = new ArrayList<>();

        for(int i = (int)(cheekOne[0] - DEFAULT_CHEECK_RANGE * incrementalFactorX); i <= cheekOne[0] + DEFAULT_CHEECK_RANGE * incrementalFactorX; i++) {
            for(int j = (int)(cheekOne[1] - DEFAULT_CHEECK_RANGE * incrementalFactorY); j <= cheekOne[1] + DEFAULT_CHEECK_RANGE * incrementalFactorY; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        for(int i = (int)(cheekTwo[0] - DEFAULT_CHEECK_RANGE * incrementalFactorX); i <= cheekTwo[0] + DEFAULT_CHEECK_RANGE * incrementalFactorX; i++) {
            for(int j = (int)(cheekTwo[1] - DEFAULT_CHEECK_RANGE * incrementalFactorY); j <= cheekTwo[1] + DEFAULT_CHEECK_RANGE * incrementalFactorY; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        double[] averages = new double[]{getAverage(allReds), getAverage(allGreens), getAverage(allBlues)};
        double[] stnDevs = new double[]{getStnDev(allReds) * DEFAULT_COLOR_RANGE,
                getStnDev(allGreens) * DEFAULT_COLOR_RANGE, getStnDev(allBlues) * DEFAULT_COLOR_RANGE};

        return new int[]{(int) (averages[0] - stnDevs[0]), (int) (averages[0] + stnDevs[0]), (int) (averages[1] - stnDevs[1]),
                (int) (averages[1] + stnDevs[1]), (int) (averages[2] - stnDevs[2]),  (int) (averages[2] + stnDevs[2])};
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

    private double getAverage(ArrayList<Integer> array) {
        double sum = 0;

        for(Integer number : array) {
            sum += number;
        }

        return sum / (double) array.size();
    }

    private double getStnDev(ArrayList<Integer> array) {
        double average = getAverage(array);
        double sum = 0;
        double variance;

        for(Integer number : array) {
            sum += Math.pow(Math.abs(number - average), 2);
        }

        variance = sum / (double) array.size();

        return Math.sqrt(variance);
    }


}