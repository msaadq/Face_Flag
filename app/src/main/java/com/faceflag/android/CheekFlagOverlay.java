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

    private int CHEEK_OFFSET_RIGHT;
    private int CHEEK_OFFSET_LEFT;
    private double DEFAULT_CHEECK_OFFSET_RANGE=3.0;
    private int DEFAULT_FACE_POSITION_IN_BACKGROUND_X=250;
    private int DEFAULT_FACE_POSITION_IN_BACKGROUND_Y=350;
    private int DEFAULT_FACE_RADIUS_IN_BACKGROUND=125;

    private Bitmap normalFlag;
    private Bitmap bigflag;
    private Bitmap backgroundImage;
    private Bitmap originalImage;
    private Bitmap flagImageLeft;
    private Bitmap flagImageRight;
    private Bitmap leftFlag;
    private Bitmap rightFlag;
    private int[] croppingMetrics;
    private int[] cheeksLeftPos;
    private int[] cheeksRightPos;
    private int[] nosePos;
    private int[] eyesLeftPos;
    private int[] eyesRightPos;
    private double eulerY;
    private double eulerZ;

    CheekFlagOverlay(Bitmap normalFlag,Bitmap bigFlag, Bitmap backgroundImage, Bitmap originalImage, Bitmap flagImageLeft,Bitmap flagImageRight, int[] cheeksLeftPos, int[] cheeksRightPos,
                     int[] nosePos,int[] eyesLeftPos, int[] eyesRightPos,Double eulerY,Double eulerZ,int[] croppingMetrics){

        this.normalFlag=normalFlag;
        this.bigflag=bigFlag;
        this.backgroundImage=backgroundImage;
        this.originalImage = originalImage;
        this.flagImageLeft=flagImageLeft;
        this.flagImageRight=flagImageRight;
        this.cheeksLeftPos = cheeksLeftPos;
        this.cheeksRightPos = cheeksRightPos;
        this.nosePos = nosePos;
        this.eyesLeftPos = eyesLeftPos;
        this.eyesRightPos = eyesRightPos;
        this.eulerZ=-eulerZ;
        this.croppingMetrics=croppingMetrics;
        if(eulerY!=null) {
            this.eulerY=eulerY;
            CHEEK_OFFSET_LEFT = (int) ((originalImage.getWidth() / 480) * DEFAULT_CHEECK_OFFSET_RANGE
                    +
                    (eulerY * (Math.PI / 180) * (15 * DEFAULT_CHEECK_OFFSET_RANGE * (originalImage.getWidth() / 480))));
            CHEEK_OFFSET_RIGHT = (int) ((originalImage.getWidth() / 480) * DEFAULT_CHEECK_OFFSET_RANGE
                    -
                    (eulerY * (Math.PI / 180) * (15 * DEFAULT_CHEECK_OFFSET_RANGE * (originalImage.getWidth() / 480))));
        }else{
            CHEEK_OFFSET_LEFT = (int) ((originalImage.getWidth() / 480) * DEFAULT_CHEECK_OFFSET_RANGE);
            CHEEK_OFFSET_RIGHT = (int) ((originalImage.getWidth() / 480) * DEFAULT_CHEECK_OFFSET_RANGE);
        }

        this.cheeksLeftPos[0] = this.cheeksLeftPos[0] - CHEEK_OFFSET_LEFT;
        this.cheeksRightPos[0] = this.cheeksRightPos[0] + CHEEK_OFFSET_RIGHT;


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

    public int[] getRequiredFlagSize(Bitmap flagImage){

        int flagHeight;
        int flagWidth;

        flagHeight = -(eyesLeftPos[1]+eyesRightPos[1])/2 + (nosePos[1]);
        flagWidth = (int)(flagImage.getWidth()*((double)flagHeight/flagImage.getHeight()));

        Log.v("FlagHeight",String.valueOf(flagHeight));
        Log.v("FlagWidth", String.valueOf(flagWidth));

        return new int[] {flagHeight, flagWidth};
    }

    public Bitmap[] overlayFlag(){

        Bitmap transparentBitmap = getTransparentBitmap(originalImage.getWidth(),
                originalImage.getHeight());

        int[] flagDestLeft = getRequiredFlagSize(flagImageLeft);
        int[] flagDestRight = getRequiredFlagSize(flagImageRight);
        Bitmap resizedFlagImageLeft = flagImageLeft.createScaledBitmap(flagImageLeft, flagDestLeft[0], flagDestLeft[1],
                false);
        Bitmap resizedFlagImageRight= flagImageRight.createScaledBitmap(flagImageRight, flagDestRight[0], flagDestRight[1],
                false);

        leftFlag = tiltFlag(resizedFlagImageLeft, (int) (-5 + eulerZ));
        rightFlag = tiltFlag(resizedFlagImageRight, (int) (5+eulerZ));

        // For flag overlay on left cheek

        int xLeft1 = cheeksLeftPos[0] - flagDestLeft[0]/2;
        int xRight1 = cheeksLeftPos[0]+flagDestLeft[0]/2;
        int yTop1 = cheeksLeftPos[1] - flagDestLeft[1]/2;
        int yBottom1 = cheeksLeftPos[1] + flagDestLeft[1]/2;

        // For flag overlay on right cheek

        int xLeft2 = cheeksRightPos[0] - flagDestRight[0]/2;
        int xRight2 = cheeksRightPos[0]+flagDestRight[0]/2;
        int yTop2 = cheeksRightPos[1] - flagDestRight[1]/2;
        int yBottom2 = cheeksRightPos[1] + flagDestRight[1]/2;

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
        Bitmap faceWithFlagsOnCheeks=overlay(originalImage, transparentBitmap,140,0,0);
        Bitmap croppedBitmap = Bitmap.createBitmap(originalImage, croppingMetrics[0],
                croppingMetrics[1] , croppingMetrics[2] , croppingMetrics[3] );
        Log.v("Cropped width,hieght",croppedBitmap.getWidth()+" "+croppedBitmap.getHeight());
        int croppingExtention= (int) ((double)(croppedBitmap.getWidth()*300)/(double)560);
        if((faceWithFlagsOnCheeks.getWidth()-croppingExtention)>croppedBitmap.getWidth()&&
                ((faceWithFlagsOnCheeks.getHeight()-croppingExtention)>croppedBitmap.getHeight())
                &&croppingMetrics[0]>croppingExtention&&croppingMetrics[1]>croppingExtention){
            croppedBitmap = Bitmap.createBitmap(faceWithFlagsOnCheeks, croppingMetrics[0] - croppingExtention / 2,
                    croppingMetrics[1] - croppingExtention / 2, croppingMetrics[2] + croppingExtention, croppingMetrics[3] + croppingExtention);
        }else{
            croppedBitmap=originalImage;
        }
        Bitmap resizedFaceWithFlag=croppedBitmap;
        if(croppedBitmap.getWidth()>croppedBitmap.getHeight()) {
            resizedFaceWithFlag = flagImageLeft.createScaledBitmap(croppedBitmap, (int) (DEFAULT_FACE_RADIUS_IN_BACKGROUND *
                                        2*((double)croppedBitmap.getWidth()/(double)croppedBitmap.getHeight())),
                    DEFAULT_FACE_RADIUS_IN_BACKGROUND * 2,
                    false);
        }else {
            resizedFaceWithFlag = flagImageLeft.createScaledBitmap(croppedBitmap, DEFAULT_FACE_RADIUS_IN_BACKGROUND * 2,
                    (int) (DEFAULT_FACE_RADIUS_IN_BACKGROUND * 2 * (double) ((double) croppedBitmap.getHeight() / (double) croppedBitmap.getWidth())),
                    false);
        }

        int widthOfFaceWithFlags=resizedFaceWithFlag.getWidth();
        int heightOfFaceWithFlags=resizedFaceWithFlag.getHeight();

        int faceLimitLeft=DEFAULT_FACE_POSITION_IN_BACKGROUND_X-DEFAULT_FACE_RADIUS_IN_BACKGROUND;
        int faceLimitRight=DEFAULT_FACE_POSITION_IN_BACKGROUND_X+DEFAULT_FACE_RADIUS_IN_BACKGROUND;
        int faceLimitTop=DEFAULT_FACE_POSITION_IN_BACKGROUND_Y-DEFAULT_FACE_RADIUS_IN_BACKGROUND;
        int faceLimitBottom=DEFAULT_FACE_POSITION_IN_BACKGROUND_Y+DEFAULT_FACE_RADIUS_IN_BACKGROUND;

        Bitmap transparentBitmap2=getTransparentBitmap(500,833);
        for(int i=0; i<500; i++){
            for(int j=0; j<833;j++){

                if(isInsideEyeCircle(new int[]{i,j},DEFAULT_FACE_POSITION_IN_BACKGROUND_X,
                        DEFAULT_FACE_POSITION_IN_BACKGROUND_Y,DEFAULT_FACE_RADIUS_IN_BACKGROUND)){
                    transparentBitmap2.setPixel(i, j, resizedFaceWithFlag.getPixel(i - faceLimitLeft, j - faceLimitTop));
                }else{
                    transparentBitmap2.setPixel(i,j,backgroundImage.getPixel(i,j));
                }
            }
        }
        Bitmap flagFilterToApplyOnOriginalImage;
        Bitmap fullFlagFilterOnImage;
     /*   if(!(originalImage.getHeight()>originalImage.getWidth())) {
            if (originalImage.getWidth() < bigflag.getWidth() && originalImage.getHeight() < bigflag.getHeight()) {
                int centreFlagX = bigflag.getWidth() / 2;
                int centreFlagY = bigflag.getHeight() / 2;
                int fullFlagStartLeft = (int) (centreFlagX - ((double) originalImage.getWidth() / (double) 2));
                int fullFlagStartTop = (int) (centreFlagY - ((double) originalImage.getHeight() / (double) 2));
                flagFilterToApplyOnOriginalImage = Bitmap.createBitmap(bigflag, fullFlagStartLeft,
                        fullFlagStartTop, originalImage.getWidth(), originalImage.getHeight());
            } else {
                //TODO: resize the flag
                if (originalImage.getWidth() > bigflag.getWidth() && !(originalImage.getHeight() > bigflag.getHeight())) {
                    flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag, originalImage.getWidth(),
                            (int) (originalImage.getWidth() * ((double) bigflag.getHeight() / (double) bigflag.getWidth()) + 1),
                            false);
                } else if (!(originalImage.getWidth() > bigflag.getWidth()) && (originalImage.getHeight() > bigflag.getHeight())) {
                    flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag,
                            (int) (originalImage.getHeight() * ((double) bigflag.getWidth() / (double) bigflag.getHeight()) + 1),
                            originalImage.getHeight(),
                            false);
                } else if (originalImage.getHeight() > bigflag.getHeight() && originalImage.getWidth() > bigflag.getWidth()) {
                    if (bigflag.getWidth() > bigflag.getHeight()) {
                        flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag,
                                (int) (originalImage.getHeight() * ((double) bigflag.getWidth() / (double) bigflag.getHeight()) + 1),
                                originalImage.getHeight(),
                                false);
                    } else if (bigflag.getHeight() > bigflag.getWidth()) {
                        flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag, originalImage.getWidth(),
                                (int) (originalImage.getWidth() * ((double) bigflag.getHeight() / (double) bigflag.getWidth()) + 1),
                                false);
                    } else {
                        if (originalImage.getHeight() > originalImage.getWidth()) {
                            flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag,
                                    (int) (originalImage.getHeight() * ((double) bigflag.getWidth() / (double) bigflag.getHeight()) + 1),
                                    originalImage.getHeight(),
                                    false);
                        } else if (originalImage.getWidth() > originalImage.getHeight()) {
                            flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag, originalImage.getWidth(),
                                    (int) (originalImage.getWidth() * ((double) bigflag.getHeight() / (double) bigflag.getWidth()) + 1),
                                    false);
                        } else {
                            flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag,
                                    originalImage.getWidth(), originalImage.getHeight(), false);
                        }
                    }
                } else {
                    flagFilterToApplyOnOriginalImage = bigflag;
                }

                int centreFlagX = flagFilterToApplyOnOriginalImage.getWidth() / 2;
                int centreFlagY = flagFilterToApplyOnOriginalImage.getHeight() / 2;
                int fullFlagStartLeft = (int) (centreFlagX - ((double) originalImage.getWidth() / (double) 2));
                int fullFlagStartTop = (int) (centreFlagY - ((double) originalImage.getHeight() / (double) 2));
                flagFilterToApplyOnOriginalImage = Bitmap.createBitmap(flagFilterToApplyOnOriginalImage, fullFlagStartLeft,
                        fullFlagStartTop, originalImage.getWidth(), originalImage.getHeight());
            }
            fullFlagFilterOnImage=overlay(originalImage,flagFilterToApplyOnOriginalImage,50,0);*/
        if(originalImage.getHeight()>originalImage.getWidth()){
            flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag, originalImage.getWidth(),
                        (int) (originalImage.getWidth() * ((double) bigflag.getHeight() / (double) bigflag.getWidth()) + 1),
                        false);

            Bitmap transparentBitmapOfOrignalSize=getTransparentBitmap(originalImage.getWidth(),originalImage.getHeight());

            for(int i=0;i<transparentBitmapOfOrignalSize.getWidth();i++){
                for(int j=0;j<transparentBitmapOfOrignalSize.getHeight();j++){
                        transparentBitmapOfOrignalSize.setPixel(i, j, flagFilterToApplyOnOriginalImage.getPixel(i, 3));
                }
            }
            fullFlagFilterOnImage=overlay(originalImage,transparentBitmapOfOrignalSize,150,0,0);
        }else if(originalImage.getHeight()<originalImage.getWidth()){
            flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag,
                    (int) (originalImage.getHeight() * ((double) bigflag.getWidth() / (double) bigflag.getHeight()) + 1),
                    originalImage.getHeight(),
                    false);

            Bitmap transparentBitmapOfOrignalSize=getTransparentBitmap(originalImage.getWidth(),originalImage.getHeight());

            for(int i=0;i<transparentBitmapOfOrignalSize.getWidth();i++){
                for(int j=0;j<transparentBitmapOfOrignalSize.getHeight();j++){
                    transparentBitmapOfOrignalSize.setPixel(i, j, flagFilterToApplyOnOriginalImage.getPixel(3, j));
                }
            }
            fullFlagFilterOnImage=overlay(originalImage,transparentBitmapOfOrignalSize,150,0,0);

        }else{
            flagFilterToApplyOnOriginalImage = bigflag.createScaledBitmap(bigflag, originalImage.getWidth(),
                    (int) (originalImage.getWidth() * ((double) bigflag.getHeight() / (double) bigflag.getWidth()) + 1),
                    false);

            Bitmap transparentBitmapOfOrignalSize=getTransparentBitmap(originalImage.getWidth(),originalImage.getHeight());

            for(int i=0;i<transparentBitmapOfOrignalSize.getWidth();i++){
                for(int j=0;j<transparentBitmapOfOrignalSize.getHeight();j++){
                    transparentBitmapOfOrignalSize.setPixel(i, j, flagFilterToApplyOnOriginalImage.getPixel(i, 3));
                }
            }
            fullFlagFilterOnImage=overlay(originalImage,transparentBitmapOfOrignalSize,150,0,0);
        }
        int normalFlagWidth= (int) ((double)originalImage.getWidth()/(double)2.2);
        int normalFlagHieght=normalFlagWidth;
        Bitmap resizedNormalFlag=normalFlag.createScaledBitmap(normalFlag, normalFlagWidth, normalFlagHieght, false);

        fullFlagFilterOnImage=overlay(fullFlagFilterOnImage,resizedNormalFlag,255,
                fullFlagFilterOnImage.getWidth()-normalFlagWidth,
                fullFlagFilterOnImage.getHeight()-normalFlagHieght
        );
        return new Bitmap[]{faceWithFlagsOnCheeks,transparentBitmap2,fullFlagFilterOnImage};
    }

    private Bitmap getTransparentBitmap(int xRes,int yRes){
        Bitmap bmOverlay = Bitmap.createBitmap(xRes, yRes, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setAlpha(0);
        canvas.drawBitmap(bmOverlay,new Matrix(),paint);
        return  bmOverlay;
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2,int alpha,float translateX,float translateY) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        Matrix matrix=new Matrix();
        matrix.preTranslate(translateX,translateY);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, matrix, paint);
        return bmOverlay;
    }

    public boolean isInsideEyeCircle(int[] coordinates,int h,int k,int radius){
        return (
                radius>(Math.sqrt(Math.pow(coordinates[0]-h,2)+Math.pow(coordinates[1]-k,2)))
        );
    }
}
