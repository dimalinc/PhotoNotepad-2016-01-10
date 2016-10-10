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


public class Activity_Notepad_Select extends Activity implements View.OnClickListener {

    Helper_DB dbHelper = new Helper_DB(this);
    //   SQLiteDatabase db = dbHelper.getWritableDatabase();

    final static String tableName = "notepads_Table";

    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_NOTEPADS_LIST = "notepadsList";
    static SharedPreferences mSettings;

    final static int REQUEST_CODE_NEW_NOTEPAD = 5;
    final static int REQUEST_CODE_OLD_NOTEPAD = 6;

    final String noteListSerializeFilename = Data.programDirectoryName + File.separator + APP_PREFERENCES_NOTEPADS_LIST + ".ser";

    static ListAdapter adapter;
    static ListView listView;
    Button btnAddNotepad, btnDeleteAllNotepads;

    public static ArrayList<Notepad> notePadsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        creationInit();

        notePadsListInit();

        adapterInit();

        //Обрабатываем щелчки на элементах ListView:
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(Activity_Notepad_Select.this, Activity_Notes_List.class);

              //  Notepad putNotepad = notePadsList.get(position);

                intent.putExtra("id", /*putNotepad.id*/ position);

                //запускаем вторую активность
                // startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_OLD_NOTEPAD);

            }
        });

    }

    void creationInit() {
    setContentView(R.layout.activity_notepad_select);
    // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    // setSupportActionBar(toolbar);

    //  dbHelper = new Helper_DB(this);

       /* SQLiteDatabase db = openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS TutorialsPoint(Username VARCHAR,Password VARCHAR);");
        db.execSQL("INSERT INTO TutorialsPoint VALUES('admin','admin');");

        Cursor resultSet = db.rawQuery("Select * from TutorialsPoint",null);
        resultSet.moveToFirst();
        String username = resultSet.getString(1);
        String password = resultSet.getString(2);*/

    mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

    btnAddNotepad = (Button) findViewById(R.id.buttonAddNotepad);
    btnDeleteAllNotepads = (Button) findViewById(R.id.buttonDeleteAlNotepads);
    btnAddNotepad.setOnClickListener(this);
    btnDeleteAllNotepads.setOnClickListener(this);

    listView = (ListView) findViewById(R.id.listView);}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent incomingIntent) {
        super.onActivityResult(requestCode, resultCode, incomingIntent);
        Log.d("myLogs", "requestCode = " + requestCode + ", resultCode = " + resultCode);


        if ((requestCode == REQUEST_CODE_NEW_NOTEPAD) && (resultCode == RESULT_OK)) {

//            int gotNotepadId = incomingIntent.getParcelableExtra("notepadID");
  //          Log.d("myLogs","gotNotePad ID = " + gotNotepadId);



            Notepad gotNotePad = (Notepad) incomingIntent.getParcelableExtra("notepad");
            Log.d("myLogs","gotNotePad ID = " + gotNotePad.id);

            boolean oldNotepad = false;

            Log.d("myLogs","notePadsList.size = " + notePadsList.size());

            for (Notepad notePad : notePadsList) {
                Log.d("myLogs","notepadID from notepadList = " + notePad.id);
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

            Log.d("myLogs", "NotePad extracted from parcel");

            adapterInit();
        }

        //

        if ((requestCode == REQUEST_CODE_OLD_NOTEPAD) && (resultCode == RESULT_OK)) {

            Notepad gotNotePad = (Notepad) incomingIntent.getParcelableExtra("notepad");

            boolean oldNotepad = false;
            for (Notepad notePad : notePadsList) {
                if (notePad.id == gotNotePad.id) {
                    oldNotepad = true;
                    break;
                }
            }

            if (!oldNotepad)
                notePadsList.add(gotNotePad);
            else {
//                notePadsList.remove(gotNotePad.id);
                notePadsList.add(gotNotePad);
            }

            // сохранение списка блокнотов

            Notepad.saveNotepads(notePadsList);
            notePadsList = Notepad.readNotepadsListFromFile();


            Log.d("myLogs", "gotNotePad read from intent name = " + gotNotePad.name);

            // textView.setText(gotNotePad.name);

            Log.d("myLogs", "NotePad extracted from parcel");

            adapterInit();
        }

    }

    void readNotepadsFromDB() {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        Log.d("myLogs", "--- read Rows in notepads_Table: ---");
        // делаем запрос всех данных из таблицы  notepads_Table, получаем Cursor
        Cursor c = db.query(tableName, null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            /*int dateCreatedColIndex = c.getColumnIndex("dateCreated");
            int dateModifiedColIndex = c.getColumnIndex("dateModified");*/

            do {


                // получаем значения по номерам столбцов и пишем все в лог
                Log.d("myLogs",
                        "ID = " + c.getInt(idColIndex) +
                                ", name = " + c.getString(nameColIndex)); /*+
                                ", dateCreated = " + c.getLong(dateCreatedColIndex) +
                                ", dateModified = " + c.getLong(dateModifiedColIndex));*/
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        } else
            Log.d("myLogs", "0 rows in notepads_Table");
        c.close();
    }

    void notePadsListInit() {
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Log.d("myLogs", "--- read Rows in notepads_Table: ---");
        // делаем запрос всех данных из таблицы  notepads_Table, получаем Cursor
        Cursor c = db.query(tableName, null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
           /* int dateCreatedColIndex = c.getColumnIndex("dateCreated");
            int dateModifiedColIndex = c.getColumnIndex("dateModified");*/

            do {
                // получаем значения по номерам столбцов и создаем объекты Notepad, добавляем их в notepadsList
                Log.d("myLogs",
                        "ID = " + c.getInt(idColIndex) +
                                ", name = " + c.getString(nameColIndex) /*+
                                ", dateCreated = " + c.getLong(dateCreatedColIndex) +
                                ", dateModified = " + c.getLong(dateModifiedColIndex)*/);

                Notepad newNotepad = new Notepad(c.getInt(idColIndex), c.getString(nameColIndex), 0, 0);
                notePadsList.add(newNotepad);

                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        } else
            Log.d("myLogs", "0 rows in notepads_Table");
        c.close();
    }

    void deleteNotepadsFromDB() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Log.d("myLogs", "--- Clear mytable: ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d("myLogs", "deleted rows count = " + clearCount);

        adapterInit();

    }

    void adapterInit() {
        adapter = new SimpleAdapter(this, Helper_Adapter.createDataArrayList(), R.layout.notepad_row,
                new String[]{"name", "date"}, new int[]{
                R.id.tvNotepadName, R.id.tvNotepadDate});
        listView.setAdapter(adapter);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonAddNotepad:
                addNotepad();
                break;
            case R.id.buttonDeleteAlNotepads:
                Notepad.deleteAlNotepads();
                deleteNotepadsFromDB();
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
            Notepad.lastId = mSettings.getInt(APP_PREFERENCES_NOTEPADS_LIST, 0);
        }
    }

    void addNotepad() {

        Intent intent = new Intent(this, Activity_Notes_List.class);

        // newNotePad = new NotePad();
        // intent.putExtra("newNotepad",newNotePad);

        startActivityForResult(intent, REQUEST_CODE_NEW_NOTEPAD);

    }
}
