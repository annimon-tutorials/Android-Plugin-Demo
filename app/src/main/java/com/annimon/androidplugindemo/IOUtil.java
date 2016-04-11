package com.annimon.androidplugindemo;

import android.content.Context;
import android.support.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

    @Nullable
    public static String extractAssetToInternalStorage(Context context, String fileName) {
        try {
            final InputStream is = context.getAssets().open(fileName);
            final File file = new File(context.getFilesDir(), fileName);
            final OutputStream os = new FileOutputStream(file);
            final int length = 1024;
            final byte[] buffer = new byte[length];
            int readed;
            while ((readed = is.read(buffer)) != -1) {
                os.write(buffer, 0, readed);
            }
            os.flush();
            os.close();
            is.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }
}
