package com.faceflag.android;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PhotoActivity extends AppCompatActivity {

    public static final int GET_FROM_GALLERY = 3;
    public static Bitmap originalImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_gallery_activity);

        Button galleryButton = (Button) findViewById(R.id.button_gallery);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("PhotoActivity", "Clicked Gallery");
                startActivityForResult(new Intent
                        (Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            originalImageBitmap = null;
            try {
                originalImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                        selectedImage);
                Log.v("PhotoActivity", "Captured image");

                //Create intent
                Intent intent = new Intent(PhotoActivity.this, FlagDisplayActivity.class);

                //Start Flag Display activity
                startActivity(intent);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
