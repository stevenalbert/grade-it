package io.github.stevenalbert.answersheetscorer.ui.fragment;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.process.AnalysisProcess;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerKeyViewModel;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerSheetViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnalysisProcessFragment extends Fragment {

    private static final String TAG = AnalysisProcessFragment.class.getSimpleName();
    private static final String M_CODE_KEY = "m_code";

    private LinearLayout progressBarLayout;
    private TableLayout tableLayout;
    private Button saveButtonCsv;
    private Button saveButtonPdf;

    private AnswerKey answerKeyReceived;
    private List<AnswerSheet> answerSheetList;

    private String analysisString;

    private OnAnalysisListener listener;

    public interface OnAnalysisListener {
        void onFinishSaveAnalysis(File file);
    }

    public AnalysisProcessFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analysis_process, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle == null) return;

        int mCode = bundle.getInt(M_CODE_KEY);

        progressBarLayout = view.findViewById(R.id.progress_bar_layout);
        tableLayout = view.findViewById(R.id.analysis_table_layout);
        saveButtonCsv = view.findViewById(R.id.save_button_csv);
        saveButtonPdf = view.findViewById(R.id.save_button_pdf);

        AnswerKeyViewModel answerKeyViewModel = ViewModelProviders.of(this).get(AnswerKeyViewModel.class);
        AnswerSheetViewModel answerSheetViewModel = ViewModelProviders.of(this).get(AnswerSheetViewModel.class);

        answerKeyViewModel.getAnswerKeyByMCode(mCode).observe(this, new Observer<AnswerKey>() {
            @Override
            public void onChanged(@Nullable AnswerKey answerKey) {
                answerKeyReceived = answerKey;
                getAnalysisReport();
            }
        });

        answerSheetViewModel.getAnswerSheetsByMCode(mCode).observe(this, new Observer<List<AnswerSheet>>() {
            @Override
            public void onChanged(@Nullable List<AnswerSheet> answerSheets) {
                answerSheetList = answerSheets;
                getAnalysisReport();
            }
        });

        saveButtonCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnalysisCsv();
            }
        });

        saveButtonPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnalysisPdf();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnAnalysisListener) {
            listener = (OnAnalysisListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement " + OnAnalysisListener.class.getSimpleName());
        }
    }

    public static AnalysisProcessFragment newInstance(int mCode) {
        AnalysisProcessFragment fragment = new AnalysisProcessFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(M_CODE_KEY, mCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void saveAnalysisCsv() {
        String filename = "MC" + answerKeyReceived.getMCodeString() + ".csv";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File analysisFile = new File(storageDir, filename);
        try {
            analysisFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new SaveAsyncTask(analysisFile).execute(analysisString);
    }

    private void saveAnalysisPdf() {
        String filename = "MC" + answerKeyReceived.getMCodeString() + ".pdf";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File analysisFile = new File(storageDir, filename);
        try {
            analysisFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new SaveAsyncTask(analysisFile).execute(analysisString);
    }

    private void onFinishSave(File file, Boolean isSaved) {
        if(isSaved) {
            Toast.makeText(getContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
            createNotification(file);
        } else {
            Toast.makeText(getContext(), R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotification(File file) {
/*
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(file.getName().substring(file.getName().lastIndexOf('.') + 1));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), mimeString);

        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(getContext())
                .setContentTitle(getContext().getString(R.string.app_name))
                .setContentText(file.getName() + " is saved")
                .setSubText("Location: " + file.getAbsolutePath())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
*/
        if(listener != null) {
            listener.onFinishSaveAnalysis(file);
        }
    }

    private synchronized void getAnalysisReport() {
        if(answerKeyReceived != null && answerSheetList != null) {
            new GetAnalysisAsyncTask().execute(answerKeyReceived.getMCode());
        }
    }

    private void fillTable() {
        tableLayout.removeAllViews();

        String[] lines = analysisString.split("\n");
        Log.d(TAG, "Total lines: " + lines.length);
        for(String line : lines) {
            String[] cols = line.split(",");
            Log.d(TAG, "Total columns: " + cols.length);
            TableRow tableRow = new TableRow(getContext());

            for(String col : cols) {
                TextView textView = new TextView(getContext());
                textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.main_menu_item));
                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                textView.setText(col);
                int paddingPixel = getContext().getResources().getDimensionPixelSize(R.dimen.analysis_table_cell_padding);
                textView.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                tableRow.addView(textView);
            }

            tableLayout.addView(tableRow);
        }
    }

    private void onAnalysisReceived(String analysisString) {
        this.analysisString = analysisString;
        fillTable();
        progressBarLayout.setVisibility(View.GONE);
    }

    private void onFailedReceiveAnalysis(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private class GetAnalysisAsyncTask extends AsyncTask<Integer, Void, String> {

        private boolean isSuccess = true;

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                return AnalysisProcess.getAnswersSummary(answerSheetList, answerKeyReceived);
            } catch (Exception e) {
                isSuccess = false;
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isSuccess) onAnalysisReceived(s);
            else onFailedReceiveAnalysis(s);
        }
    }

    private class SaveAsyncTask extends AsyncTask<String, Void, Boolean> {

        private static final int NOT_SUPPORTED_TYPE = 0;
        private static final int CSV_FILE = 1;
        private static final int PDF_FILE = 2;

        private int fileType;
        private File file;

        private SaveAsyncTask(File file) {
            this.file = file;
            String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            switch (ext) {
                case "csv":
                    fileType = CSV_FILE;
                    break;
                case "pdf":
                    fileType = PDF_FILE;
                    break;
                default:
                    fileType = NOT_SUPPORTED_TYPE;
                    break;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            if(strings.length == 0) return false;

            String analysisResult = strings[0];

            switch (fileType) {
                case CSV_FILE: {
                    try {
                        file.createNewFile();
                        PrintWriter writer = new PrintWriter(file);
                        // Write to file
                        writer.write(analysisResult);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } break;
                case PDF_FILE: {

                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        Document document = new Document(PageSize.A3.rotate());
                        PdfWriter.getInstance(document, outputStream);
                        document.open();

                        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 7);

                        String[] lines = analysisString.split("\n");
                        ArrayList<String[]> cellPerLines = new ArrayList<>();
                        int[] colsWidth;
                        int maxColumns = 0;
                        Log.d(TAG, "Total lines: " + lines.length);
                        for(String line : lines) {
                            String[] cols = line.split(",");
                            cellPerLines.add(cols);
                            maxColumns = Math.max(maxColumns, cols.length);
                        }

                        colsWidth = new int[maxColumns];
                        Arrays.fill(colsWidth, 1);
                        colsWidth[0] = colsWidth[1] = colsWidth[maxColumns - 1] = 2;

                        PdfPTable table = new PdfPTable(maxColumns);
                        table.setWidths(colsWidth);
                        table.setHeaderRows(2);
                        for(String[] cellPerLine : cellPerLines) {
                            for(String cellContent : cellPerLine) {
                                PdfPCell cell = new PdfPCell(new Phrase(cellContent, font));
                                cell.setFixedHeight(25);
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                table.addCell(cell);
                            }
                            table.completeRow();
                        }

                        document.add(table);
                        document.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } break;
                default: return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            progressBarLayout.setVisibility(View.GONE);
            onFinishSave(file, b);
        }
    }

}
