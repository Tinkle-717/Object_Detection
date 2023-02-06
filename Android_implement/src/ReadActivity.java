package com.example.yolo_picture_01;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.internal.SafeIterableMap;
import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

//import com.theartofdev.edmodo.cropper.CropImageView;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ReadActivity extends AppCompatActivity implements Runnable {
    private Button button1,button2,button0;
    private ImageView image;
    private ResultView mResultView;
    private TextView text;
    private Spinner spinner1;
    private ArrayAdapter<String> adapter1;
    private static final int GALLERY_CODE = 1;
    private static final int CROP_CODE = 2;
    private Uri cropImageUri;
//    private CropImageView cropImage;
    private Uri imageUri;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private float[] arr;
    Uri dataURI = null;
    MyTask mTask;
    Bitmap dataBitmap = null;
    ProgressBar mProgressBar;
    String storagePath;
    Uri ImageUri;

    private static final String[] WAYS = {"切片", "重采样"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        this.initViews();
        this.initListeners();
        try {
//            if(spinner1.getSelectedItemPosition()==0){
//                mModule = LiteModuleLoader.load(ReadActivity.assetFilePath(getApplicationContext(), "test640(1).ptl"));
//                System.out.println(spinner1.getSelectedItemPosition()+"******************************");
//            }else if (spinner1.getSelectedItemPosition()==1){
//                mModule = LiteModuleLoader.load(ReadActivity.assetFilePath(getApplicationContext(), "test2016(1).ptl"));
//                System.out.println(spinner1.getSelectedItemPosition()+"******************************");
//            }
//            mModule = LiteModuleLoader.load(ReadActivity.assetFilePath(getApplicationContext(), "best819(1).torchscript.ptl"));
            mModule = LiteModuleLoader.load(ReadActivity.assetFilePath(getApplicationContext(), "test2016(1).ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
        }
    }
    private void initViews(){
        adapter1 = new ArrayAdapter<String>(ReadActivity.this, android.R.layout.simple_spinner_item, WAYS);
        spinner1 = (Spinner) findViewById(R.id.spinner01);
        spinner1.setAdapter(adapter1);
        button0=(Button)findViewById(R.id.button00);
        button1=(Button)findViewById(R.id.button01);
        button2=(Button)findViewById(R.id.button02);
        text=(TextView)findViewById(R.id.text01);
        image=(ImageView)findViewById(R.id.imageView);
//        cropImage=(CropImageView)findViewById(R.id.cropImageView) ;
//        Bitmap receive=(Bitmap)(getIntent().getParcelableExtra("bitmap"));
//        image.setImageBitmap(receive);
        mProgressBar = findViewById(R.id.progressBar);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);
        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open("test1.png"));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
        image.setImageBitmap(mBitmap);
    }
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
    @Override
    public void run() {
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
//        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
//        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
//        final Tensor outputTensor = outputTuple[0].toTensor();
//        final float[] outputs = outputTensor.getDataAsFloatArray();
//        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);
        arr = Detect.array(mBitmap,image);
        final ArrayList<Result> results = Detect.res(mBitmap,mModule,arr);
        runOnUiThread(() -> {
            button2.setEnabled(true);
            button2.setText("计算");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            text.setText( Integer.toString(results.size()));
        });

//        if(spinner1.getSelectedItemPosition()==0){
//            final ArrayList<Result> results = Detect.handleResult(mBitmap,mModule);
//            runOnUiThread(() -> {
//                button2.setEnabled(true);
//                button2.setText("计算");
//                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
////            mResultView.setResults(results);
////            mResultView.invalidate();
////            mResultView.setVisibility(View.VISIBLE);
//                text.setText( Integer.toString(results.size()));
//            });
//        }else if (spinner1.getSelectedItemPosition()==1){
//            arr = Detect.array(mBitmap,image);
//            final ArrayList<Result> results = Detect.res(mBitmap,mModule,arr);
//            runOnUiThread(() -> {
//                button2.setEnabled(true);
//                button2.setText("计算");
//                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
////            mResultView.setResults(results);
////            mResultView.invalidate();
////            mResultView.setVisibility(View.VISIBLE);
//                text.setText( Integer.toString(results.size()));
//            });
//        }
    }
    private void initListeners(){
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);
                text.setText("");
                final CharSequence[] options = { "相册", "拍照", "关闭" };
                AlertDialog.Builder builder = new AlertDialog.Builder(ReadActivity.this);
                builder.setTitle("方式");
//
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("拍照")) {
                            getPath();
                            File outputImage = new File(storagePath);
                            try {
                                //创建一个文件，等待输入流
                                outputImage.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //第二个参数与provider的authorities属性一致
                            ImageUri = FileProvider.getUriForFile(ReadActivity.this,"com.example.yolo_picture_01.provider",outputImage);
                            //直接使用隐式Intent的方式去调用相机，就不需要再去申请相机权限
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            //指定拍照的输出地址，当向intent传入MEdiaStore.Exter_OUTPUT参数后，表明这是一个存储动作，相机拍摄到的图片会直接存储到相应路径，不会缓存在内存中
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,ImageUri);
                            //第二个参数为requestCode，他的值必须大于等于0，否则就不会回调
                            startActivityForResult(intent, 0);
//                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                            startActivityForResult(takePicture, 0);
                        }
                        else if (options[item].equals("相册")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , 1);
                        }
                        else if (options[item].equals("关闭")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

//                getPath();
//                File outputImage = new File(storagePath);
//                try {
//                    //创建一个文件，等待输入流
//                    outputImage.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //第二个参数与provider的authorities属性一致
//                ImageUri = FileProvider.getUriForFile(ReadActivity.this,"com.example.yolo_picture_01.provider",outputImage);
//                //直接使用隐式Intent的方式去调用相机，就不需要再去申请相机权限
//                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//                //指定拍照的输出地址，当向intent传入MEdiaStore.Exter_OUTPUT参数后，表明这是一个存储动作，相机拍摄到的图片会直接存储到相应路径，不会缓存在内存中
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,ImageUri);
//                //第二个参数为requestCode，他的值必须大于等于0，否则就不会回调
//                startActivityForResult(intent, 0);
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCrop();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);
                button2.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                button2.setText("等待");

