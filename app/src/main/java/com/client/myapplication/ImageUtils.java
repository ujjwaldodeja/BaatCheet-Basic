package com.client.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    public static byte[] getImageBytes(Context context, String imageName) {
        InputStream inputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            System.out.println("CHECK 1");
            inputStream = context.getAssets().open(imageName);
            System.out.println("CHECK 2");
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            // Read bytes from the InputStream into a ByteArrayOutputStream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Convert the ByteArrayOutputStream to a byte array
        return byteArrayOutputStream.toByteArray();

//        File imageFile = new File(imageName);
//        byte[] imageData = new byte[0];
//        try {
//            imageData = Files.readAllBytes(imageFile.toPath());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return imageData;
    }


    public static Bitmap reformImage(byte[] imageBytes) {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
