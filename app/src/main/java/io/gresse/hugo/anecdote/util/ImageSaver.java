package io.gresse.hugo.anecdote.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Save images to the shared Pictures collection so they show up in the user gallery.
 * <p>
 * Created by Hugo Gresse on 18/03/2017.
 */
public final class ImageSaver {

    private static final String TAG       = ImageSaver.class.getSimpleName();
    private static final String MIME_TYPE = "image/png";

    private ImageSaver() {
    }

    /**
     * @return a content Uri readable by other apps, or null if the image could not be saved
     */
    @Nullable
    public static Uri saveToGallery(Context context, Drawable drawable, String directoryName, String fileName) {
        Bitmap bitmap = Utils.drawableToBitmap(drawable);
        if (bitmap == null) {
            return null;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return saveWithMediaStore(context.getContentResolver(), bitmap, directoryName, fileName);
            }
            return saveToPublicDirectory(context, bitmap, directoryName, fileName);
        } catch (IOException e) {
            Log.w(TAG, "Fail to save image " + fileName, e);
            return null;
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @NonNull
    private static Uri saveWithMediaStore(ContentResolver resolver,
                                          Bitmap bitmap,
                                          String directoryName,
                                          String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + directoryName);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
        if (uri == null) {
            throw new IOException("MediaStore refused to create " + fileName);
        }

        try (OutputStream outputStream = resolver.openOutputStream(uri)) {
            if (outputStream == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new IOException("Unable to write " + fileName);
            }
        } catch (IOException e) {
            resolver.delete(uri, null, null);
            throw e;
        }

        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
        return uri;
    }

    @NonNull
    private static Uri saveToPublicDirectory(Context context,
                                             Bitmap bitmap,
                                             String directoryName,
                                             String fileName) throws IOException {
        File directory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                directoryName);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create " + directory.getAbsolutePath());
        }

        File file = new File(directory, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        }

        ContentValues values = new ContentValues();
        //noinspection deprecation
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }
}
