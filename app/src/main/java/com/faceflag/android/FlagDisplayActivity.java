package com.faceflag.android;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.ArrayList;

public class FlagDisplayActivity extends AppCompatActivity {

    int cheeks_pos[][];
    int eyes_pos[][];
    FaceCharacteristics faceCharacteristics;
    ImageView bitmap_image;
    GridView gridView;
    GridViewAdapter gridAdapter;
    public static Bitmap croppedBitmap;
    public static Bitmap resizedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flag_activity);

        Bitmap bitmap = PhotoActivity.originalImageBitmap;

        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //bitmap_image = (ImageView) findViewById(R.id.bitmap_image);

        // Create a frame from the bitmap and run face detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        Log.v("Faces Detected: ",String.valueOf(faces.size()));

        if (faces.size() == 0){
            Log.v("Inside ", "Zero Condition");

            //Create intent
            Intent intent = new Intent(FlagDisplayActivity.this, PhotoActivity.class);
            Toast.makeText(this, "We couldn't detect any faces. Please try uploading another " +
                            "image", Toast.LENGTH_LONG).show();
            //Start Flag Display activity
            startActivity(intent);
        }

        else {
            faceCharacteristics = new FaceCharacteristics(faces);

            // Get face features
            cheeks_pos = faceCharacteristics.getCheeks_pos();
            eyes_pos = faceCharacteristics.getEyes_pos();
            croppedBitmap = faceCharacteristics.getCroppedBitmap(bitmap);
            resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 100, 100, false);

            // Set bitmap in ImageView
            //bitmap_image.setImageBitmap(resizedBitmap);

            // Release resources associated with DetectorFace object
            detector.release();

            gridView = (GridView) findViewById(R.id.gridView);
            gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
            gridView.setAdapter(gridAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                    //Create intent
                    Intent intent = new Intent(FlagDisplayActivity.this, FinalImageActivity.class);
                    Log.v("Image: ", String.valueOf(item.getTitle()));
                    intent.putExtra("title", item.getTitle());

                    //Start details activity
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Prepare some dummy data for gridview
     */
    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);

        String titles[] = {getString(R.string.title_islamabad),getString(R.string.title_karachi),
                getString(R.string.title_lahor), getString(R.string.title_peshawar),
                getString(R.string.title_quetta)};

        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, titles[i]));
        }
        return imageItems;
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

