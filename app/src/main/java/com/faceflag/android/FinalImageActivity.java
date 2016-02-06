package com.faceflag.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Random;

public class FinalImageActivity extends AppCompatActivity {
    public final String LOG_TAG="FACE FLAG";

    ImageView imageView;
    ImageButton deleteButton;
    ImageButton shareButton;
    ImageButton saveButton;
    ImageButton view1;
    ImageButton view2;
    ProgressBar progressBar;
    Boolean isInVisible;
    int cheeks_pos[][];
    int eyes_pos[][];
    int nose_pos[];
    int croppingMetrics[];
    Double eulerY;
    Double eulerZ;
    String flagTitle;
    Bitmap resizedFaceBitmap;
    Bitmap croppedBitmap;
    Bitmap face;
    Bitmap transparent;
    Bitmap flagLeft;
    Bitmap flagRight;
    Bitmap backgroundImage;
    FaceCharacteristics faceCharacteristics;
    LinearLayout layoutTop;
    LinearLayout layoutBottom;
    RelativeLayout relativeLayout;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.final_image_activity);

        bindViews();
        setOnClickListerners();
        String selectedImage=getIntent().getStringExtra("URI");
        flagTitle = getIntent().getStringExtra("title");

        int imageIdLeft = R.raw.image02;
        int imageIdRight = R.raw.image02;
        int imageIdBackground=R.raw.image02;
        if(flagTitle.equals("PESHAWAR ZALMI")){
            imageIdLeft = R.raw.peshawar_left;
            imageIdRight=R.raw.peshawar_right;
            imageIdBackground=R.raw.team_peshwar;
        }else if(flagTitle.equals("ISLAMABAD UNITED")){
            imageIdLeft = R.raw.islamabad_left;
            imageIdRight=R.raw.islamabad_right;
            imageIdBackground=R.raw.team_islamabad;
        }else if(flagTitle.equals("KARACHI KINGS")){
            imageIdLeft = R.raw.karachi_left;
            imageIdRight=R.raw.karachi_right;
            imageIdBackground=R.raw.team_karachi;
        }else if(flagTitle.equals("LAHORE QALANDERS")){
            imageIdLeft = R.raw.lahore_left;
            imageIdRight=R.raw.lahore_right;
            imageIdBackground=R.raw.team_lahore;
        }else if(flagTitle.equals("QUETTA GLADIATORS")){
            imageIdLeft = R.raw.quetta_left;
            imageIdRight=R.raw.quetta_right;
            imageIdBackground=R.raw.team_quetta;
        }

        InputStream streamLeft = getResources().openRawResource(imageIdLeft);
        flagLeft = BitmapFactory.decodeStream(streamLeft);

        InputStream streamRight = getResources().openRawResource(imageIdRight);
        flagRight = BitmapFactory.decodeStream(streamRight);

        InputStream streamBackground = getResources().openRawResource(imageIdBackground);
        backgroundImage = BitmapFactory.decodeStream(streamBackground);

        try {
            face= MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    Uri.parse(selectedImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //InputStream stream = getResources().openRawResource(R.raw.face);
        //face = BitmapFactory.decodeStream(stream);



        //resizedFaceBitmap=getScaledFaceBitmap();
        //resizedFlagBitmap=getScaledFlagBitmap();
        //normalizeCheekPosition();
        AddFlagOnFace addFlagOnFace=new AddFlagOnFace();
        addFlagOnFace.execute("start");

    }

    void bindViews(){

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#2569c9"),
                PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.VISIBLE);

        layoutTop = (LinearLayout) findViewById(R.id.layout_top);
        layoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        layoutTop.setVisibility(View.VISIBLE);
        layoutBottom.setVisibility(View.VISIBLE);
        isInVisible=false;

        relativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
        imageView=(ImageView) findViewById(R.id.image);
        deleteButton=(ImageButton) findViewById(R.id.delete_button);
        shareButton=(ImageButton) findViewById(R.id.share_button);
        saveButton=(ImageButton) findViewById(R.id.save_button);
        view1 = (ImageButton) findViewById(R.id.view_1);
        view2 = (ImageButton) findViewById(R.id.view_2);

        view2.setVisibility(View.INVISIBLE);
        view1.setVisibility(View.INVISIBLE);

    }

    void setOnClickListerners(){
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });


        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInVisible) {
                    isInVisible = false;
                    transitionOutFullScreen();
                }
                else if(!isInVisible){
                    isInVisible = true;
                    transitionIntoFullScreen();
                }
            }

        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
    }

    private void saveImage() {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File image = new File(sdCardDirectory, "FaceFlag.png");

        boolean success = false;
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            Log.v("Coming here", "sjjs");
            // Encode the file as a PNG image.
            FileOutputStream outStream;
            try {

                outStream = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                /* 100 to keep full quality of the image */

                outStream.flush();
                outStream.close();
                success = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (success) {
                Toast.makeText(getApplicationContext(), "Image saved with success",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Error during image saving", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void shareImage() {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        int n = 10000;
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);



        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        Uri uri = Uri.parse(file.getPath());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I am supporting" + flagTitle + "! Get " +
                "your team's flag here: http://playstore.android.com/FaceFlag");
        sendIntent.setType("image/*");
        startActivity(Intent.createChooser(sendIntent, "share"));
    }

    private void deleteImage(){
        //Create intent
        Intent intent = new Intent(FinalImageActivity.this, PhotoActivity.class);
        Toast.makeText(this, "Image has been deleted. Try out a new one!",
                Toast.LENGTH_LONG).show();
        //Terminate the already existing activities in stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Start Photo activity
        startActivity(intent);
    }

    private class AddFlagOnFace extends AsyncTask<String, Integer, Bitmap[]> {
        protected Bitmap[] doInBackground(String... uris) {

            FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .build();

            bindViews();

            // Create a frame from the bitmap and run face detection on the frame.
            Frame frame = new Frame.Builder().setBitmap(face).build();
            SparseArray<Face> faces = detector.detect(frame);

            faceCharacteristics = new FaceCharacteristics(faces);

            cheeks_pos = faceCharacteristics.getCheeksOriginalPos();
            eyes_pos = faceCharacteristics.getEyesOriginalPos();
            nose_pos = faceCharacteristics.getNoseOriginalPos();
            eulerY=faceCharacteristics.getFaceEulerY();
            eulerZ=faceCharacteristics.getFaceEulerZ();
            if(eulerY==null){
                eulerY=0.0;
            }
            if(eulerZ==null){
                eulerZ=0.0;
            }
            croppingMetrics=faceCharacteristics.getCroppingMetrics();
            CheekFlagOverlay cheekFlagOverlay = new CheekFlagOverlay(backgroundImage,face,flagLeft,flagRight,cheeks_pos[0],
                    cheeks_pos[1],nose_pos,eyes_pos[0],eyes_pos[1],eulerY,eulerZ,croppingMetrics);

            return cheekFlagOverlay.overlayFlag();

            //FlagOverlay flagOverlay=new FlagOverlay(resizedFaceBitmap,resizedFlagBitmap,cheeks_pos[0],
            //        cheeks_pos[1],eyes_pos[0],eyes_pos[1]);
            //return flagOverlay.getFlagOnFaceRough();
            //FaceBoundaryDetector faceBoundaryDetector=new FaceBoundaryDetector(resizedFaceBitmap,resizedFlagBitmap);
            //return faceBoundaryDetector.getFaceWithFlag(resizedFaceBitmap, resizedFlagBitmap, cheeks_pos,eyes_pos,transparent);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(final Bitmap[] result) {

            progressBar.setVisibility(View.INVISIBLE);

            view2.setVisibility(View.VISIBLE);
            SaveImage(result[0]);
            imageView.setImageBitmap(result[0]);

            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SaveImage(result[0]);
                    imageView.setImageBitmap(result[0]);
                    fadeOutImageButton(view1);
                    view1.setVisibility(View.INVISIBLE);
                    fadeInImageButton(view2);
                    view2.setVisibility(View.VISIBLE);
                }
            });

            view2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SaveImage(result[1]);
                    imageView.setImageBitmap(result[1]);
                    fadeOutImageButton(view2);
                    view2.setVisibility(View.INVISIBLE);
                    fadeInImageButton(view1);
                    view1.setVisibility(View.VISIBLE);
                }
            });


        }
    }

    Bitmap getScaledFaceBitmap(){
        //InputStream stream = getResources().openRawResource(R.raw.face);
        //Bitmap bitmap = BitmapFactory.decodeStream(stream);

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
        Frame frame = new Frame.Builder().setBitmap(face).build();
        SparseArray<Face> faces = detector.detect(frame);

        faceCharacteristics = new FaceCharacteristics(faces);

        croppedBitmap = faceCharacteristics.getCroppedBitmap(face);


        // Get face features
        cheeks_pos = faceCharacteristics.getCheeks_pos();
        Log.v(LOG_TAG, "Cheeks X: " + cheeks_pos[0][0] + "to" + cheeks_pos[1][0]);
        Log.v(LOG_TAG, "Cheeks Y: " + cheeks_pos[0][1] + "to" + cheeks_pos[1][1]);

        eyes_pos = faceCharacteristics.getEyes_pos();

        detector.release();

        return croppedBitmap;

    }

    Bitmap getScaledFlagBitmap(){
        InputStream stream = getResources().openRawResource(R.raw.image_1);
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        return Bitmap.createScaledBitmap(bitmap,200,200,false);
    }


    void normalizeCheekPosition(){
        cheeks_pos[0][0]=(cheeks_pos[0][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        cheeks_pos[1][0]=(cheeks_pos[1][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        cheeks_pos[0][1]=(cheeks_pos[0][1]*resizedFaceBitmap.getHeight())/croppedBitmap.getHeight();
        cheeks_pos[1][1]=(cheeks_pos[1][1]*resizedFaceBitmap.getHeight())/croppedBitmap.getHeight();
        eyes_pos[0][0]=(eyes_pos[0][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        eyes_pos[1][0]=(eyes_pos[1][0]*resizedFaceBitmap.getWidth())/croppedBitmap.getWidth();
        eyes_pos[0][1]=(eyes_pos[0][1]*resizedFaceBitmap.getHeight())/croppedBitmap.getHeight();
        eyes_pos[1][1]=(eyes_pos[1][1]*resizedFaceBitmap.getHeight()) / croppedBitmap.getHeight();
        Log.v(LOG_TAG,"X: "+eyes_pos[0][0] + "to" + eyes_pos[1][0]);
        Log.v(LOG_TAG, "Y: " + eyes_pos[0][1] + "to" + eyes_pos[1][1]);
    }

    public static File writebitmaptofilefirst(String filename, String source) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extStorageDirectory + "/temp_images");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
        OutputStream outStream = null;


        File file = new File(mFolder.getAbsolutePath(), filename + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, filename + ".png");
            Log.e("file exist", "" + file + ",Bitmap= " + filename);
        }
        try {
            URL url = new URL(source);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("file", "" + file);
        return file;

    }


    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        int n = 10000;
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transitionOutFullScreen(){

        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setFillAfter(true);
        animation.setDuration(750);

        //apply the animation ( fade In ) to your LAyout
        layoutTop.startAnimation(animation);
        layoutBottom.startAnimation(animation);
    }


    private void transitionIntoFullScreen(){

        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setFillAfter(true);
        animation.setDuration(750);

        //apply the animation ( fade In ) to your LAyout
        layoutTop.startAnimation(animation);
        layoutBottom.startAnimation(animation);
    }

    private void fadeOutImageButton(ImageButton button) {

        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setFillAfter(true);
        animation.setDuration(250);

        //apply the animation ( fade In ) to your LAyout
        button.startAnimation(animation);
    }

    private void fadeInImageButton(ImageButton button) {

        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setFillAfter(true);
        animation.setDuration(250);

        //apply the animation ( fade In ) to your LAyout
        button.startAnimation(animation);
    }





}
