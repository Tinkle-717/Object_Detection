package com.example.yolo_picture_01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

//import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

public class Cropper extends AppCompatActivity {

//    private CropImageView cropImage;
//    private ImageView image;
    private Button button;
    private Bitmap cropped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);
        this.initViews();
        this.initListeners();
    }

    private void initViews(){

//        cropImage=(CropImageView)findViewById(R.id.cropImageView) ;
//        image=(ImageView)findViewById(R.id.image01);
        button=(Button)findViewById(R.id.button01);
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        Uri imageUri = Uri.parse(bundle.getString("URI"));
//        cropImage.setImageUriAsync(imageUri);
//        cropped = cropImage.getCroppedImage();
//        cropped = cropImage.getCroppedImage();







    }

    private void initListeners(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent( Cropper.this,ReadActivity.class);
                intent.putExtra("bitmap", cropped);
                startActivity(intent);
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                cropped.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                byte[] buf = out.toByteArray();
//                BitmapFactory.Options op = new BitmapFactory.Options();
//
//                op.inSampleSize = 2;
//
//                Bitmap bitmap = BitmapFactory.decodeByteArray(buf, 0, buf.length);
//                Intent intent=new Intent( Cropper.this,ReadActivity.class);
//                intent.putExtra("bitmap", cropped);
//                startActivity(intent);

//                DisplayMetrics dm = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(dm);
//                int screenWidth=dm.widthPixels;
//                if(cropped.getWidth()<=screenWidth){
//                    image.setImageBitmap(cropped);
//                }else{
//                    Bitmap bmp=Bitmap.createScaledBitmap(cropped, screenWidth, cropped.getHeight()*screenWidth/cropped.getWidth(), true);
//                    image.setImageBitmap(bmp);
//                }
//



            }
        });


    }

}