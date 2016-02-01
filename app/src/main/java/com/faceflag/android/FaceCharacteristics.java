package com.faceflag.android;

import android.graphics.Bitmap;
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
    SparseArray<Face> faces;

    FaceCharacteristics(SparseArray<Face> faces){

        this.faces = faces;
        // x&y coordinates of left and right cheek. Right cheek is followed by
        // left cheek. Similarly, y is followed by x.
        cheeks_pos = new int[2][2];
        eyes_pos = new int[2][2];
    }

    public int[][] getCheeks_pos(){
        //for (int i = 0; i < mFaces.size(); ++i) {
        for (int i = 0; i < 1; ++i) {
            Face face = faces.valueAt(i);

            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);

                //Left Cheek
                if (landmark.getType() == 1){
                    Log.d("Left Cheek Value (x): ", String.valueOf(cx));
                    Log.d("Left Cheek Value (y):", String.valueOf(cy));
                    cheeks_pos[0][0] = cx;
                    cheeks_pos[0][1] = cy;
                }

                // Right Cheek
                if (landmark.getType() == 7){
                    Log.d("Right Cheek Value (x): ", String.valueOf(cx));
                    Log.d("Right Cheek Value (y):", String.valueOf(cy));
                    cheeks_pos[1][0] = cx;
                    cheeks_pos[1][1] = cy;
                }

                Log.d("Feature: ", String.valueOf(landmark.getType()));
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

    public Bitmap getCroppedBitmap(Bitmap bitmap){

        int offset_x, offset_y;

        // for (int i = 0; i < faces.size(); ++i) {
        Face face = faces.valueAt(0);

        offset_x = (int)(face.getWidth());
        offset_y = (int)(face.getHeight());

        //}

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 10, 10,
                offset_x, offset_y);

        return croppedBitmap;
    }

}
