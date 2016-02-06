package com.faceflag.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PhotoActivity extends AppCompatActivity {

    public static final int GET_FROM_GALLERY = 3;

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

}
