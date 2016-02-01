package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
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

    int cheeks_pos[][];
    int eyes_pos[][];
    FaceCharacteristics faceCharacteristics;
    ImageView bitmap_image;
    Bitmap croppedBitmap;
    Bitmap resizedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStream stream = getResources().openRawResource(R.raw.image02);
        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        bitmap_image = (ImageView) findViewById(R.id.bitmap_image);

        // Create a frame from the bitmap and run face detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        faceCharacteristics = new FaceCharacteristics(faces);

        // Get face features
        cheeks_pos = faceCharacteristics.getCheeks_pos();
        eyes_pos = faceCharacteristics.getEyes_pos();
        croppedBitmap = faceCharacteristics.getCroppedBitmap(bitmap);
        resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 100, 100, false);

        FaceBoundaryDetector faceBoundaryDetector = new FaceBoundaryDetector(croppedBitmap, cheeks_pos[0], cheeks_pos[1]);
        int[] array = faceBoundaryDetector.getStandardColor();

        for (int number : array) {
            Log.e("number color", String.valueOf(number));
        }

        // Set bitmap in ImageView
        bitmap_image.setImageBitmap(resizedBitmap);

        // Release resources associated with DetectorFace object
        detector.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
