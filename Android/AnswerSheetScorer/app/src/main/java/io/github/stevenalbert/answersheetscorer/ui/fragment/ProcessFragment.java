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
import org.opencv.core.Mat;

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

    // Is OpenCV Loaded
    private final int OPENCV_LOAD_WAIT = -1;
    private int openCvLoadStatus = OPENCV_LOAD_WAIT;
    private boolean isWaiting = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext()) {
        @Override
        public void onManagerConnected(int status) {
            openCvLoadStatus = status;
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "OpenCV Loaded successfully");

                    if(isWaiting) {
                        startProcessImage();
                    }

                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

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

                if(openCvLoadStatus == OPENCV_LOAD_WAIT) {
                    isWaiting = true;
                } else if(openCvLoadStatus == BaseLoaderCallback.SUCCESS) {
                    startProcessImage();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getContext(), mLoaderCallback);
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
        bitmapImage = BitmapProcess.getScaledFitBitmap(bitmapImage, imageWidth, imageHeight);

        updateImage(imageView, bitmapImage);
    }

    private void updateImage(ImageView imageView, Bitmap bitmap) {
        if(imageView != null && bitmap != null)
            imageView.setImageBitmap(bitmap);
    }

    private void startProcessImage() {
        ProcessAsyncTask task = new ProcessAsyncTask();
        task.execute(Uri.parse(getArguments().getString(IMAGE_URI)));

        // Disable button
        processButton.setEnabled(false);
    }

    private class ProcessAsyncTask extends AsyncTask<Uri, String, AnswerSheet> {

        @Override
        protected AnswerSheet doInBackground(Uri... uris) {
            if(uris.length == 0) return null;
            Bitmap image = BitmapProcess.getBitmap(getContext(), uris[0]);
            Mat imageMat = new Mat();
            Utils.bitmapToMat(image, imageMat);

            InputStream metadataInputStream = null;
            try {
                metadataInputStream = getContext().getAssets().open("P-40.asmf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(metadataInputStream == null) return null;

            try {
                AnswerSheetMetadata metadata = new AnswerSheetMetadata(metadataInputStream);
                onProgressUpdate("Start converting image");
                imageMat = AnswerSheetScorer.convertAnswerSheet(imageMat, metadata);
                onProgressUpdate("Finish converting image");

                onProgressUpdate("Start retrieving answer sheet squares");
                ArrayList<AnswerMat> matSquares = AnswerSheetScorer.processAnswerSheet(imageMat, metadata);
                onProgressUpdate("Finish retrieving answer sheet squares");

                onProgressUpdate("Start scoring answer sheet");
                AnswerSheet answerSheet = AnswerSheetScorer.scoreAnswerSheet(matSquares);
                onProgressUpdate("Finish scoring answer sheet");

                return answerSheet;
            } catch (Exception e) {
                onProgressUpdate(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d(TAG, values[0]);
        }

        @Override
        protected void onPostExecute(AnswerSheet answerSheet) {
            super.onPostExecute(answerSheet);
            Toast.makeText(getContext(), "Done! Well done!", Toast.LENGTH_SHORT).show();
        }
    }
}
