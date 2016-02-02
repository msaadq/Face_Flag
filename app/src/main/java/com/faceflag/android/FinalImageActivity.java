package com.faceflag.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class FinalImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.final_image_activity);

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageResource(R.drawable.bg_image);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_final_image, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_share:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                Uri uri = Uri.parse("android.resource://com.faceflag.android/drawable/bg_image");
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "I am supporting Peshawar Zalmi! Get " +
                        "your team's flag here: http://playstore.android.com/FaceFlag");
                sendIntent.setType("image/*");
                startActivity(Intent.createChooser(sendIntent, "share"));

                return true;

            case R.id.menu_item_save:
                return true;

            case R.id.menu_item_delete:

                //Create intent
                Intent intent = new Intent(FinalImageActivity.this, PhotoActivity.class);
                Toast.makeText(this, "Image has been deleted. Try out a new one!",
                        Toast.LENGTH_LONG).show();

                //Start Photo activity
                startActivity(intent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
