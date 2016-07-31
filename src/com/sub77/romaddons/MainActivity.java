package com.sub77.romaddons;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import eu.chainfire.libsuperuser.Shell;

import static android.content.ContentValues.TAG;
import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends Activity implements View.OnClickListener {

    /*
    public CheckBox cb1;
    final String KEY_CB1 = "KEY_CB1";
    String VALUE_CB1;
    String READ_CB1;
    TextView tv1;
    */
    String READ_URL;

    String httpList;
    String httpPref;

    TextView tv2;
    EditText et1;

    Button bt1;
    Button bt2;
    Button bt3;
    Button bt4;
    Button bt5;
    Button bt6;

    SharedPreferences prefs;
    SharedPreferences.Editor prefseditor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        prefs = this.getSharedPreferences("settings", MODE_PRIVATE);
        prefseditor = prefs.edit();

        /*cb1 = (CheckBox) findViewById(R.id.CheckBox1);
        cb1.setOnClickListener(this);
        tv1 = (TextView) findViewById(R.id.TextView1);
        */
        tv2 = (TextView) findViewById(R.id.TextView2);
        et1 = (EditText) findViewById(R.id.EditText1);
        bt1 = (Button) findViewById(R.id.btn_1);
        bt2 = (Button) findViewById(R.id.btn_2);
        bt3 = (Button) findViewById(R.id.btn_3);
        bt4 = (Button) findViewById(R.id.btn_4);

        checkPersist();
    };

    @Override
    public void onClick(View v) {

        /*if (cb1.isChecked()) {
            VALUE_CB1 = "checked";
        } else {
            VALUE_CB1 = "unchecked";
        }
        */

        if (bt1.isPressed()) {
            httpList = "mixFile.txt";
            httpPref = "m";
            downloadTxt();
            readTxt();
            downloadApk();
        }

        if (bt2.isPressed()) {
            httpList = "mixAddonFile.txt";
            httpPref = "ma";
            downloadTxt();
            readTxt();
            downloadApk();
        }

        if (bt3.isPressed()) {
            checkPersist();
            httpList = "persistFile.txt";
            httpPref = "p";
            File cpto = new File(getApplicationContext().getFilesDir().getPath() + "/RomAddon/"+httpList);
            Shell.SU.run(("cp -p /persist/persistFile.txt"+" " + cpto));
            readTxt();
            downloadApk();
        }

        if (bt4.isPressed()) {
            (new AlertDialog.Builder(this))
                    .setTitle(R.string.persistent_title)
                    .setMessage(Html.fromHtml(getString(R.string.persistent_description)))
                    .setCancelable(true)
                    .setNeutralButton(android.R.string.ok, null).show();
            File check = new File(getApplicationContext().getFilesDir().getPath() + "RomAddon/persistTemplateFile.txt");
            File check2 = new File(getApplicationContext().getFilesDir().getPath() + "RomAddon/persistFile.txt");
            if (!check.exists()) {
                httpList = "persistTemplateFile.txt";
                downloadTxt();
            }   if (check2.exists()) {
                bt4.setText("Update persistent FileList");
                Shell.SU.run(("mount -o rw,remount /persist"));
                Shell.SU.run(("rm" +" "+"/persist/persistFile.txt"));
                Shell.SU.run(("cp -p "+ check2 +" "+"/persist/persistFile.txt"));
                Shell.SU.run(("mount -o ro,remount /persist"));
            }
        }

        //prefseditor.putString(KEY_CB1, VALUE_CB1);
        //prefseditor.commit();

        switch (v.getId()) {

            /*case  R.id.CheckBox1:{
                READ_CB1 =  prefs.getString(KEY_CB1,"KeinText gespeichert");
                tv1.setText(READ_CB1);
            }
            */

            case  R.id.btn_3:{
                checkPersist();
            }
        }
    }

    private void checkPersist() {
        File check = new File("persist/persistFile.txt");
        if (!check.exists()) {
            bt3.setText("no persistent FileList found");
            //bt3.setClickable(false);
        }
    }

    private void editPersist() {
        et1.setVisibility(View.VISIBLE);
        File file = new File(getApplicationContext().getFilesDir().getPath() + "RomAddon/templateFile.txt");
        String filename = "myfile";
        et1 = (EditText) findViewById(R.id.EditText1);
        String res = et1.getText().toString();
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(res, Context.MODE_PRIVATE);
            outputStream.write(res.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadTxt(){
        try {
            URL url = new URL("http://android.comtek-wiebe.de/.mix/"+httpList);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            File folder = new File(getApplicationContext().getFilesDir().getPath() + "RomAddon");

            boolean success = true;
            if (!folder.exists()) {
                Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
                success = folder.mkdir();
            }
            if (success) {
                Toast.makeText(MainActivity.this, "Directory Created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed - Error", Toast.LENGTH_SHORT).show();
            }

            File file = new File(getApplicationContext().getFilesDir().getPath() + "RomAddon/templateFile.txt");

            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
            //this.checkUnknownSourceEnability();
            //this.initiateInstallation();

        } catch (MalformedURLException e) {
            Log.e("RomAddon", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("RomAddon", e.getMessage());
            e.printStackTrace();
        }
    }

    private void readTxt() {
        File file = new File(getApplicationContext().getFilesDir().getPath() + "RomAddon/templateFile.txt");
        prefseditor.clear();
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int linenumber=1;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
                prefseditor.putString("url"+String.valueOf(linenumber), line);
                prefseditor.commit();
                linenumber=linenumber+1;
            }
            br.close();
        }

        catch (IOException e) {
            Log.e("RomAddon", e.getMessage());
            e.printStackTrace();
        }

        tv2.setText(text.toString());
        //et1.setText(text.toString());
    }

    private void downloadApk(){
        try {
            int inst=1;
            READ_URL = prefs.getString("url"+String.valueOf(inst), "Keine URL gespeichert");
            String newurl;
            while (READ_URL != null) {
                newurl = prefs.getString("url"+String.valueOf(inst), "Keine URL gespeichert");
                URL url = new URL(newurl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                File file = new File(getApplicationContext().getFilesDir().getPath() + "RomAddon/"+httpPref+String.valueOf(inst)+"name.apk");

                FileOutputStream fileOutput = new FileOutputStream(file);
                InputStream inputStream = urlConnection.getInputStream();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(getApplicationContext().getFilesDir().getPath() + "RomAddon/"+httpPref+String.valueOf(inst)+"name.apk"));
                intent.setDataAndType(uri, "application/vnd.android.package-archive");

                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                }
                fileOutput.close();

                startActivity(intent);
                Log.i("RomAddon","installed");
                inst=inst+1;
            }

        } catch (MalformedURLException e) {
            Log.e("RomAddon", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("RomAddon", e.getMessage());
            e.printStackTrace();
        }

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }


    }

};
