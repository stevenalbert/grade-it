import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import model.Answer;
import model.AnswerKey;
import model.AnswerMat;
import model.AnswerSheet;
import model.AnswerSheetMetadata;
import model.Option;
import process.AnswerSheetScorer;
import process.FeatureExtractor;

public class Main {

    public static final String RES_DIR = "res";
    private static AnswerKey answerKey;
    
    public static void main(String[] args) {
        // load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // read directory
        File resDirectory = new File(RES_DIR);
        File ansSheetDirectory = new File(resDirectory, "Test-Not-X");
//        Calendar calendar = Calendar.getInstance();
        StringBuilder folderName = new StringBuilder(ansSheetDirectory.getName() + "-Result");
//        folderName.append(String.valueOf(calendar.get(Calendar.MONTH) + 1) + "-");
//        folderName.append(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "-");
//        folderName.append(String.valueOf(calendar.get(Calendar.YEAR)));
        File processedDirectory = new File(resDirectory, folderName.toString());
        processedDirectory.mkdirs();
        processDirectory(ansSheetDirectory, processedDirectory, true);

        File[] files = processedDirectory.listFiles();
        File newDirectory = new File(processedDirectory, "PerspectiveTransform");
        newDirectory.mkdirs();
        for (File file : files) {
            String name = file.getName();
            file = new File(new File(file, "0-Preprocess"), "2-transform.jpg");
            if (!file.exists())
                continue;
            File newFile = new File(newDirectory, name + ".jpg");
            try {
                copyFileUsingStream(file, newFile);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static void processDirectory(File directory, File outputDirectory, boolean recognize) {
        long startTime, endTime;
        if (!directory.isDirectory())
            return;

        InputStream in = null;
        try {
            in = new FileInputStream(new File(RES_DIR, "P-40.asmf"));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        AnswerSheetMetadata metadata = new AnswerSheetMetadata(in);

        File[] files = directory.listFiles();

        PrintWriter printWriter = null;
        if(recognize) {        
            try {
                File file = new File(outputDirectory, outputDirectory.getName() + ".txt");
                file.createNewFile();
                printWriter = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                System.err.println("File not found!");
            } catch (IOException e) {
                System.err.println("Can't create file!");
            }
        
            printWriter.println("label\toskok val\tosknk val\tnskok val\tnsknk val\toskok\tosknk\tnskok\tnsknk");
        }
        
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() || !files[i].getName().endsWith(".jpg")) {
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

            
            File scoringDirectory = new File(output, "2-Normalization and Detection");
            scoringDirectory.mkdirs();

            // CALCULATE THRESHOLD AND ACCURACY
            if(recognize) {
                final double maxNewKernel = 78.0;
                final double newKernelThreshold = 10.0 / maxNewKernel;
                final double oldKernelThreshold = 0.05;
                for (AnswerMat answerMat : answerMats) {
                    String temp = output.getName() + "-" + answerMat.getLabel().toString();
                    double nsknk = FeatureExtractor.getFeatureX(answerMat.clone(), true, true,
                            scoringDirectory.getAbsolutePath() + "/nsknk-" + temp) / maxNewKernel;
                    double osknk = FeatureExtractor.getFeatureX(answerMat.clone(), true, false,
                            scoringDirectory.getAbsolutePath() + "/osknk-" + temp) / maxNewKernel;
                    double nskok = FeatureExtractor.getFeatureX(answerMat.clone(), false, true,
                            scoringDirectory.getAbsolutePath() + "/nskok-" + temp) / 57.0;
                    double oskok = FeatureExtractor.getFeatureX(answerMat.clone(), false, false,
                            scoringDirectory.getAbsolutePath() + "/oskok-" + temp) / 57.0;
                    printWriter.println(temp + "\t" + oskok + "\t" + osknk + "\t" + nskok + "\t" + nsknk + "\t"
                            + (oskok >= oldKernelThreshold ? 1 : 0) + "\t" + (osknk >= newKernelThreshold ? 1 : 0) + "\t"
                            + (nskok >= oldKernelThreshold ? 1 : 0) + "\t" + (nsknk >= newKernelThreshold ? 1 : 0) + "\t");
                }
            } else {
                startTime = System.nanoTime();
                AnswerSheet answerSheet = AnswerSheetScorer.recognizeAnswerSheet(answerMats, scoringDirectory);
                endTime = System.nanoTime();
                System.out.println("Scoring answer sheet in " + ((double) (endTime - startTime)) / (double) 1e9);
                
                boolean scored = false;
                
                if(AnswerKey.isAnswerKey(answerSheet)) {
                    answerKey = AnswerKey.fromAnswerSheet(answerSheet);
                } else if(answerKey != null) {
                    AnswerSheetScorer.scoreAnswerSheet(answerSheet, answerKey);
                    scored = true;
                }
                
                File recognitionResult = new File(output, "result.txt");
    
                try {
                    recognitionResult.createNewFile();
                    PrintWriter pw = new PrintWriter(recognitionResult);
                    pw.println("EXCode = " + answerSheet.getExCode());
                    pw.println("MCode = " + answerSheet.getMCode());
                    pw.println();
                    pw.flush();
                    Option[] options = Option.values();
                    for (int number = 1; number <= answerSheet.getTotalAnswer(); number++) {
                        Answer answer = answerSheet.getAnswerOn(number);
                        pw.print(number + "\t");
                        for (Option option : options) {
                            if(answer.isOptionChosen(option)) pw.print(option.getOption());
                        }
                        
                        if(scored) {
                            pw.print("\t" + answerSheet.getAnswerVerdict(number));
                        }
                        
                        pw.println();
                        pw.flush();
                    }
                    if(scored) {
                        pw.println("Score = " + answerSheet.getTotalCorrect() + " / " + answerSheet.getTotalAnswer());
                    }
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }                
            }
            
            System.out.println("File " + files[i].getName() + " is processed.");
        }

        if(recognize) printWriter.close();
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}
