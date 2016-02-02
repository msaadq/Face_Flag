package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG="MainActivity";
    int cheeks_pos[][];
    int eyes_pos[][];
    FaceCharacteristics faceCharacteristics;
    ImageView bitmap_image;
    ImageView flag_image;
    ImageView face_plus_flag_image;
    Bitmap croppedBitmap;
    Bitmap resizedFaceBitmap;
    Bitmap resizedFlagBitmap;
    Bitmap transparent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resizedFaceBitmap=getScaledFaceBitmap();
        resizedFlagBitmap=getScaledFlagBitmap();
        // Set bitmap in ImageView
      //  bitmap_image.setImageBitmap(croppedBitmap);
        flag_image.setImageBitmap(resizedFlagBitmap);

        normalizeCheekPosition();

        DownloadFilesTask downloadFilesTask=new DownloadFilesTask();
        downloadFilesTask.execute(resizedFaceBitmap);
        // Release resources associated with DetectorFace object

    }

    void bindViews(){
        bitmap_image = (ImageView) findViewById(R.id.bitmap_image);
        flag_image=(ImageView) findViewById(R.id.flag_image);
        face_plus_flag_image=(ImageView) findViewById(R.id.face_plus_flag_image);
    }

    Bitmap getScaledFaceBitmap(){
        InputStream stream = getResources().openRawResource(R.raw.image02);
        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        InputStream tansparentStream = getResources().openRawResource(R.raw.image03);
        Bitmap transpBitmap = BitmapFactory.decodeStream(tansparentStream);
        transparent=Bitmap.createScaledBitmap(transpBitmap, 100, 100, false);
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        bindViews();

        // Create a frame from the bitmap and run face detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        faceCharacteristics = new FaceCharacteristics(faces);

        // Get face features
        cheeks_pos = faceCharacteristics.getCheeks_pos();
        Log.v(LOG_TAG,"X: "+cheeks_pos[0][0]+"to"+cheeks_pos[1][0]);
        Log.v(LOG_TAG,"Y: "+cheeks_pos[0][1]+"to"+cheeks_pos[1][1]);
        eyes_pos = faceCharacteristics.getEyes_pos();
        croppedBitmap = faceCharacteristics.getCroppedBitmap(bitmap);
        Frame frameCropped = new Frame.Builder().setBitmap(croppedBitmap).build();
        SparseArray<Face> faceCropped = detector.detect(frame);

        faceCharacteristics = new FaceCharacteristics(faceCropped);

        cheeks_pos=faceCharacteristics.getCheeks_pos();
        eyes_pos=faceCharacteristics.getEyes_pos();
        Log.v(LOG_TAG,"X: "+cheeks_pos[0][0]+"to"+cheeks_pos[1][0]);
        Log.v(LOG_TAG,"Y: "+cheeks_pos[0][1]+"to"+cheeks_pos[1][1]);
        detector.release();
        return Bitmap.createScaledBitmap(croppedBitmap,100,100,false);
    }

    Bitmap getScaledFlagBitmap(){
        InputStream stream = getResources().openRawResource(R.raw.flag);
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        return Bitmap.createScaledBitmap(bitmap,100,100,false);
    }

    void normalizeCheekPosition(){
        cheeks_pos[0][0]=(cheeks_pos[0][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        cheeks_pos[1][0]=(cheeks_pos[1][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        cheeks_pos[0][1]=(cheeks_pos[0][1]*resizedFaceBitmap.getHeight())/croppedBitmap.getHeight();
        cheeks_pos[1][1]=(cheeks_pos[1][1]*resizedFaceBitmap.getHeight())/croppedBitmap.getHeight();
        eyes_pos[0][0]=(eyes_pos[0][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        eyes_pos[1][0]=(eyes_pos[1][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        eyes_pos[0][1]=(eyes_pos[0][1]*resizedFaceBitmap.getHeight())/croppedBitmap.getHeight();
        eyes_pos[1][1]=(eyes_pos[1][1]*resizedFaceBitmap.getHeight())/croppedBitmap.getHeight();
        Log.v(LOG_TAG,"X: "+eyes_pos[0][0]+"to"+eyes_pos[1][0]);
        Log.v(LOG_TAG, "Y: " + eyes_pos[0][1] + "to" + eyes_pos[1][1]);
    }

    private class DownloadFilesTask extends AsyncTask<Bitmap, Integer, Bitmap> {
        protected Bitmap doInBackground(Bitmap... urls) {
            for(int i=0;i<100;i++){
                for(int j=0;j<100;j++){
                    int color= Color.argb(0,255,255,255);
                    transparent.setPixel(i,j,color);
                }
            }
            FaceBoundaryDetector faceBoundaryDetector=new FaceBoundaryDetector(resizedFaceBitmap,resizedFlagBitmap);
            Bitmap result=faceBoundaryDetector.getFaceWithFlag(resizedFaceBitmap, resizedFlagBitmap, cheeks_pos,eyes_pos,transparent);
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Bitmap result) {
            face_plus_flag_image.setImageBitmap(result);
        }
    }
}
