package com.sub77.romaddons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

//import android.widget.Button;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public CheckBox cb1;
    final String KEY_CB1 = "KEY_CB1";

    String VALUE_CB1;
    String READ_CB1;
    String READ_URL;

    String httpList;

    TextView tv1;
    TextView tv2;

    Button bt1;
    Button bt2;

    SharedPreferences prefs;
    SharedPreferences.Editor prefseditor;

    @Override
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
        */
        tv1 = (TextView) findViewById(R.id.TextView1);
        tv2 = (TextView) findViewById(R.id.TextView2);
        bt1 = (Button) findViewById(R.id.btn_1);
        bt2 = (Button) findViewById(R.id.btn_2);
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
            downloadTxt();
            readTxt();
            downloadApk();
        }

        if (bt2.isPressed()) {
            httpList = "mixAddonFile.txt";
            downloadTxt();
            readTxt();
            downloadApk();
        }
        prefseditor.putString(KEY_CB1, VALUE_CB1);
        prefseditor.commit();

        switch (v.getId()) {

            /*case  R.id.CheckBox1:{
                READ_CB1 =  prefs.getString(KEY_CB1,"KeinText gespeichert");
                tv1.setText(READ_CB1);
            }
            */

            case  R.id.btn_1:{

            }
        }
    }

    private void downloadTxt(){
        try {
            URL url = new URL("http://android.comtek-wiebe.de/.mix/"+httpList);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            File folder = new File(Environment.getExternalStorageDirectory() + "/RomAddon");
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

            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "RomAddon/"+httpList);

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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readTxt() {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "RomAddon/"+httpList);
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
            //You'll need to add proper error handling here
        }

        tv2.setText(text.toString());
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

                File sdcard = Environment.getExternalStorageDirectory();
                File file = new File(sdcard,"RomAddon/"+String.valueOf(inst)+"name.apk");

                FileOutputStream fileOutput = new FileOutputStream(file);
                InputStream inputStream = urlConnection.getInputStream();

                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                }
                fileOutput.close();
                //this.checkUnknownSourceEnability();
                //this.initiateInstallation();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(sdcard,"RomAddon/"+String.valueOf(inst)+"name.apk"));
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                startActivity(intent);
                inst=inst+1;
            }

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

    }
};


