package io.github.stevenalbert.answersheetscorer.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationManagerCompat;
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

import io.github.stevenalbert.answersheetscorer.BuildConfig;
import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.util.FileUtils;

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
            final String subDirectory = "Answersheet";

            // Create the image File
/*
            // To private external directory
            File storageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), subDirectory);
            storageDir.mkdirs();
            File imageFile = new File(storageDir, "answer_sheet.jpg");
            imageUri = FileProvider.getUriForFile(getContext(),
                    "io.github.stevenalbert.answersheetscorer.fileprovider",
                    imageFile);
*/

            // To public external directory
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), subDirectory);
            storageDir.mkdirs();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "AS_" + timeStamp + ".jpg";
            File imageFile = new File(storageDir, imageFileName);
            imageUri = FileProvider.getUriForFile(getContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    imageFile);
//            imageUri = Uri.fromFile(imageFile);

            Log.d(TAG, "Image Uri: " + imageUri);
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            photoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(photoIntent, PHOTO_REQUEST);
        }
    }

    private void getImageFromGallery() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void downloadAnswerSheet() {
        String subDirectory = "Answersheet";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subDirectory);
        storageDir.mkdirs();

        File answerSheetFile = FileUtils.copyFromAsset(getContext(), "answer_sheet_template.pdf", storageDir);

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(".pdf");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(answerSheetFile), mimeString);

        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(getContext())
                .setContentTitle(getContext().getString(R.string.app_name))
                .setContentText("Download complete")
                .setSubText(answerSheetFile.getAbsolutePath())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
/*
        DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(answerSheetUri);
        request.setTitle("Downloading Answer Sheet");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, answerSheetFile.getName());
        request.setVisibleInDownloadsUi(true);

        downloadManager.enqueue(request);
*/
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
