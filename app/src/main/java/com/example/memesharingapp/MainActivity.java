package com.example.memesharingapp;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    ImageView memeimageView;
    ProgressBar progressBar;
    String currentImageUrl;
    Button sharebutton;
    String fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        memeimageView  = findViewById(R.id.memeimageView);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);
        sharebutton = (Button)findViewById(R.id.sharebutton);
        load();
        sharebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareMeme(v);
            }
        });
    }

    public void shareMeme(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT , "check this meme "+currentImageUrl );
        intent.setType("text/plain");
        Intent chooser = Intent.createChooser(intent , "share using...");
        startActivity(chooser);
    }
    public void shareimage(View view){
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) memeimageView.getDrawable());
        Bitmap bitmap = bitmapDrawable .getBitmap();
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"some title", null);
        Uri bitmapUri = Uri.parse(bitmapPath);
        Intent shareIntent=new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(shareIntent,"Share Image"));
    }

    public void share(View view){
        Picasso.get().load(currentImageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    File mydir = new File(Environment.getExternalStorageDirectory() + "/memeapp");
                    if (!mydir.exists()) {
                        mydir.mkdirs();
                    }

                    fileUri = mydir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpeg";
                    FileOutputStream outputStream = new FileOutputStream(fileUri);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(getApplicationContext() , "Something went wrong" , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Toast.makeText(getApplicationContext() , "preparing to send" , Toast.LENGTH_LONG).show();
            }
        });
    }
    public Uri getlocalBitmapUri(Bitmap bitmap){
        Uri bmuri = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
            bmuri = Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return bmuri;

    }


    public void nextMeme(View view){
        load();
    }

    private void load() {
        progressBar.setVisibility(View.VISIBLE);
//        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://meme-api.herokuapp.com/gimme";

// Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null ,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                             currentImageUrl = response.getString("url");
                            Picasso.get().load(currentImageUrl).into(memeimageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext() , "Something went wrong" , Toast.LENGTH_LONG).show();
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

// Add the request to the RequestQueue.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}
