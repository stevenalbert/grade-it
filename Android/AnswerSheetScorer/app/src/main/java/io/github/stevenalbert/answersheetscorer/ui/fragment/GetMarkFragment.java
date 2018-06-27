package io.github.stevenalbert.answersheetscorer.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.util.CameraPermission;
import io.github.stevenalbert.answersheetscorer.util.ExternalStoragePermission;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class GetMarkFragment extends Fragment {

    // TAG
    private static final String TAG = GetMarkFragment.class.getSimpleName();

    // Permission Request Code
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;

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
            }
        });
    }

    private void getPhotoByCamera() {

        if(!CameraPermission.isGranted(getContext()) || !ExternalStoragePermission.isWriteGranted(getContext())) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, CAMERA_PERMISSION_CODE);
            return;
        }

        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (photoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the image File
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(storageDir, "answer_sheet.jpg");

            imageUri = FileProvider.getUriForFile(getContext(),
                    "io.github.stevenalbert.answersheetscorer.fileprovider",
                    imageFile);
            Log.d(TAG, "Image Uri: " + imageUri);
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(photoIntent, PHOTO_REQUEST);
        }
    }

    private void getImageFromGallery() {
        if(!ExternalStoragePermission.isReadGranted(getContext())) {
            ExternalStoragePermission.requestRead(getActivity(), READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            // Send to next activity for processing answer sheet
            // Log.d(TAG, "Activity result: " + imageUri.toString());
            processImage(imageUri);
        }
        if(requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();
            // Send to next activity for processing answer sheet
            // Log.d(TAG, "Activity result: " + imageUri.toString());
            processImage(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery();
            } else {
                Toast.makeText(getContext(), "Can't get image from gallery", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getPhotoByCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
