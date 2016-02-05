package com.faceflag.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.ArrayList;

public class FlagDisplayActivity extends AppCompatActivity {

    private static final String LOG_TAG="FACE FLAG FLAGDISPLAY";
    private int cheeks_pos[][];
    private int eyes_pos[][];
    private FaceCharacteristics faceCharacteristics;
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private Bitmap croppedBitmap;
    //private Bitmap resizedBitmap;
    private ImageButton cancelPickFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.flag_activity);
        setupStatusBar();

        String imageUriString=getIntent().getStringExtra("URI");
        final Uri selectedImage=Uri.parse(imageUriString);
        Log.v(LOG_TAG," uri: "+selectedImage);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cancelPickFlag=(ImageButton) findViewById(R.id.cancel_pick_flag);
        cancelPickFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();


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

            //Terminate the already existing activities in stack
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //Start Photo activity
            startActivity(intent);
        }

        else {

            gridView = (GridView) findViewById(R.id.gridView);
            gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
            gridView.setAdapter(gridAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                    //Create intent
                    Intent intent = new Intent(FlagDisplayActivity.this, FinalImageActivity.class);
                    Log.v("Image: ", String.valueOf(item.getTitle()));
                    intent.putExtra("title", String.valueOf(item.getTitle()));
                    intent.putExtra("URI",selectedImage.toString());
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
    public void setupStatusBar(){

        LinearLayout linearLayout=(LinearLayout) findViewById(R.id.flag_root_layout);
        // Set the padding to match the Status Bar height
        linearLayout.setPadding(0, getStatusBarHeight(this), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setStatusBarAlpha(0.2f);
        tintManager.setNavigationBarAlpha(0.2f);
        tintManager.setTintAlpha(0.2f);
        tintManager.setTintColor(Color.parseColor("#0069FF"));

    }
    // A method to find height of the status bar
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}

