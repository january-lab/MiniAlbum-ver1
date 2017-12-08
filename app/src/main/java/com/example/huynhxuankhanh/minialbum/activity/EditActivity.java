package com.example.huynhxuankhanh.minialbum.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.huynhxuankhanh.minialbum.R;
import com.example.huynhxuankhanh.minialbum.gallery.InfoImage;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * Created by HUYNHXUANKHANH on 12/1/2017.
 */

public class EditActivity extends AppCompatActivity {
    private InfoImage receive;
    private Button btnCrop, btnEffect, btnFaceDetect, btnBright, btnContrast;
    private Boolean isFav;
    private Bitmap bm;
    private ImageView imageView;
    private Uri lastBmUri = null;
    private boolean isEdit = false;
    private MediaScannerConnection msConn;


    Mat source, dest;

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    source = new Mat();
                    dest = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mOpenCVCallBack);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


        initInterface();

        receive = getIntent().getParcelableExtra("image-info-edit");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_image);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        //create back button on top-left of toolbar
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (receive == null) {
            receive = getIntent().getParcelableExtra("image-info-fav-edit");
            isFav = true;
        }
        if (receive != null) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            bm = BitmapFactory.decodeFile(receive.getPathFile(), options);

            Matrix matrix = new Matrix();
            int currentOrientation=0;
            if(receive.getOrientaion()==null)
                currentOrientation = 0;
            else
                currentOrientation = Integer.parseInt(receive.getOrientaion());
            matrix.postRotate(currentOrientation);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            imageView.setImageBitmap(bm);


            btnCrop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PopupMenu popupMenu = new PopupMenu(EditActivity.this, btnCrop);
                    popupMenu.getMenuInflater().inflate(R.menu.menupop_crop, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (lastBmUri == null)
                                lastBmUri = Uri.fromFile(new File(receive.getPathFile()));
                            switch (menuItem.getItemId()) {
                                case R.id.mncrop_23: {
                                    startActivityCropper(2, 3);
                                    break;
                                }
                                case R.id.mncrop_34: {
                                    startActivityCropper(3, 4);
                                    break;
                                }
                                case R.id.mncrop_56: {
                                    startActivityCropper(5, 6);
                                    break;
                                }
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
            btnEffect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(EditActivity.this, btnEffect);
                    popupMenu.getMenuInflater().inflate(R.menu.menupop_effect, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.mneffect_bw: {
                                    //>>>>>TESTING OPENCV<<<<<
                                    Utils.bitmapToMat(bm, source);
                                    Imgproc.cvtColor(source, dest, Imgproc.COLOR_RGB2GRAY);
                                    bm = Bitmap.createBitmap(dest.width(), dest.height(), Bitmap.Config
                                            .ARGB_8888);
                                    Utils.matToBitmap(dest, bm);
                                    imageView.setImageBitmap(bm);

                                    isEdit = true;
                                }
                                    break;
                                case R.id.mneffect_doc:{
                                    Mat detected_Edge = new Mat();
                                    // tao mat tu bitmap
                                    Utils.bitmapToMat(bm,source);

                                    // convert to gray
                                    Imgproc.cvtColor(source,source,Imgproc.COLOR_RGB2GRAY);

                                    Imgproc.GaussianBlur(source, detected_Edge, new org.opencv.core.Size(3, 3), 0);

                                    Imgproc.adaptiveThreshold(detected_Edge, detected_Edge, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
                                    Utils.matToBitmap(  detected_Edge,bm);
                                    imageView.setImageBitmap(bm);

                                    isEdit = true;
                                }
                                    break;
                                case R.id.mneffect_edPre: {

                                    Utils.bitmapToMat(bm, source);
                                    Imgproc.cvtColor(source,source,Imgproc.COLOR_BGRA2BGR);
                                    Photo.edgePreservingFilter(source,source,1 , 50,0.4f);

                                    Utils.matToBitmap(source, bm);

                                    imageView.setImageBitmap(bm);
                                    isEdit = true;
                                    isEdit = true;
                                }
                                    break;
                                case R.id.mneffect_pencil: {
                                    Utils.bitmapToMat(bm, source);
                                    Imgproc.cvtColor(source, source, Imgproc.COLOR_BGRA2BGR);
                                    Photo.pencilSketch(source, source, source, 10, 0.08f, 0.05f);

                                    Utils.matToBitmap(source, bm);

                                    imageView.setImageBitmap(bm);
                                    isEdit = true;
                                }
                                    break;
                                case R.id.mneffect_stylization: {
                                    Utils.bitmapToMat(bm, source);
                                    Imgproc.cvtColor(source, source, Imgproc.COLOR_BGRA2BGR);
                                    Photo.stylization(source, source, 200, 0.80f);
                                    Utils.matToBitmap(source, bm);

                                    imageView.setImageBitmap(bm);
                                    isEdit = true;
                                }
                                    break;
                                case R.id.mneffect_detail: {
                                    Utils.bitmapToMat(bm, source);
                                    Imgproc.cvtColor(source, source, Imgproc.COLOR_BGRA2BGR);
                                    Photo.detailEnhance(source, source, 10, 0.15f);
                                    Utils.matToBitmap(source, bm);

                                    imageView.setImageBitmap(bm);
                                    isEdit = true;
                                }
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
            btnFaceDetect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            btnBright.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    
                }
            });
            btnContrast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) { // tức là đã có crop ảnh mới có ảnh trả về
                if (resultCode == RESULT_OK) {
                    // vì kết quả cropper image trả về là một uri
                    Uri resultUri = result.getUri();
                    lastBmUri = resultUri;
                    try {
                        bm = MediaStore.Images.Media.getBitmap(EditActivity.this.getContentResolver(), resultUri);
                        isEdit = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bm != null)
                        imageView.setImageBitmap(bm);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
    }

    public void initInterface() {
        btnCrop = (Button) findViewById(R.id.btn_crop);
        btnEffect = (Button) findViewById(R.id.btn_effect);
        btnFaceDetect = (Button) findViewById(R.id.btn_facedetect);
        btnBright = (Button) findViewById(R.id.btn_bright);
        btnContrast = (Button) findViewById(R.id.btn_contrast);
        imageView = (ImageView) findViewById(R.id.img_view_edit);
    }

    @Override
    public void onBackPressed() {
        if (isEdit) {
            // khởi tạo một dialog khi mà quá trình chỉnh sửa ảnh được diễn ra.
            android.app.AlertDialog.Builder alertDialogConfirm = new android.app.AlertDialog.Builder(EditActivity.this);
            alertDialogConfirm.setTitle("Do you want to save before quitting?").setPositiveButton("Save and Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // xử lí lưu ảnh vào gallery store của phone
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MiniAlbum";
                    String fileName = "crop-" + receive.getNameFile();
                    File file = new File(path, fileName);
                    int distinct = 1;
                    // kiểm tra tên trùng: nếu trùng thì đặt tên khác
                    while (file.exists()) {
                        fileName = "crop_" + Integer.toString(distinct) + "-" + receive.getNameFile();
                        file = new File(path, fileName);
                        distinct++;
                    }

                    // get current ID
                    Cursor cursor = EditActivity.this
                            .getContentResolver()
                            .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    new String[]{MediaStore.Images.ImageColumns._ID}, null, null, null);
                    if (cursor != null)
                        cursor.moveToLast();
                    int newId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID)));

                    // đây là một tuple của Gallery Database
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media._ID, newId + 1);
                    receive.setiD(newId + 1);

                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    Date tempdate = new Date();
                    tempdate.setTime(System.currentTimeMillis());
                    receive.setDateTaken(tempdate.toString());

                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.DATA, file.toString());
                    receive.setPathFile(file.toString());

                    values.put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "MiniAlbum");
                    receive.setNameBucket("MiniAlbum");

                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    receive.setNameFile(fileName);

                    long sizeBm = BitmapCompat.getAllocationByteCount(bm);
                    values.put(MediaStore.Images.Media.SIZE, sizeBm);
                    receive.setSize(sizeBm);

                    // insert 1 tuple vào bảng Gallery Image, chỉ có thông tin tuple ko có hình ảnh
                    EditActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    // Xuất file ảnh ra folder MiniAlbum
                    FileOutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);

                        bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                        fOut.flush(); // Not really required
                        fOut.close(); // do not forget to close the stream
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    scanPhoto(file.toString());

                    Intent resultCrop = new Intent();
                    resultCrop.putExtra("crop-image", receive);

                    setResult(222, resultCrop);
                    EditActivity.super.onBackPressed();

                }
            }).setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // do nothing, just quit
                    EditActivity.super.onBackPressed();
                }
            }).show();
        } else
            EditActivity.super.onBackPressed();

    }

    public void startActivityCropper(int ratioX, int ratioY) {
        Intent intent =
                CropImage
                        .activity(lastBmUri)
                        .setAspectRatio(ratioX, ratioY)
                        .getIntent(EditActivity.this);
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(EditActivity.this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
                Toast.makeText(EditActivity.this, "Scan completely !!!", Toast.LENGTH_SHORT).show();
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
            }
        });
        msConn.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mOpenCVCallBack);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
