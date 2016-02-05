package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by usmankhan on 2/5/2016.
 */
public class CheekFlagOverlay {

    private int CHEEK_OFFSET = 7;

    private Bitmap originalImage;
    private Bitmap flagImage;
    private Bitmap leftFlag;
    private Bitmap rightFlag;
    private int[] cheeksLeftPos;
    private int[] cheeksRightPos;
    private int[] nosePos;
    private int[] eyesLeftPos;
    private int[] eyesRightPos;

    CheekFlagOverlay(Bitmap originalImage, Bitmap flagImage, int[] cheeksLeftPos, int[] cheeksRightPos,
                     int[] nosePos,int[] eyesLeftPos, int[] eyesRightPos){

        this.originalImage = originalImage;
        this.flagImage = flagImage;
        this.cheeksLeftPos = cheeksLeftPos;
        this.cheeksRightPos = cheeksRightPos;
        this.nosePos = nosePos;
        this.eyesLeftPos = eyesLeftPos;
        this.eyesRightPos = eyesRightPos;

        CHEEK_OFFSET = (originalImage.getWidth()/480) * 7;

        this.cheeksLeftPos[0] = this.cheeksLeftPos[0] - CHEEK_OFFSET;
        this.cheeksRightPos[0] = this.cheeksRightPos[0] + CHEEK_OFFSET;

    }

    /**
     *
     * @return Flag Width & Flag Height
     */


    public Bitmap tiltFlag(Bitmap flagImage, int tiltAngle){

        Matrix matrix = new Matrix();
        matrix.postRotate(tiltAngle);

        return Bitmap.createBitmap(flagImage, 0, 0, flagImage.getWidth(),flagImage.getHeight(),
                matrix,false);
    }



    public int[] getRequiredFlagSize(){

        int flagHeight;
        int flagWidth;

        flagHeight = -(eyesLeftPos[1]+eyesRightPos[1])/2 + (nosePos[1]);
        flagWidth = (int)(flagImage.getWidth()*((double)flagHeight/flagImage.getHeight()));

        Log.v("FlagHeight",String.valueOf(flagHeight));
        Log.v("FlagWidth",String.valueOf(flagWidth));

        return new int[] {flagHeight, flagWidth};
    }

    public Bitmap overlayFlag(){

        Bitmap transparentBitmap = getTransparentBitmap(originalImage.getWidth(),
                originalImage.getHeight());

        int[] flagDest = getRequiredFlagSize();
        Bitmap resizedFlagImage = flagImage.createScaledBitmap(flagImage, flagDest[0], flagDest[1],
                false);

        leftFlag = tiltFlag(resizedFlagImage,-5);
        rightFlag = tiltFlag(resizedFlagImage,5);

        // For flag overlay on left cheek

        int xLeft1 = cheeksLeftPos[0] - flagDest[0]/2;
        int xRight1 = cheeksLeftPos[0]+flagDest[0]/2;
        int yTop1 = cheeksLeftPos[1] - flagDest[1]/2;
        int yBottom1 = cheeksLeftPos[1] + flagDest[1]/2;

        // For flag overlay on right cheek

        int xLeft2 = cheeksRightPos[0] - flagDest[0]/2;
        int xRight2 = cheeksRightPos[0]+flagDest[0]/2;
        int yTop2 = cheeksRightPos[1] - flagDest[1]/2;
        int yBottom2 = cheeksRightPos[1] + flagDest[1]/2;

        for (int i=xLeft1;i<xRight1;i++){
            for (int j = yTop1;j<yBottom1;j++){
                transparentBitmap.setPixel(i,j,leftFlag.getPixel(i - xLeft1, j - yTop1));
            }
        }

        for (int i=xLeft2;i<xRight2;i++){
            for (int j = yTop2;j<yBottom2;j++){
                transparentBitmap.setPixel(i,j,rightFlag.getPixel(i - xLeft2, j - yTop2));
            }
        }

        return overlay(originalImage,transparentBitmap);
    }

    private Bitmap getTransparentBitmap(int xRes,int yRes){
        Bitmap bmOverlay = Bitmap.createBitmap(xRes, yRes, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setAlpha(0);
        canvas.drawBitmap(bmOverlay,new Matrix(),paint);
        return  bmOverlay;
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setAlpha(200);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), paint);
        return bmOverlay;
    }

}
