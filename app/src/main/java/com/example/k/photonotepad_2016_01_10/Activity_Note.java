package com.example.k.photonotepad_2016_01_10;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class Activity_Note extends Activity implements View.OnClickListener{


    Helper_DB dbHelper = new Helper_DB(this);
  final static String tableName = "first_Table";

    Note note;
    final static String programDirectoryName = "CameraTest";

    TextView textViewNoteName, textViewPhotoNames;
    EditText etName, etSku, etCategory, etDescription;
    Button buttonSaveNote, buttonTakePicture;
    ArrayList<String> photosList = new ArrayList<String>();

    // variable for photo intent
    private final int PHOTO = 1;
    // variable for selection intent
    private final int PICKER = 2;
    // variable to store the currently selected image
    public static int currentPic = 0;

    private Gallery gallery;
    private ImageView bigimage;
    private Integer[] imgid = { R.drawable.ic_launcher, R.drawable.ic_launcher,
            R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
            R.drawable.ic_launcher, R.drawable.ic_launcher };


    // adapter for gallery view
    private ImageAdapter adapter;

    String name;
    String sku;
    String category;
    String description;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/



        note =  new Note(Activity_Notes_List.noteIndex);

        textViewNoteName = (TextView)findViewById(R.id.tvNoteName);
        textViewPhotoNames = (TextView)findViewById(R.id.tvPhotoNames);
        etName = (EditText)findViewById(R.id.etName);
        etSku = (EditText)findViewById(R.id.etId);
        etCategory = (EditText)findViewById(R.id.etCategory);
        etDescription = (EditText)findViewById(R.id.etDescription);
        buttonSaveNote = (Button)findViewById(R.id.buttonSaveNote);
        buttonTakePicture = (Button)findViewById(R.id.buttonTakePicture);
        //gallery = (Gallery)findViewById(R.id.gallery);

        buttonSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("myLog", "calling TakePicture()");
                takePicture();

            }
        });

        bigimage = (ImageView) findViewById(R.id.picture);

        galleryInitialize();

    }

    void galleryInitialize() {
        gallery = (Gallery) findViewById(R.id.gallery);

        // create a new adapter
        adapter = new ImageAdapter(this);
        // set the gallery adapter
        gallery.setAdapter(adapter);

        // set long click listener for each gallery thumbnail item
        gallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            // handle long clicks
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           int position, long id) {
                // take user to choose an image
                // update the currently selected position so that we assign the
                // imported bitmap to correct item
                currentPic = position;
                // take the user to their chosen image selection app (gallery or
                // file manager)
                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                // we will handle the returned data in onActivityResult
                startActivityForResult(
                        Intent.createChooser(pickIntent, "Select Picture"),
                        PICKER);
                return true;
            }
        });

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // Выводим номер позиции при щелчке на картинке из галереи
                Toast.makeText(Activity_Note.this,
                        "Позиция: " + position, Toast.LENGTH_SHORT).show();
                bigimage.setImageBitmap(adapter.getPic(position));
                bigimage.setImageResource(imgid[position]);

            }
        });
    }

    void takePicture() {
        Intent intent = new Intent(this,Activity_Just_Capture.class);
        startActivityForResult(intent, PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        switch (requestCode) {
            case PHOTO:

                String photoName = data.getStringExtra("pictureFileUri");
                String oldText = textViewNoteName.getText().toString();
                String result = oldText + photoName;
                photosList.add(photoName);

                photosListWriteToFile();

                textViewPhotoNames.setText(photosList.toString());
                break;

            case PICKER:


                // the returned picture URI
                Uri pickedUri = data.getData();

                // declare the bitmap
                Bitmap pic = null;
                // declare the path string
                String imgPath = "";

                // retrieve the string using media data
                String[] medData = { MediaStore.Images.Media.DATA };
                // query the data
                Cursor picCursor = managedQuery(pickedUri, medData, null, null,
                        null);
                if (picCursor != null) {
                    // get the path string
                    int index = picCursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    picCursor.moveToFirst();
                    imgPath = picCursor.getString(index);
                } else
                    imgPath = pickedUri.getPath();

                // if and else handle both choosing from gallery and from file
                // manager

                // if we have a new URI attempt to decode the image bitmap
                if (pickedUri != null) {

                    // set the width and height we want to use as maximum
                    // display
                    int targetWidth = 600;
                    int targetHeight = 400;

                    // sample the incoming image to save on memory resources

                    // create bitmap options to calculate and use sample size
                    BitmapFactory.Options bmpOptions = new BitmapFactory.Options();

                    // first decode image dimensions only - not the image bitmap
                    // itself
                    bmpOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imgPath, bmpOptions);

                    // work out what the sample size should be

                    // image width and height before sampling
                    int currHeight = bmpOptions.outHeight;
                    int currWidth = bmpOptions.outWidth;

                    // variable to store new sample size
                    int sampleSize = 1;

                    // calculate the sample size if the existing size is larger
                    // than target size
                    if (currHeight > targetHeight || currWidth > targetWidth) {
                        // use either width or height
                        if (currWidth > currHeight)
                            sampleSize = Math.round((float) currHeight
                                    / (float) targetHeight);
                        else
                            sampleSize = Math.round((float) currWidth
                                    / (float) targetWidth);
                    }
                    // use the new sample size
                    bmpOptions.inSampleSize = sampleSize;

                    // now decode the bitmap using sample options
                    bmpOptions.inJustDecodeBounds = false;

                    // get the file as a bitmap
                    pic = BitmapFactory.decodeFile(imgPath, bmpOptions);

                    // pass bitmap to ImageAdapter to add to array
                    adapter.addPic(pic);

                    // redraw the gallery thumbnails to reflect the new addition
                    gallery.setAdapter(adapter);

                    // display the newly selected image at larger size
                    bigimage.setImageBitmap(pic);
                    // scale options
                    bigimage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                break;
        }
    }

    private void photosListWriteToFile() {


        Log.d("myLogs", "preparing to generatePhotosListFileName ");
        Toast.makeText(Activity_Note.this,
                "preparing to generatePhotosListFileName ", Toast.LENGTH_SHORT).show();


        String noteFileName = generatePhotosListFileName();

        Log.d("myLogs", "generateNoteDescriptionFileName: " + noteFileName);
        Toast.makeText(Activity_Note.this,
                "generateNoteDescriptionFileName: " + noteFileName, Toast.LENGTH_SHORT).show();

        StringBuilder sb = new StringBuilder();
        for (String photoName: photosList) {
            sb.append(photoName);
            sb.append(System.getProperty("line.separator"));
        }
        String outputString = sb.toString();

        try {
            String fileName = noteFileName.substring(8);
            Log.d("myLogs","fileName: " + fileName);
            Toast.makeText(Activity_Note.this,
                    "fileName: " + fileName, Toast.LENGTH_SHORT).show();


            OutputStream os = new FileOutputStream(fileName);
            os.write(outputString.getBytes());
            os.close();
            Log.d("myLogs","PhotosList saved to: " + noteFileName);
            Toast.makeText(Activity_Note.this,
                    "PhotosList saved to: " + noteFileName, Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {}

    }


    void saveNote()  {



        // получаем данные из полей ввода
         name = etName.getText().toString();
         sku = etSku.getText().toString();
         category = etCategory.getText().toString();
         description = etDescription.getText().toString();

        int id = note.id;

        note.name = name;
        note.sku = sku;
        note.category = category;
        note.description = description;





        saveNoteToTxtFile();

        saveNoteToDB();

        readNotesFromDB();

        Intent intent = new Intent();
        intent.putExtra("note", note);
        Log.d("myLogs","note returned as result of NoteActivity");
        setResult(RESULT_OK, intent);
        finish();


    }

    void saveNoteToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

// создаем объект для данных
        ContentValues cv = new ContentValues();
        cv.put("name", note.name);
        cv.put("sku", note.sku);
        cv.put("category", note.category);
        cv.put("description", note.description);
        // вставляем запись и получаем ее ID
        long rowID = db.insert(tableName, null, cv);
        Log.d("myLogs", "row inserted, ID = " + rowID);

        dbHelper.close();
    }

    void readNotesFromDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Log.d("myLogs", "--- Rows in mytable: ---");
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c = db.query(tableName, null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int nameColIndex = c.getColumnIndex("name");
            int skuColIndex = c.getColumnIndex("sku");
            int categoryColIndex = c.getColumnIndex("category");
            int descriptionColIndex = c.getColumnIndex("description");

            do {
                // получаем значения по номерам столбцов и пишем все в лог
                Log.d("myLogs",
                        "name = " + c.getInt(nameColIndex) +
                                ", sku = " + c.getString(skuColIndex) +
                                ", category = " + c.getString(categoryColIndex) +
                                ", description = " + c.getString(descriptionColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        } else
            Log.d("myLogs", "0 rows");
        c.close();

        dbHelper.close();
    }

    void deleteAllRowsFromDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        Log.d("myLogs", "--- Clear mytable: ---");
        // удаляем все записи
        int clearCount = db.delete("mytable", null, null);
        Log.d("myLogs", "deleted rows count = " + clearCount);

        dbHelper.close();
    }

    void saveNoteToTxtFile() {



        String outputString = "NoteID = " + note.id + "\n" + System.getProperty("line.separator")
                + "NoteNAME = " + note.name + "\n" + System.getProperty("line.separator")
                + "NoteSKU = " + note.sku + "\n" + System.getProperty("line.separator")
                + "NoteCATEGORY = " + note.category +  "\n" + System.getProperty("line.separator")
                + "NoteDESCRIPTION = " + note.description +  "\n" + System.getProperty("line.separator");



        Log.d("myLogs","preparing to generateNoteDescriptionFileName ");
        Toast.makeText(Activity_Note.this,
                "preparing to generateNoteDescriptionFileName ", Toast.LENGTH_SHORT).show();


        String noteFileName = generateNoteDescriptionFileName(note.sku);

        Log.d("myLogs","generateNoteDescriptionFileName: " + noteFileName);
        Toast.makeText(Activity_Note.this,
                "generateNoteDescriptionFileName: " + noteFileName, Toast.LENGTH_SHORT).show();

        try {
            String fileName = noteFileName.substring(8);
            Log.d("myLogs","fileName: " + fileName);
            Toast.makeText(Activity_Note.this,
                    "fileName: " + fileName, Toast.LENGTH_SHORT).show();


            OutputStream os = new FileOutputStream(fileName);
            os.write(outputString.getBytes());
            os.close();
            Log.d("myLogs","Note saved to: " + noteFileName);
            Toast.makeText(Activity_Note.this,
                    "Note saved to: " + noteFileName, Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {}
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNote();
    }


    /* @Override
    protected void onDestroy() {
        saveNote();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        saveNote();
        super.onStop();

    }*/

    String generateNoteDescriptionFileName(String id) {
        // Проверяем доступность SD карты
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        // Проверяем и создаем директорию
        File path = new File(Environment.getExternalStorageDirectory(),
                Activity_Note.programDirectoryName + File.separator + Activity_Notes_List.notepadName + File.separator + Activity_Notes_List.noteIndex);
        if (! path.exists()){
            if (! path.mkdirs()){
                return null;
            }
        }

        // Создаем имя файла
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File newFile = new File(path.getPath() + File.separator /* + id + "_" + timeStamp*/ + "_note_description" + ".txt");

        return Uri.fromFile(newFile).toString();
    }

    String generatePhotosListFileName() {
        // Проверяем доступность SD карты
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        // Проверяем и создаем директорию
        File path = new File(Environment.getExternalStorageDirectory(), Activity_Note.programDirectoryName +
                File.separator + Activity_Notes_List.notepadName + File.separator + Activity_Notes_List.noteIndex);
        if (! path.exists()){
            if (! path.mkdirs()){
                return null;
            }
        }

        // Создаем имя файла
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File newFile = new File(path.getPath() + File.separator + Activity_Notes_List.noteIndex + "_photosList" + ".txt");

        return Uri.fromFile(newFile).toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
           /*case (R.id.buttonSaveNote):
                saveNote();
                break;
            case (R.id.buttonTakePicture):
                Log.d("myLog","calling TakePicture()");
                takePicture();
                break;*/
        }
    }

}