//                mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
//                mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;
//
//                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)image.getWidth() / mBitmap.getWidth() : (float)image.getHeight() / mBitmap.getHeight());
//                mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)image.getHeight() / mBitmap.getHeight() : (float)image.getWidth() / mBitmap.getWidth());
//
//                mStartX = (image.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
//                mStartY = (image.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;

                Thread thread = new Thread(ReadActivity.this);
                thread.start();

            }
        });
//        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//                    try {
//                        mModule = LiteModuleLoader.load(ReadActivity.assetFilePath(getApplicationContext(), "test640(1).ptl"));
//                        text.setText("");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                else if (position == 1) {
//                    try {
//                        mModule = LiteModuleLoader.load(ReadActivity.assetFilePath(getApplicationContext(), "test2016(1).ptl"));
//                        text.setText("");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
    }

    private void startCrop(){
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), System.currentTimeMillis()+".jpeg"));
////        UCrop.of(dataURI, destinationUri).withAspectRatio(16, 9).withMaxResultSize(300, 300).start(this);
        UCrop uCrop = UCrop.of(dataURI, destinationUri);
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    private void getPath(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
                File.separator + "DCIM/Camera" + File.separator + timeStamp + ".jpg";
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(storagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

private class MyTask extends AsyncTask<Uri, Integer, Bitmap>{

    protected Bitmap ImageSizeCompress(Uri uri){
        InputStream Stream = null;
        InputStream inputStream = null;
        try {
            //根据uri获取图片的流
            inputStream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options的in系列的设置了，injustdecodebouond只解析图片的大小，而不加载到内存中去
            options.inJustDecodeBounds = true;
            //1.如果通过options.outHeight获取图片的宽高，就必须通过decodestream解析同options赋值
            //否则options.outheight获取不到宽高
            BitmapFactory.decodeStream(inputStream,null,options);
            //2.通过 btm.getHeight()获取图片的宽高就不需要1的解析，我这里采取第一张方式
//            Bitmap btm = BitmapFactory.decodeStream(inputStream);
            //以屏幕的宽高进行压缩
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;
            //获取图片的宽高
            int outHeight = options.outHeight;
            int outWidth = options.outWidth;
            //heightPixels就是要压缩后的图片高度，宽度也一样
            int a = (int) Math.ceil((outHeight/(float)heightPixels));
            int b = (int) Math.ceil(outWidth/(float)widthPixels);
            //比例计算,一般是图片比较大的情况下进行压缩
            int max = Math.max(a, b);
            if(max > 1){
                options.inSampleSize = max;
            }
            //解析到内存中去
            options.inJustDecodeBounds = false;
//            根据uri重新获取流，inputstream在解析中发生改变了
            Stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(Stream, null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                if(Stream != null){
                    Stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return  null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        text.setText("加载中,请等待······");
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
        try {
            Bitmap uriBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uris[0]));
//                countNumber(uriBitmap);
            dataBitmap = uriBitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//            Bitmap showBitmap = ImageSizeCompress(uris[0]);
//            return showBitmap;
        return dataBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
//            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//            int widthPixels = displayMetrics.widthPixels;
//            int width = bitmap.getWidth();
//            float w = widthPixels / width;
//            Matrix matrix = new Matrix();
//            matrix.postScale(w,w); //长和宽放大缩小的比例
//            Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//            imageView.setImageBitmap(resizeBmp);
        image.setImageBitmap(bitmap);
//        textView.setText("");
//        cropButton.setVisibility(View.VISIBLE);
//        countButton.setVisibility(View.VISIBLE);
    }
}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //相册
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                dataURI = selectedImage;
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImage != null) {
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        mBitmap = BitmapFactory.decodeFile(picturePath);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90.0f);
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        image.setImageBitmap(mBitmap);
                        cursor.close();
                    }
                }
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                Thread thread = new Thread(ReadActivity.this);
                thread.start();
            }
        }
        //相机
        if (requestCode == 0) {
//            mBitmap = (Bitmap) data.getExtras().get("data");
//            Matrix matrix = new Matrix();
//            matrix.postRotate(90.0f);
//            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
//            image.setImageBitmap(mBitmap);
            dataURI = ImageUri;
//            mTask = new MyTask();
//            mTask.execute(ImageUri);
            galleryAddPic();
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(90.0f);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            image.setImageBitmap(mBitmap);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            Thread thread = new Thread(ReadActivity.this);
            thread.start();
        }
        //裁剪
        if (requestCode == UCrop.REQUEST_CROP) {
            Uri croppedFileUri = UCrop.getOutput(data);
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedFileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            mTask = new MyTask();
            Matrix matrix = new Matrix();
            matrix.postRotate(90.0f);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            image.setImageBitmap(mBitmap);
//            mTask.execute(croppedFileUri);
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "裁切图片失败", Toast.LENGTH_SHORT).show();
        }
    }

}

