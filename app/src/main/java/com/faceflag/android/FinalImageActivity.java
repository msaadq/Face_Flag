package com.faceflag.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    int cheeks_pos[][];
    int eyes_pos[][];
    Bitmap resizedFaceBitmap;
    Bitmap resizedFlagBitmap;
    Bitmap croppedBitmap;
    Bitmap face;
    Bitmap transparent;
    Bitmap flag;
    FaceCharacteristics faceCharacteristics;
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
        try {
            face= MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    Uri.parse(selectedImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream stream = getResources().openRawResource(R.raw.face);
        face = BitmapFactory.decodeStream(stream);

        resizedFaceBitmap=getScaledFaceBitmap();
        resizedFlagBitmap=getScaledFlagBitmap();
        normalizeCheekPosition();
        AddFlagOnFace addFlagOnFace=new AddFlagOnFace();
        addFlagOnFace.execute("start");
    }

    void bindViews(){
        imageView=(ImageView) findViewById(R.id.image);
        deleteButton=(ImageButton) findViewById(R.id.delete_button);
        shareButton=(ImageButton) findViewById(R.id.share_button);
        saveButton=(ImageButton) findViewById(R.id.save_button);
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
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I am supporting Peshawar Zalmi! Get " +
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

    private class AddFlagOnFace extends AsyncTask<String, Integer, Bitmap> {
        protected Bitmap doInBackground(String... uris) {

            FaceBoundaryDetector faceBoundaryDetector=new FaceBoundaryDetector(resizedFaceBitmap,resizedFlagBitmap);
            return faceBoundaryDetector.getFaceWithFlag(resizedFaceBitmap, resizedFlagBitmap, cheeks_pos,eyes_pos,transparent);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Bitmap result) {
            SaveImage(result);
            imageView.setImageBitmap(result);
        }
    }

    Bitmap getScaledFaceBitmap(){
        InputStream stream = getResources().openRawResource(R.raw.face);
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
        InputStream stream = getResources().openRawResource(R.raw.image_1);
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
}
