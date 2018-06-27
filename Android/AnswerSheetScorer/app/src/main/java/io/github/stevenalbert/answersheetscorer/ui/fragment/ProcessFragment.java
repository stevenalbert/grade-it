package io.github.stevenalbert.answersheetscorer.ui.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import io.github.stevenalbert.answersheetscorer.R;
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
            }
        });
    }

    public static ProcessFragment newInstance(Uri imageUri) {
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
}
