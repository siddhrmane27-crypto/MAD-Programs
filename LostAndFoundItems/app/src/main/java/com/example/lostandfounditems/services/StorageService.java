package com.example.lostandfounditems.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Converts images to compressed Base64 strings for storage in Firestore.
 * No Firebase Storage plan required.
 */
public class StorageService {

    public interface UploadCallback {
        void onSuccess(String base64Image);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Compresses the image and converts it to a Base64 string.
     * Runs on the calling thread — call from a background thread if needed,
     * but for typical gallery images the compression is fast enough on main thread.
     */
    public void uploadImage(InputStream inputStream, UploadCallback callback) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                callback.onFailure("Could not decode image");
                return;
            }

            // Scale down to max 600px wide to keep Firestore document size small
            bitmap = scaleBitmap(bitmap, 600);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] data = baos.toByteArray();

            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            callback.onSuccess(base64);
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth) {
        if (bitmap.getWidth() <= maxWidth) return bitmap;
        float ratio = (float) maxWidth / bitmap.getWidth();
        int newHeight = Math.round(bitmap.getHeight() * ratio);
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
    }

    /** Decode a Base64 string back to a Bitmap for display. */
    public static Bitmap decodeBase64(String base64) {
        try {
            byte[] data = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }

    /** No-op for Base64 — nothing to delete remotely. */
    public void deleteImage(String base64Image, SimpleCallback callback) {
        callback.onSuccess();
    }
}
