package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Application;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.DhcpInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.Python;



public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "测试用";
    private ImageView shotview ;
    private File currentImageFile = null;
    private String mFilePath;
    private Uri currentPath=null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shotview = findViewById(R.id.imageView);

        //调用系统摄像机拍照功能
        Button takePhoto = findViewById(R.id.getcamera);
        takePhoto.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                File dir = new File(Environment.getExternalStorageDirectory(),"pictures");
                if(dir.exists())
                {
                    dir.mkdirs();
                }

                //命名临时图片的文件名
                currentImageFile = new File(dir,System.currentTimeMillis() + ".jpg");
                if(!currentImageFile.exists())
                {
                    try
                    {
                        currentImageFile.createNewFile();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//通过intent调用照相机照相
                //Uri uri = Uri.fromFile(currentImageFile);
                Uri uri;
                mFilePath = Environment.getExternalStorageDirectory().getPath()+"/FCa/"
                        + System.currentTimeMillis() + ".jpg";
                File file = new File(mFilePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                String path = file.getPath();
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, path);
                uri = getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                //Log.v(TAG,uri.toString());
                currentPath = uri;
                it.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(it, Activity.DEFAULT_KEYS_DIALER);

            }
        });

        //相册选图功能
        Button chooseFromAlbum = (Button) findViewById(R.id.album);

        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent();
                it.setAction(Intent.ACTION_PICK);
                it.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(it, Activity.BIND_DEBUG_UNBIND);
                //Log.v(TAG,currentPath.toString());
            }
        });

//调用python程序估计距离并回显结果
        Button showResult = findViewById(R.id.showResult);
        showResult.setEnabled(false);
        showResult.setOnClickListener(new View.OnClickListener()
        {
            public Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
                int src_w = bitmap.getWidth();
                int src_h = bitmap.getHeight();
                float scale_w = ((float) dst_w) / src_w;
                float scale_h = ((float) dst_h) / src_h;
                Matrix matrix = new Matrix();
                matrix.postScale(scale_w, scale_h);
                Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true);
                return dstbmp;

            }
            public void onClick (View v)
            {
                boolean isOk = false;//判断是不是符合防疫要求
//                double min = 20.0;//记录最小的排队距离
                double limit = 1.0;//用于比较
                //此处调用Python编写的程序回传照片中行人最小的间距保存到min
                //使用的照片是shotView中的图片（shotview为ImageView组件用于在界面上显                                                                                                                                                        示图片）
                //可以使用Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap()等方法获取图片内容

                Bitmap bitmap = imageScale(((BitmapDrawable)shotview.getDrawable()).getBitmap(),500,500);

                int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];//保存所有的像素的数组，图片宽×高
                bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
                int[] p = new int[bitmap.getHeight()*bitmap.getWidth()*3];
                for(int i = 0; i < pixels.length; i++){
                    int clr = pixels[i];
                    int red  = (clr & 0x00ff0000) >> 16; //取高两位
                    int green = (clr & 0x0000ff00) >> 8; //取中两位
                    int blue = clr & 0x000000ff; //取低两位
                    p[i*3+0] = red;
                    p[i*3+1] = green;
                    p[i*3+2] = blue;
//                    System.out.println("r="+red+",g="+green+",b="+blue);
                }
                int[] myList = {1, 2, 3, 3};

                //Python
                //

                initPython();
                Python py = Python.getInstance();
                PyObject obj1 = py.getModule("test");
                PyObject obj2 = py.getModule("test").callAttr("test2",p);
                double distance = obj2.toJava(double.class);
//                double min = distance/100;
                BigDecimal minn = new BigDecimal(distance/100);
                double min = minn.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();

                Log.d(TAG,obj1.callAttr("test2",p).toString());


                //
                //
                //总之最后需要搞一个min回来
                //如果图片上人数>2就返回那个最小的距离保存在min中
                //后面就是和limit比大小输出结论了

                if(min > limit){
                    isOk = true;
                }
                TextView infoText = findViewById(R.id.info);
                TextView resultText = findViewById(R.id.result);
                resultText.setVisibility(View.VISIBLE);
                infoText.setText("距离前方人群的最小距离约为："+min+"米");
                if (isOk == false){
                    resultText.setText("不符合距离要求");
                    resultText.setTextColor(android.graphics.Color.RED);
                }
                else{
                    resultText.setText("遵守排队间距");
                    resultText.setTextColor(Color.GREEN);
                }
                Button showResult = findViewById(R.id.showResult);
                showResult.setEnabled(false);
            }
        });
    }
    //重写onActivityResult(int requestCode, int resultCode, Intent data)方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER) {
            Button showResult = findViewById(R.id.showResult);
            showResult.setEnabled(true);
            TextView infoText = findViewById(R.id.info);
            infoText.setText("请点击测量距离按钮检测间距");
            //Log.v(TAG, Uri.fromFile(currentImageFile).toString());
            shotview.setImageURI(currentPath);
            TextView resultText = findViewById(R.id.result);
            resultText.setVisibility(View.INVISIBLE);
            //shotview.setImageURI(FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", currentImageFile));
        }
        else if(requestCode == Activity.BIND_DEBUG_UNBIND){
            Button showResult = findViewById(R.id.showResult);
            showResult.setEnabled(true);
            TextView infoText = findViewById(R.id.info);
            infoText.setText("请点击测量距离按钮检测间距");
            //Log.v(TAG, Uri.fromFile(currentImageFile).toString());
            currentPath = data.getData();
            shotview.setImageURI(currentPath);
            TextView resultText = findViewById(R.id.result);
            resultText.setVisibility(View.INVISIBLE);
            //shotview.setImageURI(FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", currentImageFile));
        }
    }

    void initPython(){
        if(!Python.isStarted())
        {
            Python.start(new AndroidPlatform(this));
        }
    }
}



