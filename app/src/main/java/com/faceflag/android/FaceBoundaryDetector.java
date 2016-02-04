package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Saad on 2/1/2016.
 */
class FaceBoundaryDetector {

    final String LOG_TAG="FaceBoundaryDetector";

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
    private int[] eyeOpeningLeft;
    private int[] eyeOpeningRight;
    static final private int DEFAULT_EYE_OPENING_WIDTH=7;
    static final private int DEFAULT_EYE_OPENING_HIEGHT=2;

    FaceBoundaryDetector(Bitmap scaledFace,Bitmap scaledFlag){
        face=scaledFace;
        flag=scaledFlag;
    }

// @return: int[ rdMin, rdMax, greenMin, greenMax, blueMin, blueMax]

    private ArrayList<int[]> getStandardColor(int[][] cheeckPositions,int[][] eyesPositions) {
        final int interval=2;
        final int range=0;
        //right Cheek
        cheekOne=cheeckPositions[0];
        //left cheek
        cheekTwo=cheeckPositions[1];
        ArrayList<int[]> colorArray=new ArrayList<>();
        colorArray.add(getStandardColor());
        return colorArray;
    }
/*
    private ArrayList<int[]> getBoundary(int[][] cheeckPositions,int[][] eyesPositions) {
        ArrayList<int[]> colorRangeArray = getStandardColor(cheeckPositions,eyesPositions);
        ArrayList<int[]> boundaryArray=new ArrayList<>();
        boolean isFacePart;
        boolean isTopBoundaryFound;
        boolean isBottomBoundaryFound;
        Color tempColor;

        int tempCount = 0;
        for(int count=0;count<colorRangeArray.size();count++) {
            for (int i = 0; i < 100; i++) {
                isTopBoundaryFound = false;
                isBottomBoundaryFound = false;

                for (int j = 0; j < 100; j++) {
                    isFacePart = getIsFacePart(new int[]{i, j}, colorRangeArray.get(count));

                    if (isFacePart && !isTopBoundaryFound) {
                        tempCount = 0;

                        for (int k = j; k <= j + 5; k++) {
                            if (getIsFacePart(new int[]{i, k}, colorRangeArray.get(count))) {
                                tempCount++;
                            }
                        }

                        if (tempCount >= 3) {
                            isTopBoundaryFound = true;
                            boundaryArray.add(new int[]{i, j});
                        }
                    }

                    if (!isFacePart && !isTopBoundaryFound && isTopBoundaryFound) {
                        tempCount = 0;
                        for (int k = j; k <= j + 5; k++) {
                            if (!getIsFacePart(new int[]{i, k}, colorRangeArray.get(count))) {
                                tempCount++;
                            }
                        }

                        if (tempCount >= 3) {
                            isTopBoundaryFound = true;
                            boundaryArray.add(new int[]{i, j});
                        }
                    }
                }
            }
        }
        return boundaryArray;
    }*/

