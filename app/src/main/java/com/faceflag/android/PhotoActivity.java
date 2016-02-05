package com.faceflag.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoActivity extends AppCompatActivity {

    public static final int GET_FROM_GALLERY = 3;
    public static final int REQUEST_TAKE_PHOTO = 1;

    private TextView headerText1;
    private TextView headerText2;
    private TextView footerText1;
    private TextView footerText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_gallery_activity);

        Button galleryButton = (Button) findViewById(R.id.gallary_button);

        headerText1=(TextView) findViewById(R.id.header_text_1);
        headerText2=(TextView) findViewById(R.id.header_text_2);
        footerText1=(TextView) findViewById(R.id.footer_text_1);
        footerText2=(TextView) findViewById(R.id.footer_text_2);
        setupStatusBar();
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/keepclam.ttf");
        headerText1.setTypeface(myTypeface);
        headerText2.setTypeface(myTypeface);
        footerText1.setTypeface(myTypeface);
        footerText2.setTypeface(myTypeface);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("PhotoActivity", "Clicked Gallery");
                //startActivityForResult(new Intent
                //        (Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
                dispatchTakePictureIntent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            Log.v("PhotoActivity", "Captured image");

            //Create intent
            Intent intent = new Intent(PhotoActivity.this, FlagDisplayActivity.class);
            intent.putExtra("URI", selectedImage.toString());
            //Start Flag Display activity
            startActivity(intent);
            Log.v("PHOTO ACTIVITY", " uri: " + selectedImage);
        }
    }

    public void setupStatusBar(){
        RelativeLayout.LayoutParams layoutParams=(RelativeLayout.LayoutParams) headerText1.getLayoutParams();
        layoutParams.topMargin+=getStatusBarHeight(getApplicationContext());
        headerText1.setLayoutParams(layoutParams);
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

    private String dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                String photoPath = "file:" + photoFile.getAbsolutePath();
                Log.v("Photo Path",photoPath);
                return dispatchTakePictureIntent();

            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.v("PhotoActivity","Image Captured.");
            }
        }
        return null;
    }

    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }


}
