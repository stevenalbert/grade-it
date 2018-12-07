package io.github.stevenalbert.gradeit.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.util.AppSharedPreference;
import io.github.stevenalbert.gradeit.util.FileUtils;
import io.github.stevenalbert.gradeit.util.MetadataUtils;

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

    private GetMarkListener mListener;

    private Uri imageUri;

    // Metadata
    private List<String> metadataFormatsFilename;

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
        Spinner formFormatSpinner;

        takePhotoButton = view.findViewById(R.id.photo);
        fromGalleryButton = view.findViewById(R.id.gallery);
        downloadAnswerSheet = view.findViewById(R.id.download_answer_sheet);
        formFormatSpinner = view.findViewById(R.id.metadata_format_spinner);

        takePhotoButton.setOnClickListener((v) ->
                // Take picture from camera
                getPhotoByCamera());

        fromGalleryButton.setOnClickListener((v) ->
                // Take picture from gallery
                getImageFromGallery());

        downloadAnswerSheet.setOnClickListener((v) ->
                // Download to "Download" directory
                downloadAnswerSheet());

        metadataFormatsFilename = MetadataUtils.metadataFormatsFilename();
        ArrayAdapter<String> formFormatAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, metadataFormatsFilename);
        formFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formFormatSpinner.setAdapter(formFormatAdapter);
        formFormatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSharedPreference.saveMetadataString(getContext(), metadataFormatsFilename.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String savedMetadataFilename = AppSharedPreference.getSavedMetadataString(getContext());
        int indexOfSavedMetadata;
        if((indexOfSavedMetadata = metadataFormatsFilename.indexOf(savedMetadataFilename)) != -1) {
            formFormatSpinner.setSelection(indexOfSavedMetadata);
        } else {
            formFormatSpinner.setSelection(0);
        }
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
            final String subDirectory = getString(R.string.app_directory_name);

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
            String imageFileName = getString(R.string.taken_image_filename, timeStamp);
            File imageFile = new File(storageDir, imageFileName);
/*
            imageUri = FileProvider.getUriForFile(getContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    imageFile);
*/
            imageUri = Uri.fromFile(imageFile);

            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
        String subDirectory = getString(R.string.app_directory_name);
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subDirectory);
        storageDir.mkdirs();

        String[] pdfFilenames = getResources().getStringArray(R.array.metadata_filename);
        for(String filename : pdfFilenames) {
            FileUtils.copyFromAsset(getContext(), getString(R.string.pdf_form_asset_file, filename), new File(storageDir, filename + ".pdf"));
        }

        mListener.onFinishDownloadAnswerSheet(storageDir);
    }

    private void processImage(Uri uri) {
        if(uri == null) {
            Toast.makeText(getContext(), R.string.no_image_chosen, Toast.LENGTH_SHORT).show();
        }
        if(mListener != null)
            mListener.onProcessImageTaken(uri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GetMarkListener) {
            mListener = (GetMarkListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement " + GetMarkListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface GetMarkListener {
        void onProcessImageTaken(Uri uri);
        void onFinishDownloadAnswerSheet(File downloadFolder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            // Send to next activity for processing answer sheet
            // Log.d(TAG, "Activity result: " + imageUri.answerToString());
            MediaScannerConnection.scanFile(getContext(), new String[]{imageUri.getPath()}, null, ((path, uri1) -> Log.d(TAG, path + " | " + uri1)));
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

        if(requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery();
            } else {
                Toast.makeText(getContext(), R.string.gallery_restrict_message, Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadAnswerSheet();
            } else {
                Toast.makeText(getContext(), R.string.storage_restrict_message, Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getPhotoByCamera();
            } else {
                Toast.makeText(getContext(), R.string.camera_restrict_message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
