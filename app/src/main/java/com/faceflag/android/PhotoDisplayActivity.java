package com.faceflag.android;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class PhotoDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_display_activity);

        Bitmap bitmap = getIntent().getParcelableExtra("image");

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText("Hey");

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

    }
}
