package io.github.stevenalbert.answersheetscorer.ui.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerMat;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheetMetadata;
import io.github.stevenalbert.answersheetscorer.process.AnswerSheetScorer;
import io.github.stevenalbert.answersheetscorer.util.BitmapProcess;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProcessFragment extends Fragment {

    // TAG
    private static final String TAG = ProcessFragment.class.getSimpleName();

    // Image Uri
    private static final String IMAGE_URI = "image_uri";

    // Image dimension
    private ImageView imageView;
    private int imageWidth;
    private int imageHeight;

    // Process button
    private Button processButton;

    // OnProcessFinishListener object
    private OnProcessFinishListener onProcessFinishListener;

    public interface OnProcessFinishListener {
        void onFinish(AnswerSheet answerSheet);
    }

    public ProcessFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_process, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get ImageView
        imageView = view.findViewById(R.id.answer_sheet_image);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageWidth = imageView.getMeasuredWidth();
                imageHeight = imageView.getMeasuredHeight();

                insertScaledImage();
            }
        });

        processButton = view.findViewById(R.id.process_button);
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), R.string.process_button_start, Toast.LENGTH_SHORT).show();
                startProcessImage();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnProcessFinishListener) {
            this.onProcessFinishListener = (OnProcessFinishListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement " + OnProcessFinishListener.class.getSimpleName());
        }
    }

    public static ProcessFragment newInstance(Uri imageUri) {
        if(imageUri == null) return null;

        ProcessFragment fragment = new ProcessFragment();
        Bundle bundle = new Bundle();
        bundle.putString(IMAGE_URI, imageUri.toString());

        fragment.setArguments(bundle);
        return fragment;
    }

    private void insertScaledImage() {
        Uri imageUri = Uri.parse(getArguments().getString(IMAGE_URI));

        Bitmap bitmapImage = BitmapProcess.getExifRotatedBitmap(getContext(), imageUri);
        Log.d(TAG, "width = " + bitmapImage.getWidth() + ", height = " + bitmapImage.getHeight());

        updateImage(imageView, bitmapImage);
    }

    private void updateImage(ImageView imageView, Bitmap bitmap) {
        if(imageView != null && bitmap != null) {
            bitmap = BitmapProcess.getScaledFitBitmap(bitmap, imageWidth, imageHeight);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void startProcessImage() {
        ProcessAsyncTask task = new ProcessAsyncTask();
        task.execute(Uri.parse(getArguments().getString(IMAGE_URI)));

        // Disable button
        processButton.setEnabled(false);
    }

    private void nextProcessAnswerSheet(AnswerSheet answerSheet) {
        if(onProcessFinishListener != null) {
            onProcessFinishListener.onFinish(answerSheet);
        }
    }

    private void notify(String message) {
        Log.i(TAG, message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private class ProcessAsyncTask extends AsyncTask<Uri, Object, AnswerSheet> {

        @Override
        protected AnswerSheet doInBackground(Uri... uris) {
            if(uris.length == 0) return null;
            Long startTime = System.nanoTime(), endTime, sectionStartTime, sectionEndTime;
            Bitmap image = BitmapProcess.getExifRotatedBitmap(getContext(), uris[0]);
            Mat imageMat = new Mat();
            Utils.bitmapToMat(image, imageMat, true);

            InputStream metadataInputStream = null;
            try {
                metadataInputStream = getContext().getAssets().open("P-40.asmf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(metadataInputStream == null) return null;

            try {
                AnswerSheetMetadata metadata = new AnswerSheetMetadata(metadataInputStream);

                sectionStartTime = System.nanoTime();
                publishProgress("Start converting image");
                imageMat = AnswerSheetScorer.convertAnswerSheet(imageMat, metadata);
                publishProgress("Finish converting image");
                sectionEndTime = System.nanoTime();
                image = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(imageMat, image, true);
                publishProgress(image);
                publishProgress(sectionEndTime - sectionStartTime);

                sectionStartTime = System.nanoTime();
                publishProgress("Start retrieving answer sheet squares");
                Mat updateImageMat = new Mat(imageMat.rows(), imageMat.cols(), CvType.CV_8UC3);
                ArrayList<AnswerMat> matSquares = AnswerSheetScorer.processAnswerSheet(imageMat, updateImageMat, metadata);
                publishProgress("Finish retrieving answer sheet squares");
                sectionEndTime = System.nanoTime();
                Utils.matToBitmap(updateImageMat, image, true);
                publishProgress(image);
                publishProgress(sectionEndTime - sectionStartTime);
                updateImageMat.release();

                sectionStartTime = System.nanoTime();
                publishProgress("Start scoring answer sheet");
                AnswerSheet answerSheet = AnswerSheetScorer.scoreAnswerSheet(matSquares);
                publishProgress("Finish scoring answer sheet");
                sectionEndTime = System.nanoTime();
                publishProgress(sectionEndTime - sectionStartTime);

                endTime = System.nanoTime();
                publishProgress("Total elapsed time", endTime - startTime);

                return answerSheet;
            } catch (Exception e) {
                publishProgress(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            for(Object value : values) {
                if(value instanceof String)
                    ProcessFragment.this.notify((String) value);
                else if(value instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) value;
                    updateImage(imageView, bitmap);
                } else if(value instanceof Long) {
                    Log.d(TAG, "Elapsed process time = " + ((Long) value).doubleValue() / 1e9);
                }
            }
        }

        @Override
        protected void onPostExecute(AnswerSheet answerSheet) {
            super.onPostExecute(answerSheet);
            if(answerSheet != null) {
                Toast.makeText(getActivity(), "Answer sheet retrieved! Well done!", Toast.LENGTH_SHORT).show();
                nextProcessAnswerSheet(answerSheet);
            }
            else {
                Toast.makeText(getActivity(), "Failed to get answer sheet", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
