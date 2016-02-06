package com.faceflag.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AlphaAnimation;
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
import java.text.SimpleDateFormat;
import java.util.Date;

public class FinalImageActivity extends AppCompatActivity {
    public final String LOG_TAG="FACE FLAG";

    ImageView imageView;
    ImageButton deleteButton;
    ImageButton shareButton;
    ImageButton saveButton;
    ImageButton view1;
    ImageButton view2;
    ImageButton backButton;
    ProgressBar progressBar;
    Boolean isInVisible;
    int cheeks_pos[][];
    int eyes_pos[][];
    int nose_pos[];
    int croppingMetrics[];
    Double eulerY;
    Double eulerZ;
    String flagTitle;
    Bitmap normalFlag;
    Bitmap bigFlag;
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
    String mCurrentPhotoPath;
    File image;
    boolean isLoading;

    private int DEFAULT_POSITION_FLAG_CHEEKS=0;
    private int DEFAULT_POSITION_INFOGRAPHIC=1;
    private int DEFAULT_POSTION_FULL_FLAG_FILTER=2;

    int currentPosition;
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

        int imageIdLeft = R.raw.bean_right;
        int imageIdRight = R.raw.bean_left;
        int imageIdBackground=R.raw.team_bean;
        int bigFalgImageId=R.raw.image02;
        int normalFlagId=R.raw.image02;
        if(flagTitle.equals("PESHAWAR ZALMI")){
            imageIdLeft = R.raw.peshawar_left;
            imageIdRight=R.raw.peshawar_right;
            imageIdBackground=R.raw.team_peshwar;
            bigFalgImageId=R.raw.peshawar_zalmi;
            normalFlagId=R.raw.image_4;
        }else if(flagTitle.equals("ISLAMABAD UNITED")){
            imageIdLeft = R.raw.islamabad_left;
            imageIdRight=R.raw.islamabad_right;
            imageIdBackground=R.raw.team_islamabad;
            bigFalgImageId=R.raw.islamabad_united;
            normalFlagId=R.raw.image_1;
        }else if(flagTitle.equals("KARACHI KINGS")){
            imageIdLeft = R.raw.karachi_left;
            imageIdRight=R.raw.karachi_right;
            imageIdBackground=R.raw.team_karachi;
            bigFalgImageId=R.raw.karachi_kings;
            normalFlagId=R.raw.image_2;
        }else if(flagTitle.equals("LAHORE QALANDERS")){
            imageIdLeft = R.raw.lahore_left;
            imageIdRight=R.raw.lahore_right;
            imageIdBackground=R.raw.team_lahore;
            bigFalgImageId=R.raw.lahore_qalandars;
            normalFlagId=R.raw.image_3;
        }else if(flagTitle.equals("QUETTA GLADIATORS")){
            imageIdLeft = R.raw.quetta_left;
            imageIdRight=R.raw.quetta_right;
            imageIdBackground=R.raw.team_quetta;
            bigFalgImageId=R.raw.quetta_gladiators;
            normalFlagId=R.raw.image_5;
        }

        InputStream streamLeft = getResources().openRawResource(imageIdLeft);
        flagLeft = BitmapFactory.decodeStream(streamLeft);

        InputStream streamRight = getResources().openRawResource(imageIdRight);
        flagRight = BitmapFactory.decodeStream(streamRight);

        InputStream streamBackground = getResources().openRawResource(imageIdBackground);
        backgroundImage = BitmapFactory.decodeStream(streamBackground);

        InputStream streamBigFlag = getResources().openRawResource(bigFalgImageId);
        bigFlag = BitmapFactory.decodeStream(streamBigFlag);

        InputStream streamNormalFlag = getResources().openRawResource(normalFlagId);
        normalFlag = BitmapFactory.decodeStream(streamNormalFlag);
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

        backButton=(ImageButton) findViewById(R.id.back_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#2569c9"),
                PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.VISIBLE);

        isLoading=true;
        layoutTop = (LinearLayout) findViewById(R.id.layout_top);
        layoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        layoutTop.setVisibility(View.INVISIBLE);
        layoutBottom.setVisibility(View.INVISIBLE);

        isInVisible=true;

        relativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
        imageView=(ImageView) findViewById(R.id.image);
        deleteButton=(ImageButton) findViewById(R.id.delete_button);
        shareButton=(ImageButton) findViewById(R.id.share_button);
        saveButton=(ImageButton) findViewById(R.id.save_button);
        view1 = (ImageButton) findViewById(R.id.view_1);
        view2 = (ImageButton) findViewById(R.id.view_2);
        backButton.setVisibility(View.INVISIBLE);
        view2.setVisibility(View.INVISIBLE);
        view1.setVisibility(View.INVISIBLE);
        shareButton.setClickable(false);
        saveButton.setClickable(false);
        deleteButton.setClickable(false);
        view1.setClickable(false);
        view2.setClickable(false);
        backButton.setClickable(false);

    }

    void setOnClickListerners(){
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                if (!isLoading) {
                    if (isInVisible) {
                        isInVisible = false;
                        transitionOutFullScreen();
                        shareButton.setClickable(true);
                        saveButton.setClickable(true);
                        deleteButton.setClickable(true);
                        view1.setClickable(true);
                        view2.setClickable(true);
                        backButton.setClickable(true);
                    } else if (!isInVisible) {
                        isInVisible = true;
                        transitionIntoFullScreen();
                        shareButton.setClickable(false);
                        saveButton.setClickable(false);
                        deleteButton.setClickable(false);
                        view1.setClickable(false);
                        view2.setClickable(false);
                        backButton.setClickable(false);
                    }
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


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void saveImage() {

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        try {
            image = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                /* 100 to keep full quality of the image */
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, imageFileName , " ");
                outStream.flush();
                outStream.close();
                success = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.v(LOG_TAG,e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.v(LOG_TAG, e.toString());
            }

            if (success) {
                Toast.makeText(getApplicationContext(), "Image saved with success. Image was Saved In the \"Pictures\" directory.",
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
        //sendIntent.putExtra(Intent.EXTRA_STREAM,file.getPath());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
          sendIntent.putExtra(Intent.EXTRA_TEXT, "I am supporting " + flagTitle + "! Get " +
                  "your team's flag here: http://playstore.android.com/FaceFlag");
        sendIntent.setType("image/jpeg");
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
            CheekFlagOverlay cheekFlagOverlay = new CheekFlagOverlay(
                    normalFlag,
                    bigFlag,
                    backgroundImage,
                    face,flagLeft,
                    flagRight,
                    cheeks_pos[0],
                    cheeks_pos[1],
                    nose_pos,eyes_pos[0],
                    eyes_pos[1],
                    eulerY,
                    eulerZ,
                    croppingMetrics
            );

            detector.release();
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
            currentPosition=DEFAULT_POSITION_FLAG_CHEEKS;
            layoutTop.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            transitionOutFullScreen();

            isInVisible=false;
            isLoading=false;
            //make the buttons clickable
            shareButton.setClickable(true);
            saveButton.setClickable(true);
            deleteButton.setClickable(true);
            view1.setClickable(true);
            view2.setClickable(true);
            backButton.setClickable(true);
            backButton.setVisibility(View.VISIBLE);
            view2.setVisibility(View.VISIBLE);
            SaveImage(result[currentPosition]);
            imageView.setImageBitmap(result[currentPosition]);

            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentPosition==DEFAULT_POSTION_FULL_FLAG_FILTER)
                    {
                        SaveImage(result[DEFAULT_POSITION_INFOGRAPHIC]);
                        imageView.setImageBitmap(result[DEFAULT_POSITION_INFOGRAPHIC]);
                        fadeInImageButton(view2);
                        view2.setVisibility(View.VISIBLE);
                        currentPosition=DEFAULT_POSITION_INFOGRAPHIC;
                    }else if(currentPosition==DEFAULT_POSITION_INFOGRAPHIC){
                        SaveImage(result[DEFAULT_POSITION_FLAG_CHEEKS]);
                        imageView.setImageBitmap(result[DEFAULT_POSITION_FLAG_CHEEKS]);
                        fadeOutImageButton(view1);
                        view1.setVisibility(View.INVISIBLE);
                        currentPosition=DEFAULT_POSITION_FLAG_CHEEKS;
                    }

                }
            });

            view2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentPosition==DEFAULT_POSITION_FLAG_CHEEKS)
                    {
                        SaveImage(result[DEFAULT_POSITION_INFOGRAPHIC]);
                        imageView.setImageBitmap(result[DEFAULT_POSITION_INFOGRAPHIC]);
                        fadeInImageButton(view1);
                        view1.setVisibility(View.VISIBLE);
                        currentPosition=DEFAULT_POSITION_INFOGRAPHIC;
                    }else if(currentPosition==DEFAULT_POSITION_INFOGRAPHIC){
                        SaveImage(result[DEFAULT_POSTION_FULL_FLAG_FILTER]);
                        imageView.setImageBitmap(result[DEFAULT_POSTION_FULL_FLAG_FILTER]);
                        fadeOutImageButton(view2);
                        view2.setVisibility(View.INVISIBLE);
                        currentPosition=DEFAULT_POSTION_FULL_FLAG_FILTER;
                    }
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
        Log.v(LOG_TAG, "X: " + eyes_pos[0][0] + "to" + eyes_pos[1][0]);
        Log.v(LOG_TAG, "Y: " + eyes_pos[0][1] + "to" + eyes_pos[1][1]);
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

        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setFillAfter(true);
        animation.setDuration(250);

        //apply the animation ( fade In ) to your LAyout
        layoutTop.startAnimation(animation);
        layoutBottom.startAnimation(animation);
        backButton.startAnimation(animation);
    }


    private void transitionIntoFullScreen(){

        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setFillAfter(true);
        animation.setDuration(250);

        //apply the animation ( fade In ) to your LAyout
        layoutTop.startAnimation(animation);
        layoutBottom.startAnimation(animation);
        backButton.startAnimation(animation);
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
