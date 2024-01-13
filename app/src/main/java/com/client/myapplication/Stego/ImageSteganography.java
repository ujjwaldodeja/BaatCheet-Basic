package com.client.myapplication.Stego;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class ImageSteganography {

    // Hide message in image using LSB substitution
    public static byte[] hideMessage(byte[] originalImageBytes, String message) {
        Bitmap originalImage = null;
        try {
            originalImage = BitmapFactory.decodeByteArray(originalImageBytes, 0, originalImageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap encodedImage = encodeMessage(originalImage, message);

        ByteBuffer buffer = ByteBuffer.allocate(encodedImage.getByteCount());
        encodedImage.copyPixelsToBuffer(buffer);
        return buffer.array();
    }

    // Encode message in LSB of an image
    private static Bitmap encodeMessage(Bitmap coverImage, String message) {
        String binaryMessage = stringToBinary(message);
        int messageIndex = 0;

        Bitmap encodedImage = Bitmap.createBitmap(coverImage.getWidth(), coverImage.getHeight(), coverImage.getConfig());

        for (int i = 0; i < coverImage.getWidth(); i++) {
            for (int j = 0; j < coverImage.getHeight(); j++) {
                int pixel = coverImage.getPixel(i, j);

                int alpha = Color.alpha(pixel);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                // Modify the least significant bit of each color channel
                red = (red & 0xFE) | (binaryMessage.charAt(messageIndex++) - '0');
                green = (green & 0xFE) | (binaryMessage.charAt(messageIndex++) - '0');
                blue = (blue & 0xFE) | (binaryMessage.charAt(messageIndex++) - '0');

                int modifiedPixel = Color.argb(alpha, red, green, blue);
                encodedImage.setPixel(i, j, modifiedPixel);

                if (messageIndex >= binaryMessage.length()) {
                    break;
                }
            }
            if (messageIndex >= binaryMessage.length()) {
                break;
            }
        }

        return encodedImage;
    }

    // Convert string to binary
    private static String stringToBinary(String input) {
        StringBuilder binaryMessage = new StringBuilder();
        for (char character : input.toCharArray()) {
            binaryMessage.append(String.format("%8s", Integer.toBinaryString(character)).replace(' ', '0'));
        }
        return binaryMessage.toString();
    }
    // Extract hidden message from image bytes
    public static String extractMessage(byte[] encodedImageBytes) {
        Bitmap encodedImage = BitmapFactory.decodeByteArray(encodedImageBytes, 0, encodedImageBytes.length);

        System.out.println(encodedImage);

        return decodeMessage(encodedImage);
    }

    // Decode hidden message from LSB of an image
    private static String decodeMessage(Bitmap encodedImage) {
        if (encodedImage != null) {
            StringBuilder binaryMessage = new StringBuilder();

            for (int i = 0; i < encodedImage.getWidth(); i++) {
                for (int j = 0; j < encodedImage.getHeight(); j++) {
                    int pixel = encodedImage.getPixel(i, j);

                    int red = Color.red(pixel) & 1;
                    int green = Color.green(pixel) & 1;
                    int blue = Color.blue(pixel) & 1;

                    binaryMessage.append(red);
                    binaryMessage.append(green);
                    binaryMessage.append(blue);
                }
            }
            return binaryToMessage(binaryMessage.toString());
        }
        System.out.println("BITMAP IS NOT GENERATED PROPERLY");
        return null;
    }

    // Convert binary to string message
    private static String binaryToMessage(String binary) {
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < binary.length(); i += 8) {
            String byteStr = binary.substring(i, i + 8);
            int charCode = Integer.parseInt(byteStr, 2);
            message.append((char) charCode);
        }

        return message.toString();
    }
//    public static void main(String[] args) {
//        byte[] imageBytes = ImageUtils.getImageBytes("/Users/ftw/Desktop/images/posture.png");
//        System.out.print(imageBytes);
//        byte[] encodedImage = hideMessage(imageBytes, "Hello");
//        System.out.print(encodedImage);
//        System.out.print(extractMessage(encodedImage));
//    }
}
