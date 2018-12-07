package io.github.stevenalbert.gradeit.util;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven Albert on 10/21/2018.
 */
public class MetadataUtils {

    private static final String appDirectoryName = "GradeIt";
    private static final String metadataDirectoryName = "Metadata";
    private static final String metadataExtension = ".asmf";

    public static File getMetadataDirectory() {
        return new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), appDirectoryName), metadataDirectoryName);
    }

    public static List<String> metadataFormatsFilename() {
        String[] filenames = getMetadataDirectory().list();
        ArrayList<String> metadataFormats = new ArrayList<>();
        if(filenames == null) return metadataFormats;
        for(String filename : filenames) {
            if(filename.endsWith(metadataExtension))
                metadataFormats.add(filename.substring(0, filename.length() - metadataExtension.length()));
        }
        return metadataFormats;
    }

    public static File metadataFile(String filename) {
        return new File(getMetadataDirectory(), filename + metadataExtension);
    }
}
