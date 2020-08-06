package com.example.clickme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView filters, torch, flash, popup, switch_camera, image_capture;
    HorizontalScrollView scrollView;
    int count = 0;
    int REQUEST_CAMERA_CODE = 0;
    int WRITE_TO_EXTERNAL_STORAGE = 1;
    //    TextureView textureView;
    RelativeLayout gray;
    PreviewView previewView;
    ScaleGestureDetector scaleGestureDetector;
    TextureView textureView;
    ImageCapture imageCapture;
    ImageAnalysis imageAnalyser;
    UI_handling ui_handling;
    ProcessCameraProvider cameraProvider;
    Executor cameraExecutor;
    CameraInfo camerainfo;
    Camera camera;
    CameraControl cameraControl;
    ScaleGestureDetector.OnScaleGestureListener listener;
    int height,width;
    Bitmap bit;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        findViewById(R.id.filters).setOnClickListener(this);
        findViewById(R.id.torch).setOnClickListener(this);
        findViewById(R.id.pop_up).setOnClickListener(this);
        findViewById(R.id.flash).setOnClickListener(this);
        findViewById(R.id.switch_camera).setOnClickListener(this);
        findViewById(R.id.image_capture).setOnClickListener(this);
        findViewById(R.id.gray).setOnClickListener(this);

        scrollView = findViewById(R.id.filters_scroll_view);
        previewView = findViewById(R.id.camera_preview);
//        textureView=findViewById(R.id.camera_preview);
        scrollView.setVisibility(View.GONE);

        cameraExecutor = Executors.newSingleThreadExecutor();
        ui_handling = new UI_handling(CameraActivity.this);
        checkpermissions();
        setmetrics();
        OpenCamera();
    }

    private void setmetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filters:
                if (count == 0) {
                    scrollView.setVisibility(View.VISIBLE);
                    count = 1;
                } else {
                    scrollView.setVisibility(View.GONE);
                    count = 0;
                }
                break;

            case R.id.pop_up:
//                new PopUP(CameraActivity.this);
                break;

            case R.id.flash:
                ui_handling.clickFlash();
                break;

            case R.id.torch:
                ui_handling.clickTorch(camera);
                break;

            case R.id.switch_camera:
                ui_handling.switchCamera();
                break;

            case R.id.image_capture:
                clickimageCapture();
                break;

            case R.id.gray:
                bit=createBitmap(height,width);
                Bitmap bitmap=toGrayscale(bit);
                previewView.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                Toast.makeText(getApplicationContext(), "gray", Toast.LENGTH_SHORT).show();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private Bitmap createBitmap(int height, int width) {
       Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    public void OpenCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bindPreview();
            setUpPreviewTouchListeners();
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview() {

        ProcessCameraProvider provider = cameraProvider;
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder()
                .build();

        imageCapture = imageCapture();

        CameraSelector cameraSelector = cameraselector();

        imageAnalyser = imageAnalysis();

        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, imageAnalysis());

        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));
        camerainfo=camera.getCameraInfo();
        cameraControl = camera.getCameraControl();
    }

    private CameraSelector cameraselector() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(ui_handling.camera_face)
                .build();
        return cameraSelector;
    }

    private ImageCapture imageCapture() {
        ImageCapture imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
//                .setTargetAspectRatio(screenAspectRatio)
//                .setTargetResolution(screenSize)
//                .setTargetRotation(rotation)
                .setFlashMode(ui_handling.flashmode)
                .build();
        return imageCapture;
    }

    private ImageAnalysis imageAnalysis() {
        ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
//                .setTargetRotation(rotation)
                .build();

//        imageAnalyzer.setAnalyzer(new ImageAnalysis().);
        return imageAnalyzer;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpPreviewTouchListeners() {
        listener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                float scaleFactor = detector.getScaleFactor();
//                zoomLevel.setText(Math.floor(currentZoomRatio * scaleFactor) + "X");
                cameraControl.setZoomRatio(currentZoomRatio * scaleFactor);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
//                zoomLevel.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
//                zoomLevel.setVisibility(View.GONE);
            }
        };
        scaleGestureDetector = new ScaleGestureDetector(CameraActivity.this, listener);
        previewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                //If action is move trigger listener for zoom functionality
                scaleGestureDetector.onTouchEvent(event);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //Else if simple touch then trigger tap to focus functionality
                //For focus animation use action up as well.
                MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(previewView.getWidth(), previewView.getHeight());
                MeteringPoint point = factory.createPoint(event.getX(), event.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        .addPoint(point, FocusMeteringAction.FLAG_AE)
                        .setAutoCancelDuration(5, TimeUnit.SECONDS)
                        .build();
                cameraControl.startFocusAndMetering(action);
                return true;
            }
            return false;
        });
    }

    public void checkpermissions() {
        String[] permissions = {
                WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(CameraActivity.this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, permissions, REQUEST_CAMERA_CODE);
        } else if (ContextCompat.checkSelfPermission(CameraActivity.this, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, permissions, WRITE_TO_EXTERNAL_STORAGE);
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                OpenCamera();
            } else {
                ShowDialog();
            }

        } else if (requestCode == WRITE_TO_EXTERNAL_STORAGE) {
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                OpenCamera();
            } else {
                ShowDialog();
            }
        }
    }

    private void ShowDialog() {
        AlertDialog.Builder Dialog = new AlertDialog.Builder(this);
        Dialog.setMessage("Need Permission TO Perform Task")
                .setTitle("Important Message")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkpermissions();
                    }
                });
    }

    public File CreateImageDirectory() {
        File storage = Environment.getExternalStorageDirectory();
        File dir = new File(storage.getAbsolutePath() + "/tasveer");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = String.format("%d.jpeg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
//        Toast.makeText(CameraActivity.this,"filename "+ fileName,Toast.LENGTH_SHORT).show();
        return outFile;
    }

    public void clickimageCapture() {
        File file = CreateImageDirectory();

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file)
                .build();
        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                System.out.println(file.toString());
                previewView.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("stored at: "+file.toString());
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException e) {
                e.printStackTrace();
            }
        });
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}