    private boolean getIsFacePart(int[] coordinates,int[] colorRange) {
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
    ArrayList<int[]> colorRangeArray;
    public Bitmap getFaceWithFlag(Bitmap scaledFace,Bitmap scaledFlag,int[][] cheeckPositions,int[][] eyesPositions,Bitmap transparent){
        Bitmap facePlusFlag=scaledFace;
        colorRangeArray = getStandardColor(cheeckPositions, eyesPositions);
        boolean isFacePart;
        int leftFaceLimit=0;
        int transitions = 0;
        int rightFaceLimit=100;
        Bitmap flagFiler = getTransparentBitmap(transparent);
        for(int count=0;count<colorRangeArray.size();count++) {


            outerloop:
            for (int j = 0; j < 100; j++) {
                for (int i = 0; i < 70; i++) {
                    boolean thisIsFacePart = getIsFacePart(new int[]{i, j}, colorRangeArray.get(count));
                    boolean nextIsFacePart = getIsFacePart(new int[]{i+1, j}, colorRangeArray.get(count));
                    if (!thisIsFacePart&&nextIsFacePart){
                        leftFaceLimit+=i;
                        transitions++;
                        break;
                    }
                }
            }
        }
        leftFaceLimit=leftFaceLimit/transitions;
        transitions = 0;
        for(int count=0;count<colorRangeArray.size();count++) {
            outerloop:
            for (int j = 0; j < 100; j++) {
                for (int i = 99; i >= 30; i--) {
                    boolean thisIsFacePart = getIsFacePart(new int[]{i, j}, colorRangeArray.get(count));
                    boolean nextIsFacePart = getIsFacePart(new int[]{i-1, j}, colorRangeArray.get(count));
                    if (!thisIsFacePart&&nextIsFacePart){
                        rightFaceLimit+=i;
                        transitions++;
                        break;
                    }
                }
            }
        }

        rightFaceLimit=rightFaceLimit/transitions;

        ArrayList<Integer> faceBottomLimitIndexes=new ArrayList<Integer>();
        for(int count=0;count<colorRangeArray.size();count++) {
            for (int i = leftFaceLimit; i < rightFaceLimit; i++) {
                for (int j = 99; j > 50; j--) {
                    boolean thisIsFacePart = getIsFacePart(new int[]{i, j}, colorRangeArray.get(count));
                    boolean nextIsFacePart = getIsFacePart(new int[]{i, j-1}, colorRangeArray.get(count));
                    if ((!thisIsFacePart&&nextIsFacePart)||(thisIsFacePart&&!nextIsFacePart)){
                        transitions++;

                    }
                    if(transitions==1){
                        faceBottomLimitIndexes.add(j);
                        break;
                    }

                }
            }
        }
        eyesPositions[0]=getAccurateEyePosition(eyesPositions[0],10);
        eyesPositions[1]=getAccurateEyePosition(eyesPositions[1],10);
        eyeOpeningRight=new int[]{
                eyesPositions[0][0]-DEFAULT_EYE_OPENING_WIDTH,
                eyesPositions[0][0]+DEFAULT_EYE_OPENING_WIDTH,
                eyesPositions[0][1]-DEFAULT_EYE_OPENING_HIEGHT,
                eyesPositions[0][1]+DEFAULT_EYE_OPENING_HIEGHT
        };
        eyeOpeningLeft=new int[]{
                eyesPositions[1][0]-DEFAULT_EYE_OPENING_WIDTH,
                eyesPositions[1][0]+DEFAULT_EYE_OPENING_WIDTH,
                eyesPositions[1][1]-DEFAULT_EYE_OPENING_HIEGHT,
                eyesPositions[1][1]+DEFAULT_EYE_OPENING_HIEGHT
        };
        for(int count=0;count<colorRangeArray.size();count++) {
            for (int i = leftFaceLimit; i < rightFaceLimit; i++) {
                for (int j = 0; j < 100; j++) {
                    isFacePart = getIsFacePart(new int[]{i, j}, colorRangeArray.get(count));

                    if(isFacePart){
                        if(
                                ((i>eyeOpeningLeft[0]&&i<eyeOpeningLeft[1])&&(j>eyeOpeningLeft[2]&&j<eyeOpeningLeft[3]))
                                        ||
                                        ((i>eyeOpeningRight[0]&&i<eyeOpeningRight[1])&&(j>eyeOpeningRight[2]&&j<eyeOpeningRight[3]))
                                ){


                        }else {
                            int flagPixelColor = flag.getPixel(i, j);

                            int tempFlagRed = Color.red(flagPixelColor);
                            int tempFlagGreen = Color.green(flagPixelColor);
                            int tempFlagBlue = Color.blue(flagPixelColor);

                            int flagFilterPixelColor = Color.argb(
                                    255,
                                    tempFlagRed,
                                    tempFlagGreen,
                                    tempFlagBlue
                            );
                            flagFiler.setPixel(i, j, flagFilterPixelColor);

                        }
                    }
                }
            }
        }
        for(int count=0;count<colorRangeArray.size();count++) {
            for(int i=leftFaceLimit;i<rightFaceLimit;i++){
                boolean bottomIsFacePart = getIsFacePart(new int[]{i, 99}, colorRangeArray.get(count));
                if(bottomIsFacePart) {
                    for (int j = 99; j > 50; j--) {
                        boolean thisIsFacePart = getIsFacePart(new int[]{i, j}, colorRangeArray.get(count));
                        int flagFilterPixelColor = Color.argb(
                                0,
                                0,
                                0,
                                0
                        );
                        flagFiler.setPixel(i, j, flagFilterPixelColor);
                        if(!thisIsFacePart){
                            break;
                        }
                    }
                }else{
                    for (int j = 99; j > 50; j--) {
                        boolean thisIsFacePart = getIsFacePart(new int[]{i, j}, colorRangeArray.get(count));
                        int flagFilterPixelColor = Color.argb(
                                0,
                                0,
                                0,
                                0
                        );
                        flagFiler.setPixel(i, j, flagFilterPixelColor);
                        if(thisIsFacePart){
                            break;
                        }
                    }
                }
            }
        }

        return overlay(face,flagFiler);
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setAlpha(140);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), paint);
        return bmOverlay;
    }

    private Bitmap getTransparentBitmap(Bitmap bmp1){
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setAlpha(0);
        canvas.drawBitmap(bmp1,new Matrix(),paint);
        return  bmOverlay;
    }
