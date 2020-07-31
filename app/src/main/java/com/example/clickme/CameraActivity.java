package com.example.clickme;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {


    ImageView filters,torch,flash,popup,switch_camera,image_capture;
    HorizontalScrollView scrollView;
    int count=0;
    preview preview;
    int REQUEST_CAMERA_CODE=0;
    int WRITE_TO_EXTERNAL_STORAGE=1;
//    TextureView textureView;
    PreviewView previewView;
    ImageCapture imageCapture;
    UI_handling ui_handling;
    ProcessCameraProvider cameraProvider;
    ExecutorService cameraExecutor;
    CameraInfo camerainfo;
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

        scrollView=findViewById(R.id.filters_scroll_view);
        previewView=findViewById(R.id.camera_preview);
        scrollView.setVisibility(View.GONE);
        ui_handling=new UI_handling(CameraActivity.this);
        checkpermissions();
        OpenCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filters:
                if(count==0) {
                    scrollView.setVisibility(View.VISIBLE);
                    count=1;
                }else{
                    scrollView.setVisibility(View.GONE);
                    count=0;
                }
                break;

            case R.id.pop_up:
//                new PopUP(CameraActivity.this);
                break;

            case R.id.flash:
//                ui_handling=new UI_handling(CameraActivity.this);
                ui_handling.clickFlash();
                break;

            case R.id.torch:
                break;

            case R.id.switch_camera:ui_handling.switchCamera();
                break;

            case R.id.image_capture:
                clickimageCapture();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
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
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview() {

        ProcessCameraProvider provider = cameraProvider;
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder()
                .build();

        imageCapture= imageCapture();

        CameraSelector cameraSelector=cameraselector();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview,imageCapture);

        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));
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

    public File CreateImageDirectory(){
        File storage = Environment.getExternalStorageDirectory();
        File dir = new File(storage.getAbsolutePath() + "/Camera");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = String.format("%d.jpeg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        Toast.makeText(CameraActivity.this,"filename "+ fileName,Toast.LENGTH_SHORT).show();
        return outFile;
    }

    public void clickimageCapture() {
        File file=CreateImageDirectory();
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file)
//                .setMetadata(metadata)
                .build();
        imageCapture.takePicture(outputOptions,cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(CameraActivity.this, "Image Saved ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}