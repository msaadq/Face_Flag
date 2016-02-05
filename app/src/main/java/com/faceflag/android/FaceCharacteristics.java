package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Created by usmankhan on 2/1/2016.
 */

public class FaceCharacteristics {

    int cheeks_pos[][];
    int eyes_pos[][];
    int nose_pos[];
    Double eulerZ;
    Double eulerY;
    SparseArray<Face> faces;

    FaceCharacteristics(SparseArray<Face> faces){

        this.faces = faces;
        // x&y coordinates of left and right cheek. Right cheek is followed by
        // left cheek. Similarly, y is followed by x.
        cheeks_pos = new int[2][2];
        eyes_pos = new int[2][2];
        nose_pos = new int[2];
    }

    public int[][] getCheeks_pos(){
        //for (int i = 0; i < mFaces.size(); ++i) {

        int pos_x = 0;
        int pos_y = 0;

        for (int i = 0; i < 1; i++) {
            Face face = faces.valueAt(i);
            pos_x = (int) face.getPosition().x;
            pos_y = (int) face.getPosition().y;
            Log.v("Positions: ",String.valueOf(pos_x)+String.valueOf(pos_y));


            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);

                //Left Cheek
                if (landmark.getType() == 7){

                    cheeks_pos[0][0] = cx - pos_x;
                    cheeks_pos[0][1] = cy - pos_y;
                    Log.v("Left Cheek x",String.valueOf(cheeks_pos[0][0]));
                    Log.v("Left Cheek y",String.valueOf(cheeks_pos[0][1]));
                }

                // Right Cheek
                if (landmark.getType() == 1){

                    cheeks_pos[1][0] = cx - pos_x;
                    cheeks_pos[1][1] = cy - pos_y;
                    Log.v("3",String.valueOf(cheeks_pos[1][0]));
                    Log.v("4",String.valueOf(cheeks_pos[1][1]));
                }

            }

        }

        return cheeks_pos;
    }


    public int[][] getEyes_pos(){
        //for (int i = 0; i < mFaces.size(); ++i) {
        for (int i = 0; i < 1; ++i) {
            Face face = faces.valueAt(i);

            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);

                //Left Eye
                if (landmark.getType() == 4){
                    Log.d("Left Eye Value (x): ", String.valueOf(cx));
                    Log.d("Left Eye Value (y):", String.valueOf(cy));
                    eyes_pos[0][0] = cx;
                    eyes_pos[0][1] = cy;
                }

                // Right Eye
                if (landmark.getType() == 10){
                    Log.d("Right Eye Value (x): ", String.valueOf(cx));
                    Log.d("Right Eye Value (y):", String.valueOf(cy));
                    eyes_pos[1][0] = cx;
                    eyes_pos[1][1] = cy;
                }

                Log.d("Feature: ", String.valueOf(landmark.getType()));
            }

        }
        return eyes_pos;
    }

    public int[][] getCheeksOriginalPos(){
        //for (int i = 0; i < mFaces.size(); ++i) {
        for (int i = 0; i < 1; ++i) {
            Face face = faces.valueAt(i);

            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);

                //Left Cheek
                if (landmark.getType() == 7){
                    Log.d("Left Cheek Value (x): ", String.valueOf(cx));
                    Log.d("Left Cheek Value (y):", String.valueOf(cy));
                    cheeks_pos[0][0] = cx;
                    cheeks_pos[0][1] = cy;
                }

                // Right Cheek
                if (landmark.getType() == 1){
                    Log.d("Right Cheek Value (x): ", String.valueOf(cx));
                    Log.d("Right Cheek Value (y):", String.valueOf(cy));
                    cheeks_pos[1][0] = cx;
                    cheeks_pos[1][1] = cy;
                }

            }

        }
        return cheeks_pos;
    }


    public int[][] getEyesOriginalPos(){
        //for (int i = 0; i < mFaces.size(); ++i) {
        for (int i = 0; i < 1; ++i) {
            Face face = faces.valueAt(i);

            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);

                //Left Eye
                if (landmark.getType() == 10){
                    Log.d("Left Eye Value (x): ", String.valueOf(cx));
                    Log.d("Left Eye Value (y):", String.valueOf(cy));
                    eyes_pos[0][0] = cx;
                    eyes_pos[0][1] = cy;
                }

                // Right Eye
                if (landmark.getType() == 4){
                    Log.d("Right Eye Value (x): ", String.valueOf(cx));
                    Log.d("Right Eye Value (y):", String.valueOf(cy));
                    eyes_pos[1][0] = cx;
                    eyes_pos[1][1] = cy;
                }

                Log.d("Feature: ", String.valueOf(landmark.getType()));
            }

        }
        return eyes_pos;
    }

    public int[] getNoseOriginalPos(){
        //for (int i = 0; i < mFaces.size(); ++i) {
        for (int i = 0; i < 1; ++i) {
            Face face = faces.valueAt(i);

            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);

                //Nose
                if (landmark.getType() == 6){
                    Log.d("Nose Value (x): ", String.valueOf(cx));
                    Log.d("Nose Value (y):", String.valueOf(cy));
                    nose_pos[0] = cx;
                    nose_pos[1] = cy;
                }

                Log.d("Feature: ", String.valueOf(landmark.getType()));
            }

        }
        return nose_pos;
    }


    public Bitmap getCroppedBitmap(Bitmap bitmap){

        // for (int i = 0; i < faces.size(); ++i) {
        Face face = faces.valueAt(0);

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (int)face.getPosition().x,
                (int)face.getPosition().y, (int)face.getWidth(), (int)face.getHeight());

        return croppedBitmap;
    }


    public Double getFaceEulerY(){
        for(int i=0;i<1;i++){
            Face face = faces.valueAt(i);
            eulerY=Double.valueOf(face.getEulerY());
        }

        return  eulerY;
    }

    public Double getFaceEulerZ(){
        for(int i=0;i<1;i++){
            Face face = faces.valueAt(i);
            eulerY=Double.valueOf(face.getEulerZ());
        }

        return  eulerZ;
    }
    public int[] getCroppingMetrics(){
        Face face=faces.valueAt(0);
        return new int[]{(int)face.getPosition().x,
                (int)face.getPosition().y, (int)face.getWidth(), (int)face.getHeight()};
    }

}
