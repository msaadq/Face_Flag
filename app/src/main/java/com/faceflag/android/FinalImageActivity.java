package com.faceflag.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FinalImageActivity extends AppCompatActivity {

    ImageView imageView;
    ImageButton deleteButton;
    ImageButton shareButton;
    ImageButton saveButton;
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
        imageView.setImageResource(R.drawable.bg_image);
        setOnClickListerners();


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
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        Uri uri = Uri.parse("android.resource://com.faceflag.android/drawable/bg_image");
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

        //Start Photo activity
        startActivity(intent);
    }

}
