package io.github.stevenalbert.gradeit.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.stevenalbert.gradeit.BuildConfig;
import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.util.FileUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class GetMarkFragment extends Fragment {

    // TAG
    private static final String TAG = GetMarkFragment.class.getSimpleName();

    // Permission Request Code
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 103;

    // Action Request Code
    private static final int PHOTO_REQUEST = 1000;
    private static final int GALLERY_REQUEST = 2000;

    private OnFragmentInteractionListener mListener;

    private Uri imageUri;

    public GetMarkFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_get_mark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button takePhotoButton;
        Button fromGalleryButton;
        Button downloadAnswerSheet;

        takePhotoButton = view.findViewById(R.id.photo);
        fromGalleryButton = view.findViewById(R.id.gallery);
        downloadAnswerSheet = view.findViewById(R.id.download_answer_sheet);

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Take picture from camera
                getPhotoByCamera();
            }
        });

        fromGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Take picture from gallery
                getImageFromGallery();
            }
        });

        downloadAnswerSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Download to "Download" directory
                downloadAnswerSheet();
            }
        });
    }

    private void getPhotoByCamera() {

        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, CAMERA_PERMISSION_CODE);
            return;
        }

        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (photoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            final String subDirectory = "Gradeit_Answersheet";

            // Create the image File
/*
            // To private external directory
            File storageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), subDirectory);
            storageDir.mkdirs();
            File imageFile = new File(storageDir, "answer_sheet.jpg");
            imageUri = FileProvider.getUriForFile(getContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    imageFile);
*/

            // To public external directory
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), subDirectory);
            storageDir.mkdirs();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "AS_" + timeStamp + ".jpg";
            File imageFile = new File(storageDir, imageFileName);
/*
            imageUri = FileProvider.getUriForFile(getContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    imageFile);
*/
            imageUri = Uri.fromFile(imageFile);

            Log.d(TAG, "Image Uri: " + imageUri);
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            photoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(photoIntent, PHOTO_REQUEST);
        }
    }

    private void getImageFromGallery() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void downloadAnswerSheet() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
            return;
        }
        String subDirectory = "Gradeit_Answersheet";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subDirectory);
        storageDir.mkdirs();

        String[] pdfFilenames = getResources().getStringArray(R.array.metadata_filename);
        for(String filename : pdfFilenames) {
            FileUtils.copyFromAsset(getContext(), "pdf_form/" + filename + ".pdf", new File(storageDir, filename + ".pdf"));
        }

        mListener.onFinishDownloadAnswerSheet(storageDir);
    }

    private void processImage(Uri uri) {
        if(uri == null) {
            Toast.makeText(getContext(), "No image chosen", Toast.LENGTH_SHORT).show();
        }
        if(mListener != null)
            mListener.onProcessImageTaken(uri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onProcessImageTaken(Uri uri);
        void onFinishDownloadAnswerSheet(File downloadFolder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            // Send to next activity for processing answer sheet
            // Log.d(TAG, "Activity result: " + imageUri.answerToString());
            processImage(imageUri);
        }
        if(requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();
            // Send to next activity for processing answer sheet
            // Log.d(TAG, "Activity result: " + imageUri.answerToString());
            processImage(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult in");
        if(requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult in gallery");
                getImageFromGallery();
            } else {
                Toast.makeText(getContext(), "Can't get image from gallery", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult in download");
                downloadAnswerSheet();
            } else {
                Toast.makeText(getContext(), "Can't write to storage", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult in photo");
                getPhotoByCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
