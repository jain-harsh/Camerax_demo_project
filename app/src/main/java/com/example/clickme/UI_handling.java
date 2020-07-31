package com.example.clickme;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class UI_handling {
    CameraActivity cameraActivity;
    public int flashmode= ImageCapture.FLASH_MODE_OFF;
    public int camera_face= CameraSelector.LENS_FACING_BACK;

    public UI_handling(CameraActivity cameraActivity) {
        this.cameraActivity = cameraActivity;
    }

    public void clickFlash(){
        ImageView view= cameraActivity.findViewById(R.id.flash);
        if(flashmode==ImageCapture.FLASH_MODE_OFF){
            flashmode=ImageCapture.FLASH_MODE_ON;
            view.setImageResource(R.drawable.flash_on);
        }else if(flashmode==ImageCapture.FLASH_MODE_ON){
            flashmode=ImageCapture.FLASH_MODE_AUTO;
            view.setImageResource(R.drawable.flash_auto);
        }else{
            flashmode=ImageCapture.FLASH_MODE_OFF;
            view.setImageResource(R.drawable.flash_off);
        }
        cameraActivity.bindPreview();
    }

    public void clickTorch(){

    }

    public void switchCamera(){
        if(camera_face==CameraSelector.LENS_FACING_BACK){
            camera_face=CameraSelector.LENS_FACING_FRONT;
        }else{
            camera_face=CameraSelector.LENS_FACING_BACK;
        }
        cameraActivity.bindPreview();
    }


}
