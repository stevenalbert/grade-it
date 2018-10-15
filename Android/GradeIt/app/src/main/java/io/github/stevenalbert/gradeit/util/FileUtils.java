package io.github.stevenalbert.gradeit.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Steven Albert on 7/12/2018.
 */
public class FileUtils {

    public static File copyFromAsset(Context context, String assetFilename, File outputFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        if(outputFile.exists()) return outputFile;

        try {
            inputStream = context.getAssets().open(assetFilename);
            outputStream = new FileOutputStream(outputFile);

            // Copy to outputStream using byte
            byte[] bytes = new byte[1024];
            int len;
            while((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return outputFile;
    }
}
