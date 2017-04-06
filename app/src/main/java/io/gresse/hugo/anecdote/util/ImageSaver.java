package io.gresse.hugo.anecdote.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Hugo Gresse on 18/03/2017.
 * From http://stackoverflow.com/a/35827955/1377145
 */

public class ImageSaver {

    public static final String TAG = ImageSaver.class.getSimpleName();
    private String mDirectoryName = "images";
    private String mFileName      = "image.png";
    private Context mContext;
    private boolean mExternal;

    public ImageSaver(Context context) {
        this.mContext = context;
    }

    public ImageSaver setFileName(String fileName) {
        this.mFileName = fileName;
        return this;
    }

    public ImageSaver setExternal(boolean external) {
        this.mExternal = external;
        return this;
    }

    public ImageSaver setDirectoryName(String directoryName) {
        this.mDirectoryName = directoryName;
        return this;
    }

    @Nullable
    public File save(Bitmap bitmapImage) {
        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            fileOutputStream = new FileOutputStream(file = createFile());
            Log.d(TAG, "Saving image to " + file.getAbsolutePath());
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            if(mExternal){
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mContext = null;
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @NonNull
    private File createFile() {
        File directory;
        if (mExternal) {
            directory = new File(Environment.getExternalStorageDirectory() + "/" + mDirectoryName);
            if(!directory.exists()) {
                directory.mkdir();
            }
        } else {
            directory = mContext.getDir(mDirectoryName, Context.MODE_PRIVATE);
        }

        return new File(directory, mFileName);
    }

    public Bitmap load() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(createFile());
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
