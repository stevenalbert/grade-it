import java.io.File;
import java.util.Calendar;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {

	public static final String RES_DIR = "res";
	
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
		if(!directory.isDirectory()) return;
		File[] files = directory.listFiles();
		for(int i=0; i<files.length; i++) {
			if(files[i].isDirectory()) {
				/*File newOutputDirectory = new File(outputDirectory, files[i].getName());
				newOutputDirectory.mkdirs();
				processDirectory(files[i], newOutputDirectory);*/
				continue;
			}
			// read answer sheet photo image file
			Mat mat = Imgcodecs.imread(files[i].getAbsolutePath());
			
			// process the image
			System.out.println("Start find paper => " + files[i].getName());
			startTime = System.nanoTime();
			Mat process = AnswerSheetScorer.convertAnswerSheet(mat, outputDirectory, files[i].getName());
			endTime = System.nanoTime();
			System.out.println("Converting image in " + ((double)(endTime - startTime)) / (double)1e9);

			File p = new File(outputDirectory, "After perspective");
			p.mkdirs();
			System.out.println("Process answer sheet");
			startTime = System.nanoTime();
			AnswerSheetScorer.processAnswerSheet(process, p, files[i].getName());
			endTime = System.nanoTime();
			System.out.println("Processing answer sheet in " + ((double)(endTime - startTime)) / (double)1e9);
			System.out.println("File " + files[i].getName() + " is processed.");
		}
	}

}
