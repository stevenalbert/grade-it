import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import model.AnswerMat;
import model.AnswerSheetMetadata;
import process.AnswerSheetScorer;

public class Main {

    public static final String RES_DIR = "res";
    private static AnswerSheetMetadata metadata = new AnswerSheetMetadata(new File(RES_DIR, "P-40.asmf"));

    public static void main(String[] args) {
        // load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // read directory
        File resDirectory = new File(RES_DIR);
        File ansSheetDirectory = new File(resDirectory, "AnsSheet");
        Calendar calendar = Calendar.getInstance();
        StringBuilder folderName = new StringBuilder("Processed_");
        folderName.append(String.valueOf(calendar.get(Calendar.MONTH) + 1) + "-");
        folderName.append(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "-");
        folderName.append(String.valueOf(calendar.get(Calendar.YEAR)));
        File processedDirectory = new File(resDirectory, folderName.toString());
        processedDirectory.mkdirs();
        processDirectory(ansSheetDirectory, processedDirectory);

    }

    private static void processDirectory(File directory, File outputDirectory) {
        long startTime, endTime;
        if (!directory.isDirectory())
            return;
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                /*
                 * File newOutputDirectory = new File(outputDirectory, files[i].getName());
                 * newOutputDirectory.mkdirs(); processDirectory(files[i], newOutputDirectory);
                 */
                continue;
            }
            // read answer sheet photo image file
            Mat mat = Imgcodecs.imread(files[i].getAbsolutePath());

            // process the image
            System.out.println("Start find paper => " + files[i].getName());
            startTime = System.nanoTime();
            File output = new File(outputDirectory, files[i].getName().replace(".jpg", ""));
            output.mkdirs();
            File preprocessOutput = new File(output, "0-Preprocess");
            preprocessOutput.mkdirs();
            Mat process = AnswerSheetScorer.convertAnswerSheet(mat, metadata, preprocessOutput, files[i].getName(),
                    true);
            endTime = System.nanoTime();
            System.out.println("Converting image in " + ((double) (endTime - startTime)) / (double) 1e9);

            File p = new File(output, "1-Retrieve squares content");
            p.mkdirs();
            System.out.println("Process answer sheet");
            startTime = System.nanoTime();
            ArrayList<AnswerMat> answerMats = AnswerSheetScorer.processAnswerSheet(process, metadata, p,
                    files[i].getName(), true);
            endTime = System.nanoTime();
            System.out.println("Processing answer sheet in " + ((double) (endTime - startTime)) / (double) 1e9);

            startTime = System.nanoTime();
            File scoringDirectory = new File(output, "2-Normalization and Detection");
            scoringDirectory.mkdirs();
            AnswerSheetScorer.scoreAnswerSheet(answerMats, scoringDirectory, "../result.txt", true);
            endTime = System.nanoTime();
            System.out.println("Scoring answer sheet in " + ((double) (endTime - startTime)) / (double) 1e9);

            System.out.println("File " + files[i].getName() + " is processed.");
        }
    }

}
