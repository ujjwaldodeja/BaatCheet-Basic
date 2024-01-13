package com.client.myapplication.Stego;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
    }

    public static Bitmap reformImage(byte[] imageBytes) {
        System.out.println(Arrays.toString(imageBytes));
        System.out.println(imageBytes.length);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public static Uri writeImage(Context context, byte[] imageBytes) {
        try {
            // Save the image to internal storage
            File file = new File(context.getFilesDir(), "received.jpeg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(imageBytes);
            System.out.println("RCHECK");

            fileOutputStream.flush();
            fileOutputStream.close();
            return Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
//        String PATH = "/Users/ftw/Desktop/BaatCheet/Chat Application/Client/app/src/main/assets/";
//        byte[] imageBytes = getBytes(PATH + "posture.png");
//        System.out.println(imageBytes);
//        Bitmap imageMap = reformImage(imageBytes);
//        System.out.println(imageMap);
//
//        try {
//            FileOutputStream file = new FileOutputStream(new File(PATH + "test.png"));
//            file.write(imageBytes);
//            file.flush();
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

}

//        File imageFile = new File(imageName);
//        byte[] imageData = new byte[0];
//        try {
//            imageData = Files.readAllBytes(imageFile.toPath());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return imageData;