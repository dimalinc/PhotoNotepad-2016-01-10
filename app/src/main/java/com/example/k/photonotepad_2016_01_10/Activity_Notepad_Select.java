package com.example.k.photonotepad_2016_01_10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;


public class Activity_Notepad_Select extends Activity implements View.OnClickListener{

   static Helper_DB dbHelper;

    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_NOTEPADS_LIST = "notepadsList";
    static SharedPreferences mSettings;

    final static int REQUEST_CODE_NEW_NOTEPAD = 5;

    final String noteListSerializeFilename = Activity_Note.programDirectoryName + File.separator + APP_PREFERENCES_NOTEPADS_LIST+".ser";

   static ListAdapter adapter ;
   static ListView listView;
    Button btnAddNotepad, btnDeleteAllNotepads;

    public static ArrayList<Notepad> notePadsList= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_select);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

        dbHelper = new Helper_DB(this);

       /* SQLiteDatabase db = openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS TutorialsPoint(Username VARCHAR,Password VARCHAR);");
        db.execSQL("INSERT INTO TutorialsPoint VALUES('admin','admin');");

        Cursor resultSet = db.rawQuery("Select * from TutorialsPoint",null);
        resultSet.moveToFirst();
        String username = resultSet.getString(1);
        String password = resultSet.getString(2);*/

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        btnAddNotepad = (Button)findViewById(R.id.buttonAddNotepad);
        btnDeleteAllNotepads = (Button)findViewById(R.id.buttonDeleteAlNotepads);
        btnAddNotepad.setOnClickListener(this);
        btnDeleteAllNotepads.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.listView);

        Notepad.notePadsListInit();

        adapterInit();

        //Обрабатываем щелчки на элементах ListView:
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(Activity_Notepad_Select.this, Activity_Notes_List.class);

                intent.putExtra("id", position);

                //запускаем вторую активность
                // startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_NEW_NOTEPAD);

            }
        });

    }

    void adapterInit() {
        adapter = new SimpleAdapter(this, Helper_Adapter.createDataArrayList(), R.layout.notepad_row,
                new String[] { "name", "date" }, new int[] {
                R.id.tvNotepadName, R.id.tvNotepadDate });
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("myLogs", "requestCode = " + requestCode + ", resultCode = " + resultCode);


        if ((requestCode == REQUEST_CODE_NEW_NOTEPAD) && (resultCode == RESULT_OK)) {

            Notepad gotNotePad = (Notepad) data.getParcelableExtra("notepad");

            boolean oldNotepad = false;
            for (Notepad notePad: notePadsList) {
                if (notePad.id == gotNotePad.id) {
                    oldNotepad = true;
                    break;
                }
            }

            if (!oldNotepad)
                notePadsList.add(gotNotePad);

            // сохранение списка блокнотов

            Notepad.saveNotepads(notePadsList);
            notePadsList = Notepad.readNotepadsListFromFile();




            Log.d("myLogs", "gotNotePad read from intent name = " + gotNotePad.name);

            // textView.setText(gotNotePad.name);

            Log.d("myLogs","NotePad extracted from parcel" );

            adapterInit();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonAddNotepad:
                addNotepad();
                break;
            case R.id.buttonDeleteAlNotepads:
                Notepad.deleteAlNotepads();
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mSettings.contains(APP_PREFERENCES_NOTEPADS_LIST)) {
            Notepad.lastId = mSettings.getInt(APP_PREFERENCES_NOTEPADS_LIST,0);
        }
    }


    void addNotepad() {

        Intent intent = new Intent(this, Activity_Notes_List.class);

        // newNotePad = new NotePad();
        // intent.putExtra("newNotepad",newNotePad);

        startActivityForResult(intent, REQUEST_CODE_NEW_NOTEPAD);

    }

}
