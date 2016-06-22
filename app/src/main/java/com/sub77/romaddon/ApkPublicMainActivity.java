package com.sub77.romaddon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ApkPublicMainActivity extends android.app.Activity {

    public static final String LOG_TAG = ApkPublicMainActivity.class.getSimpleName();

    private ApkPublicDataSource dataSource;

    private ListView mApkPublicListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_public);

        Log.d(LOG_TAG, "Das Datenquellen-Objekt wird angelegt.");
        dataSource = new ApkPublicDataSource(this);

        initializeApkPublicsListView();

        activateAddButton();
        initializeContextualActionBar();
    }

    private void initializeApkPublicsListView() {
        List<ApkPublic> emptyListForInitialization = new ArrayList<>();

        mApkPublicListView = (ListView) findViewById(R.id.listview_apk_publics);

        // Erstellen des ArrayAdapters für unseren ListView
        ArrayAdapter<ApkPublic> apkPublicArrayAdapter = new ArrayAdapter<ApkPublic> (
                this,
                android.R.layout.simple_list_item_multiple_choice,
                emptyListForInitialization) {

            // Wird immer dann aufgerufen, wenn der übergeordnete ListView die Zeile neu zeichnen muss
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view =  super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                ApkPublic memo = (ApkPublic) mApkPublicListView.getItemAtPosition(position);

                // Hier prüfen, ob Eintrag abgehakt ist. Falls ja, Text durchstreichen
                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175,175,175));
                }
                else {
                    textView.setPaintFlags( textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }

                return view;
            }
        };

        mApkPublicListView.setAdapter(apkPublicArrayAdapter);

        mApkPublicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ApkPublic memo = (ApkPublic) adapterView.getItemAtPosition(position);

                // Hier den checked-Wert des Memo-Objekts umkehren, bspw. von true auf false
                // Dann ListView neu zeichnen mit showAllListEntries()
                ApkPublic updatedApkPublic = dataSource.updateApkPublic(memo.getId(), memo.getApk(), memo.getUrl(), (!memo.isChecked()));
                Log.d(LOG_TAG, "Checked-Status von Eintrag: " + updatedApkPublic.toString() + " ist: " + updatedApkPublic.isChecked());
                showAllListEntries();
            }
        });

    }

    private void showAllListEntries () {
        List<ApkPublic> apkPublicList = dataSource.getAllApkPublics();

        ArrayAdapter<ApkPublic> adapter = (ArrayAdapter<ApkPublic>) mApkPublicListView.getAdapter();

        adapter.clear();
        adapter.addAll(apkPublicList);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        showAllListEntries();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }

    private void activateAddButton() {
        Button buttonAddApk = (Button) findViewById(R.id.button_add_apk);
        final EditText editTextUrl = (EditText) findViewById(R.id.editText_url);
        final EditText editTextApk = (EditText) findViewById(R.id.editText_apk);

        buttonAddApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String urlString = editTextUrl.getText().toString();
                String apk = editTextApk.getText().toString();

                if (TextUtils.isEmpty(urlString)) {
                    editTextUrl.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                if (TextUtils.isEmpty(apk)) {
                    editTextApk.setError(getString(R.string.editText_errorMessage));
                    return;
                }

                String url = urlString;
                editTextUrl.setText("");
                editTextApk.setText("");

                dataSource.createApkPublic(apk, url);

                InputMethodManager inputMethodManager;
                inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                showAllListEntries();
            }
        });

    }

    private void initializeContextualActionBar() {

        final ListView apkPublicsListView = (ListView) findViewById(R.id.listview_apk_publics);
        apkPublicsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        apkPublicsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;

            // In dieser Callback-Methode zählen wir die ausgewählen Listeneinträge mit
            // und fordern ein Aktualisieren der Contextual Action Bar mit invalidate() an
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }
                String cabTitle = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitle);
                mode.invalidate();
            }

            // In dieser Callback-Methode legen wir die CAB-Menüeinträge an
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }

            // In dieser Callback-Methode reagieren wir auf den invalidate() Aufruf
            // Wir lassen das Edit-Symbol verschwinden, wenn mehr als 1 Eintrag ausgewählt ist
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }

                return true;
            }

            // In dieser Callback-Methode reagieren wir auf Action Item-Klicks
            // Je nachdem ob das Löschen- oder Ändern-Symbol angeklickt wurde
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean returnValue = true;
                SparseBooleanArray touchedApkPublicsPositions = apkPublicsListView.getCheckedItemPositions();

                switch (item.getItemId()) {
                    case R.id.cab_delete:
                        for (int i = 0; i < touchedApkPublicsPositions.size(); i++) {
                            boolean isChecked = touchedApkPublicsPositions.valueAt(i);
                            if (isChecked) {
                                int postitionInListView = touchedApkPublicsPositions.keyAt(i);
                                ApkPublic apkPublic = (ApkPublic) apkPublicsListView.getItemAtPosition(postitionInListView);
                                Log.d(LOG_TAG, "Position im ListView: " + postitionInListView + " Inhalt: " + apkPublic.toString());
                                dataSource.deleteApkPublic(apkPublic);
                            }
                        }
                        showAllListEntries();
                        mode.finish();
                        break;

                    case R.id.cab_change:
                        Log.d(LOG_TAG, "Eintrag ändern");
                        for (int i = 0; i < touchedApkPublicsPositions.size(); i++) {
                            boolean isChecked = touchedApkPublicsPositions.valueAt(i);
                            if (isChecked) {
                                int postitionInListView = touchedApkPublicsPositions.keyAt(i);
                                ApkPublic apkPublic = (ApkPublic) apkPublicsListView.getItemAtPosition(postitionInListView);
                                Log.d(LOG_TAG, "Position im ListView: " + postitionInListView + " Inhalt: " + apkPublic.toString());

                                AlertDialog editApkPublicDialog = createEditApkPublicDialog(apkPublic);
                                editApkPublicDialog.show();
                            }
                        }

                        mode.finish();
                        break;

                    default:
                        returnValue = false;
                        break;
                }
                return returnValue;
            }

            // In dieser Callback-Methode reagieren wir auf das Schließen der CAB
            // Wir setzen den Zähler auf 0 zurück
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;
            }
        });
    }

    private AlertDialog createEditApkPublicDialog(final ApkPublic apkPublic) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_public_apk, null);

        final EditText editTextNewUrl = (EditText) dialogsView.findViewById(R.id.editText_new_url);
        editTextNewUrl.setText(String.valueOf(apkPublic.getUrl()));

        final EditText editTextNewApk = (EditText) dialogsView.findViewById(R.id.editText_new_apk);
        editTextNewApk.setText(apkPublic.getApk());

        builder.setView(dialogsView)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String urlString = editTextNewUrl.getText().toString();
                        String apk = editTextNewApk.getText().toString();

                        if ((TextUtils.isEmpty(urlString)) || (TextUtils.isEmpty(apk))) {
                            Log.d(LOG_TAG, "Ein Eintrag enthielt keinen Text. Daher Abbruch der Änderung.");
                            return;
                        }

                        //int url = Integer.parseInt(urlString);
                        String url = urlString;

                        // An dieser Stelle schreiben wir die geänderten Daten in die SQLite Datenbank
                        ApkPublic updatedApkPublic = dataSource.updateApkPublic(apkPublic.getId(), apk, url, apkPublic.isChecked());

                        Log.d(LOG_TAG, "Alter Eintrag - ID: " + apkPublic.getId() + " Inhalt: " + apkPublic.toString());
                        Log.d(LOG_TAG, "Neuer Eintrag - ID: " + updatedApkPublic.getId() + " Inhalt: " + updatedApkPublic.toString());

                        showAllListEntries();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}