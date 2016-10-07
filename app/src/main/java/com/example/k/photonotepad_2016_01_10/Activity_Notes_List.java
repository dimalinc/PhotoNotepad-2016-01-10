package com.example.k.photonotepad_2016_01_10;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Activity_Notes_List extends AppCompatActivity implements View.OnClickListener{

    public static final int ID_DEFAULT_VALUE = -1;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_LAST_NOTEPAD_ID = "last_notepad_id";
    static SharedPreferences mSettings;

   Helper_DB dbHelper = new Helper_DB(this);

    static String notepadName;

    static int lastId=0;

    static Notepad notepad;

    Button addNote, deleteNote,cleanProgramDir, btnSaveNotepad;

    static int noteIndex = 0;

    static final int REQUEST_CODE_NEW_NOTE = 4;

    static ArrayList<Note> noteList = new ArrayList<>();
    ArrayList<File> listFiles;

    TextView textView;
    EditText etNotepadName;

    ListAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        // notepad = (NotePad)getIntent().getExtras().get("newNotepad");


        listView = (ListView) findViewById(R.id.listView);

       /* noteList.add(new Note(01,"name123","category123"));
        noteList.add(new Note(02,"name124","category124"));*/


        //TODO write generation of notepadNames

        addNote = (Button)findViewById(R.id.buttonAddNote);
        deleteNote = (Button)findViewById(R.id.buttonDeleteNote);
        cleanProgramDir = (Button)findViewById(R.id.buttonCleanProgramDir);
        btnSaveNotepad = (Button)findViewById(R.id.btnSaveNotepad);
        addNote.setOnClickListener(this);
        deleteNote.setOnClickListener(this);
        cleanProgramDir.setOnClickListener(this);
        btnSaveNotepad.setOnClickListener(this);
        textView = (TextView)findViewById(R.id.textView);
        etNotepadName = (EditText)findViewById(R.id.etNotepadName);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);



        Intent intent = getIntent();

        int id = intent.getIntExtra("id",ID_DEFAULT_VALUE);

        if (id == ID_DEFAULT_VALUE) {

            if (mSettings.contains(APP_PREFERENCES_LAST_NOTEPAD_ID)) {
                // Получаем число из настроек
                lastId = mSettings.getInt(APP_PREFERENCES_LAST_NOTEPAD_ID, 0);
            }

            etNotepadName.setText("notepad"+ (lastId+1));
            //  etNotepadName.setText(notepad.name);
            etNotepadName.selectAll();
            notepad = new Notepad();
            notepad.name = etNotepadName.getText().toString();


        } else {
            notepad = Activity_Notepad_Select.notePadsList.get(id);
            etNotepadName.setText(notepad.name);
        }

        notepadName = notepad.name;







        // Проверяем и создаем директорию
       Helper_File.checkAndCreateProgramDir();


        noteListInit();

        adapter = new SimpleAdapter(this, createDataArrayList(), R.layout.note_row,
                new String[] { "name", "category" }, new int[] {
                R.id.tv1, R.id.tv2 });
        listView.setAdapter(adapter);

        //Обрабатываем щелчки на элементах ListView:
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(Activity_Notes_List.this, Activity_Note.class);

                intent.putExtra("head", position);

                //запускаем вторую активность
                startActivity(intent);
            }
        });
    }

    void noteListInit() {

        noteList = Note.readNotesFromFileSystem();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_NEW_NOTE) && (resultCode != 0)) {

            Note gotNote = (Note) data.getParcelableExtra("note");

            noteList.add(gotNote);
            Log.d("myLogs", "gotNote read from intent id = " + gotNote.id);

            textView.setText(gotNote.name);

            Log.d("myLogs","Note extracted from parcel" );
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAddNote:
                createNewNote();
                Log.d("myLogs", "buttonAddNote click ");
                Toast.makeText(Activity_Notes_List.this,
                        "buttonAddNote click ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonDeleteNote:
                Log.d("myLogs", "buttonDeleteNote click ");
                Toast.makeText(Activity_Notes_List.this,
                        "buttonDeleteNote click ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonCleanProgramDir:
               Helper_File.deleteFileOrDirRecursievely(new File(Environment.getExternalStorageDirectory(), Activity_Note.programDirectoryName));
               Helper_File.checkAndCreateProgramDir();
                break;
            case R.id.btnSaveNotepad:
                saveNotePad();
                // finish();
                break;
        }
    }

    void createNewNote() {
        noteIndex++;
        // Проверяем и создаем директорию
        File path = new File (Environment.getExternalStorageDirectory(), Activity_Note.programDirectoryName + "/" + notepadName
                + "/" + noteIndex);
        if (! path.exists()){
            if (!path.mkdirs()) {
                Toast.makeText(Activity_Notes_List.this, "unable to create note dir", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(this, Activity_Note.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_NOTE);
    }

    void saveNotePad()  {

// TODO: решить, перенести в класс Notepad или оставить здесть

        saveNotepadToDB();

        Intent intent = new Intent();
        intent.putExtra("notepad", notepad);
        Log.d("myLogs", "notePad returned as result of NotesListActivity");
        setResult(RESULT_OK, intent);
        finish(); // оставить или убрать?
        Log.d("myLogs", "finish() called in saveNotePad()");



    }

    void saveNotepadToDB() {
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // создаем объект для данных
        ContentValues cv = new ContentValues();

        Log.d("myLogs", "--- Insert in table_Notepads: ---");
        // подготовим данные для вставки в виде пар: наименование столбца - значение

        cv.put("id", notepad.id);
        cv.put("name", notepad.name);

        // вставляем запись и получаем ее ID
        long rowID = db.insert(Activity_Notepad_Select.tableName, null, cv);
        Log.d("myLogs", "row inserted in table_Notepads, ID = " + rowID);

        db.close();
    }





    @Override
    protected void onPause() {
        super.onPause();
        // saveNotePad();


    }

    @Override
    protected void onResume() {
        super.onResume();

        listFiles = Helper_File.listFilesWithSubFolders(new File(Environment.getExternalStorageDirectory(),Activity_Note.programDirectoryName));

        for (File file:listFiles) {
            Log.d("myLogs", file.getAbsolutePath());
        }

        adapter = new SimpleAdapter(this, createDataArrayList(), R.layout.note_row,
                new String[] { "name", "category" }, new int[] {
                R.id.tv1, R.id.tv2 });
        listView.setAdapter(adapter);


        if (mSettings.contains(APP_PREFERENCES_LAST_NOTEPAD_ID)) {
            lastId = mSettings.getInt(APP_PREFERENCES_LAST_NOTEPAD_ID,0);
        }
    }

    private ArrayList<HashMap<String, Object>> createDataArrayList() {

        // Упаковываем данные
        ArrayList<HashMap<String, Object>> data = new ArrayList<>(
                noteList.size());
        HashMap<String, Object> map;
        for (int i = 0; i < noteList.size(); i++) {
            map = new HashMap<>();
            map.put("name", noteList.get(i).name);
            map.put("category", noteList.get(i).category);
            data.add(map);
        }

        return data;
    }

}
