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
//			Mat mat = ImageConverter.bufferedImageToMat(img);
			Mat mat = Imgcodecs.imread(files[i].getAbsolutePath());
		// process the image
			System.out.println("Start find paper => " + files[i].getName());
			startTime = System.nanoTime();
			Mat process = AnswerSheetScorer.convertAnswerSheet(mat, outputDirectory, files[i].getName());
			endTime = System.nanoTime();
			System.out.println("Converting image in " + ((double)(endTime - startTime)) / (double)1e9);
			/*ArrayList<MatOfPoint> squares = new ArrayList<>();
			AnswerSheetScorer.findSquares(mat, squares);
			// output the image in new file
			double maxArea = 0;
			int maxIdx = -1;
			for(int j=0; j<squares.size(); j++) {
				Rect rect = Imgproc.boundingRect(squares.get(j));
				if(rect.area() > maxArea) {
					maxIdx = j;
					maxArea = rect.area();
				}					
			}
			Imgproc.drawContours(mat, squares, maxIdx, new Scalar(255, 0, 0), 2);
			
			Rect rect = Imgproc.boundingRect(squares.get(maxIdx));
			Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(0, 255, 0), 3);
			Imgcodecs.imwrite(
					new File(processedDirectory, "Squares_" + files[i].getName()).getAbsolutePath(), 
					mat);
			
			System.out.println("Start perspective transform");
			Mat perspective = new Mat(mat.rows(), mat.cols(), mat.type());
			MatOfPoint2f dst = new MatOfPoint2f(
					new Point(0, 0),
					new Point(0, mat.rows()-1),
					new Point(mat.cols()-1, mat.rows()-1),
					new Point(mat.cols()-1, 0)
			);
			
			Mat transform = Imgproc.getPerspectiveTransform(new MatOfPoint2f(squares.get(maxIdx).toArray()), dst);
			Imgproc.warpPerspective(mat, perspective, transform, new Size(mat.cols(), mat.rows()));
			Imgcodecs.imwrite(
					new File(processedDirectory, "Perspective_" + files[i].getName()).getAbsolutePath(), 
					perspective);
			
			/*perspective = perspective.clone();
			System.out.println("Start find answer squares");
			squares.clear();
			AnswerSheetScorer.findSquares(perspective, squares);
			Imgproc.drawContours(perspective, squares, -1, new Scalar(0, 255, 0), 2);
			Imgcodecs.imwrite(
					new File(processedDirectory, "Square_Perspective_" + files[i].getName()).getAbsolutePath(), 
					perspective);
			*/
			File p = new File(outputDirectory, "After perspective");
			p.mkdirs();
			System.out.println("Process answer sheet");
			startTime = System.nanoTime();
			AnswerSheetScorer.processAnswerSheet(process, p, files[i].getName());
			endTime = System.nanoTime();
			System.out.println("Processing answer sheet in " + ((double)(endTime - startTime)) / (double)1e9);
		// Log
			System.out.println("File " + files[i].getName() + " is processed.");
		}
	}

}
