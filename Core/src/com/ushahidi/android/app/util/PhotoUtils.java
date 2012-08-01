/** 
 ** Copyright (c) 2011 Ushahidi Inc
 ** All rights reserved
 ** Contact: team@ushahidi.com
 ** Website: http://www.ushahidi.com
 ** 
 ** GNU Lesser General Public License Usage
 ** This file may be used under the terms of the GNU Lesser
 ** General Public License version 3 as published by the Free Software
 ** Foundation and appearing in the file LICENSE.LGPL included in the
 ** packaging of this file. Please review the following information to
 ** ensure the GNU Lesser General Public License version 3 requirements
 ** will be met: http://www.gnu.org/licenses/lgpl.html.	
 **	
 **
 ** If you have questions regarding the use of this file, please contact
 ** Ushahidi developers at team@ushahidi.com.
 ** 
 **/

package com.ushahidi.android.app.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import com.ushahidi.android.app.util.PhotoUtils;

import com.ushahidi.android.app.util.PhotoUtils;

import com.ushahidi.android.app.ImageManager;
import com.ushahidi.android.app.Preferences;

import com.ushahidi.android.app.util.PhotoUtils;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;

public class PhotoUtils {

    private static final String CLASS_TAG = PhotoUtils.class.getCanonicalName();

    public static int getScreenOrientation(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        if (display.getWidth() == display.getHeight()) {
            return Configuration.ORIENTATION_SQUARE;
        }
        else {
            if (display.getWidth() < display.getHeight()) {
                return Configuration.ORIENTATION_PORTRAIT;
            }
            else {
                return Configuration.ORIENTATION_LANDSCAPE;
            }
        }
    }

    public static Uri getPhotoUri(String filename, Activity activity) {
        File path = new File(Environment.getExternalStorageDirectory(), activity.getPackageName());
        if (!path.exists() && path.mkdir()) {
            return Uri.fromFile(new File(path, filename));
        }
        return Uri.fromFile(new File(path, filename));
    }

    public static String getPhotoPath(Activity activity) {
        Log.d(CLASS_TAG, "getPhotoPath");
        File path = new File(Environment.getExternalStorageDirectory(), activity.getPackageName());
        return path.exists() ? path.getAbsolutePath() : null;

    }

    public static boolean imageExist(String filename, Activity activity) {
        Log.d(CLASS_TAG, "imageExist(): " + filename);
        File path = new File(filename);
        if (!path.exists()) {
            Log.d(CLASS_TAG, "image does not exist");
            return false;
        }
        Log.d(CLASS_TAG, "image does exist");
        return true;
    }

    public static Bitmap getGalleryPhoto(Activity activity, Uri uri) {
        if (uri != null) {
            String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
            Cursor cursor = activity.getContentResolver().query(uri, columns, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor.getColumnIndex(columns[0]));
                int orientation = cursor.getInt(cursor.getColumnIndex(columns[1]));
                Bitmap original = BitmapFactory.decodeFile(filePath);
                if (original != null) {
                    Bitmap scaled = scaleBitmap(original);
                    original.recycle();
                    if (orientation == 0 && scaled.getWidth() < scaled.getHeight()) {
                        Log.i("XXX", String.format("FILE:%s ORIENTATION: LANDSCAPE", filePath));
                        Bitmap rotated = rotatePhoto(scaled, -90);
                        scaled.recycle();
                        return rotated;
                    }
                    else if (orientation == 90 && scaled.getWidth() > scaled.getHeight()) {
                        Log.i("XXX", String.format("FILE:%s ORIENTATION: PORTRAIT", filePath));
                        Bitmap rotated = rotatePhoto(scaled, 90);
                        scaled.recycle();
                        return rotated;
                    }
                    else {
                        Log.i("XXX", String.format("FILE:%s ORIENTATION: %d", filePath, orientation));
                    }
                    return scaled;
                }
            }
        }
        return null;
    }

    public static Bitmap getCameraPhoto(Activity activity, Uri uri) {
        if (uri != null) {
            Bitmap original = BitmapFactory.decodeFile(uri.getPath());
            if (original != null) {
                Log.i("XXX", String.format("ORIGINAL %dx%d", original.getWidth(), original.getHeight()));
                Bitmap scaled = scaleBitmap(original);
                if (scaled != null) {
                    Log.i("XXX", String.format("SCALED %dx%d", scaled.getWidth(), scaled.getHeight()));
                    original.recycle();
                    if (getScreenOrientation(activity) == Configuration.ORIENTATION_PORTRAIT &&
                        scaled.getWidth() > scaled.getHeight()) {
                        Bitmap rotated = rotatePhoto(scaled, 90);
                        scaled.recycle();
                        return rotated;
                    }
                    return scaled;
                }
                return original;
            }
        }
        return null;
    }

    public static boolean savePhoto(Activity activity, Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArray);
            bitmap.recycle();
            ImageManager.writeImage(byteArray.toByteArray(), "photo.jpg", getPhotoPath(activity));
            byteArray.flush();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Bitmap rotatePhoto(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap scaleBitmap(Bitmap original) {
        if (original != null) {
            float ratio = (float)original.getHeight() / (float)original.getWidth();
            int width = Preferences.photoWidth > 0 ? Preferences.photoWidth : 500;
            Log.i(CLASS_TAG, "Scaling image to " + width + " x " + ratio);
            Bitmap scaled = Bitmap.createScaledBitmap(original, width, (int)(width * ratio), true);
            original.recycle();
            return scaled;
        }
        return null;
    }

}
