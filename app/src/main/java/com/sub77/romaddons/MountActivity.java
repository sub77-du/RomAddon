package com.sub77.romaddons;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class MountActivity extends Activity {

    String httpList;
    String httpPref;

    TextView tvm1;
    Button btm1;

    Button btdb1;

    SharedPreferences prefs;
    SharedPreferences.Editor prefseditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mount);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        prefs = this.getSharedPreferences("mounts", MODE_PRIVATE);
        prefseditor = prefs.edit();

        tvm1 = (TextView) findViewById(R.id.TextViewm1);
        btm1 = (Button) findViewById(R.id.btn_m1);
        btdb1 = (Button) findViewById(R.id.btn_db1);
    }

    public void onClick(View v) {

        if (btm1.isPressed()) {
            httpList = "mountsFile.txt";
            readTxt();
        }

        if (btdb1.isPressed()) {
            handleIntent();
        }

        switch (v.getId()) {

            case  R.id.btn_m1:{

            }
        }
    }

    DatabaseTable databaseTable = new DatabaseTable(this);

    private void handleIntent() {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Cursor c = databaseTable.getWordMatches(query, null);
            //process Cursor and display results
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
                prefseditor.putString("mount"+String.valueOf(linenumber), line);
                prefseditor.commit();
                linenumber=linenumber+1;
            }
            br.close();
        }

        catch (IOException e) {
            Log.e("RomAddon", e.getMessage());
            e.printStackTrace();
        }

        tvm1.setText(text.toString());
    }
}
