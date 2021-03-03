package com.example.latihanlibpdf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PdfCreatorActivity";
    private EditText mContentEditText;
    private Button mCreateButton;
    private File pdfFile;
    final private int REQ_CODE_ASK_P = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContentEditText = (EditText) findViewById(R.id.edit_teks);
        mCreateButton = (Button) findViewById(R.id.button_create);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mContentEditText.getText().toString().isEmpty()){
                    mContentEditText.setError("Isi tulisan terlebih dahulu!");
                    mContentEditText.requestFocus();
                    return;
                }
                try{
                    createPdfWrapper();
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (DocumentException d){
                    d.printStackTrace();
                }
            }
        });
    }

    private void createPdfWrapper() throws FileNotFoundException, DocumentException{
        int permisiStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permisiStorage != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)){
                    showMessageOKCancel("You need to allow access to Storage", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE_ASK_P);
                            }
                        }
                    });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE_ASK_P);
            }
            return;
        }else{
            createPDF();
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permisi, int[] grantHasil){
        switch (reqCode){
            case REQ_CODE_ASK_P:
                if(grantHasil[0] == PackageManager.PERMISSION_GRANTED){
                    //permisi di granted (dibiarkan)
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    //permisi ditolak
                    Toast.makeText(this, "Permisi Ditolak", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(reqCode, permisi, grantHasil);
        }
    }

    private void showMessageOKCancel(String pesan, DialogInterface.OnClickListener okListener){
        new AlertDialog.Builder(this)
                .setMessage(pesan)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Keluar", null)
                .create()
                .show();
    }

    private void createPDF() throws FileNotFoundException, DocumentException {
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents" );
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Buat Folder PDF Baru");
        }
        pdfFile = new File(docsFolder.getAbsolutePath(),"PDFgenerate.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document doc = new Document();
        PdfWriter.getInstance(doc, output);
        doc.open();
        doc.add(new Paragraph(mContentEditText.getText().toString()));
        doc.close();
        previewPDF();
    }

    private void previewPDF(){
        PackageManager pmg = getPackageManager();
        Intent tes = new Intent(Intent.ACTION_VIEW);
        tes.setType("application/pdf");
        List list = pmg.queryIntentActivities(tes, PackageManager.MATCH_DEFAULT_ONLY);
        if(list.size() > 0){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
//            Uri uri = Uri.fromFile(pdfFile);
            Uri uri = FileProvider.getUriForFile(
                    this,
                    this.getApplicationContext().getPackageName() + ".provider", pdfFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/pdf");

            startActivity(intent);
        }else{
            Toast.makeText(this,"Download aplikasi pdf viewer untuk melihat hasil generate",Toast.LENGTH_SHORT).show();
        }
    }
}