/*
    public ArrayList<int[]> getAllShadesOfColor(int red, int green, int blue, int interval, int min, int max){
        ArrayList<int[]> shadesArray=new ArrayList<>();

        int tred=red;
        int tgreen=green;
        int tblue=blue;
        for(int i=min;i<max;i++) {
            shadesArray.add(new int[]{
                    tred - interval, tred + interval,
                    tgreen - interval, tgreen + interval,
                    tblue - interval, tblue + interval
            });
            if (tred < max) {
                tred++;
            }
            if (tgreen < max) {
                tgreen++;
            }
            if(tblue<max) {
                tblue++;
            }
        }
        tred=red;
        tgreen=green;
        tblue=blue;
        for(int i=max;i>min;i--) {
            shadesArray.add(new int[]{
                    tred - interval, tred + interval,
                    tgreen - interval, tgreen + interval,
                    tblue - interval, tblue + interval
            });
            if (tred > min) {
                tred--;
            }
            if (tgreen > min) {
                tgreen--;
            }
            if(tblue>min) {
                tblue--;
            }
        }
        return shadesArray;
    }*/

    static private int DEFAULT_CHEECK_LEFT_RANGE_X_LEFT = 7;
    static private int DEFAULT_CHEECK_LEFT_RANGE_X_RIGHT = 30;
    static private int DEFAULT_CHEECK_RIGHT_RANGE_X_LEFT = 30;
    static private int DEFAULT_CHEECK_RIGHT_RANGE_X_RIGHT = 7;
    static private int DEFAULT_CHEECK_RANGE_Y_TOP = 2;
    static private int DEFAULT_CHEECK_RANGE_Y_BOTTOM = 40;
    static private double DEFAULT_COLOR_RANGE = 1;
    static private double DEFAULT_STANDARD_DEVIATION_THRESHHOLD=5;


    private int[] faceResolution;
    private double incrementalFactorX;
    private double incrementalFactorY;

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

        for(int i = (int)(cheekOne[0] - DEFAULT_CHEECK_RIGHT_RANGE_X_LEFT * incrementalFactorX); i <= cheekOne[0] + DEFAULT_CHEECK_RIGHT_RANGE_X_RIGHT * incrementalFactorX; i++) {
            for(int j = (int)(cheekOne[1] - DEFAULT_CHEECK_RANGE_Y_TOP * incrementalFactorY); j <= cheekOne[1] + DEFAULT_CHEECK_RANGE_Y_BOTTOM * incrementalFactorY; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        for(int i = (int)(cheekTwo[0] - DEFAULT_CHEECK_LEFT_RANGE_X_LEFT * incrementalFactorX); i <= cheekTwo[0] + DEFAULT_CHEECK_LEFT_RANGE_X_RIGHT * incrementalFactorX; i++) {
            for(int j = (int)(cheekTwo[1] - DEFAULT_CHEECK_RANGE_Y_TOP * incrementalFactorY); j <= cheekTwo[1] + DEFAULT_CHEECK_RANGE_Y_BOTTOM * incrementalFactorY; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        double[] averages = new double[]{getAverage(allReds), getAverage(allGreens), getAverage(allBlues)};
        double[] stnDevs = new double[]{getStnDev(allReds) * DEFAULT_COLOR_RANGE,
                getStnDev(allGreens) * DEFAULT_COLOR_RANGE, getStnDev(allBlues) * DEFAULT_COLOR_RANGE};

        return new int[]{
                (int) (averages[0] - DEFAULT_STANDARD_DEVIATION_THRESHHOLD*stnDevs[0]),
                (int) (averages[0] + DEFAULT_STANDARD_DEVIATION_THRESHHOLD*stnDevs[0]),
                (int) (averages[1] - DEFAULT_STANDARD_DEVIATION_THRESHHOLD*stnDevs[1]),
                (int) (averages[1] + DEFAULT_STANDARD_DEVIATION_THRESHHOLD*stnDevs[1]),
                (int) (averages[2] - DEFAULT_STANDARD_DEVIATION_THRESHHOLD*stnDevs[2]),
                (int) (averages[2] + DEFAULT_STANDARD_DEVIATION_THRESHHOLD*stnDevs[2])};
    }
/*

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
*/

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

    public int[] getAccurateEyePosition(int[] EyePosition, int radius){
        double sumxs=0;
        double sumys=0;
        int xs=0;
        int ys=0;
        int h=EyePosition[0];
        int k=EyePosition[1];
        for(int count=0;count<colorRangeArray.size();count++) {
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    if (isInsideEyeCircle(new int[]{i, j}, h, k, radius) && !getIsFacePart(new int[]{i, j}, colorRangeArray.get(count))){
                        sumxs+=i;
                        xs++;
                        sumys+=j;
                        ys++;
                    }
                }
            }
        }

        return new int[]{(int) (sumxs/xs), (int) (sumys/ys)};
    }
    public boolean isInsideEyeCircle(int[] coordinates,int h,int k,int radius){
        return (
                radius>(Math.sqrt(Math.pow(coordinates[0]-h,2)+Math.pow(coordinates[1]-k,2)))
        );
    }
}