package com.mustafakamber.citiesofturkey.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mustafakamber.citiesofturkey.R;
import com.mustafakamber.citiesofturkey.databinding.ActivityDetailsBinding;
import com.mustafakamber.citiesofturkey.model.City;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;
    private ActivityDetailsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Cities",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){

            binding.cityText.setText("");
            binding.regionText.setText("");
            binding.codeText.setText("");
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);
        }else{
            int cityId = intent.getIntExtra("cityId",1);
            binding.saveButton.setVisibility(View.INVISIBLE);
            try{
                Cursor cursor = database.rawQuery("SELECT * FROM cities WHERE id = ?",new String[] {String.valueOf(cityId)});
                int cityNameIx = cursor.getColumnIndex("cityName");
                int regionNameIx = cursor.getColumnIndex("regionName");
                int cityCodeIx = cursor.getColumnIndex("cityCode");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                   binding.cityText.setText(cursor.getString(cityNameIx));
                   binding.regionText.setText(cursor.getString(regionNameIx));
                   binding.codeText.setText(cursor.getString(cityCodeIx));

                   byte[] bytes = cursor.getBlob(imageIx);
                   Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                   binding.imageView.setImageBitmap(bitmap);
                }

              cursor.close();

            }catch(Exception e){
                e.printStackTrace();
            }

        }






    }
    public void save(View view){
         String cityName = binding.cityText.getText().toString();
         String regionName = binding.regionText.getText().toString();
         String cityCode = binding.codeText.getText().toString();

         Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try{

            database.execSQL("CREATE TABLE IF NOT EXISTS cities (id INTEGER PRIMARY KEY,cityName VARCHAR, regionName VARCHAR, cityCode VARCHAR, image BLOB)");


             String sqlString = "INSERT INTO cities (cityName, regionName, cityCode, image) VALUES (?, ?, ?, ?)";
             SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
             sqLiteStatement.bindString(1,cityName);
             sqLiteStatement.bindString(2,regionName);
             sqLiteStatement.bindString(3,cityCode);
             sqLiteStatement.bindBlob(4,byteArray);
             sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent= new Intent(DetailsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public void selectImage(View view){
       if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
           if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
               Snackbar.make(view,"Permission needed for gallery!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       //Izin alinacak
                       permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                   }
               }).show();
           }else{
               //Izin alinacak
               permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
           }
       }else{
           //Galeriye gidilecek
           Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
           activityResultLauncher.launch(intentToGallery);
       }
    }

    public void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
               if(result.getResultCode() == Activity.RESULT_OK){
                   Intent intentFromResult = result.getData();
                   if(intentFromResult != null){
                       Uri imageData = intentFromResult.getData();

                       try{
                           if(Build.VERSION.SDK_INT >= 28){
                               ImageDecoder.Source source = ImageDecoder.createSource(DetailsActivity.this.getContentResolver(),imageData);
                               selectedImage = ImageDecoder.decodeBitmap(source);
                               binding.imageView.setImageBitmap(selectedImage);
                           }else{
                               selectedImage = MediaStore.Images.Media.getBitmap(DetailsActivity.this.getContentResolver(),imageData);
                               binding.imageView.setImageBitmap(selectedImage);
                           }
                       }catch (Exception e){
                           e.printStackTrace();
                       }
                   }
               }
            }
        });




        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){

                    Intent intentToGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else{

                    Toast.makeText(DetailsActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

}