package com.example.k.photonotepad_2016_01_10;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class Notepad implements Parcelable {



    public static final String NOTEPADS_LIST_FILE_NAME = "notePadsList.fil";
    public static final String APP_PREFERENCES_LAST_NOTEPAD_ID = "last_notepad_id";
    public static final String STRING_SEPARATOR_IN_FILE = "_-_";
    public static final int PARSING_ARRAY_STRING_LENGTH = 4;

    public static final String APP_PREFERENCES_NOTEPADS_LIST = "notepadsList";

    public static final String NOTEPAD_PREF_KEY = "NotePad";

    public static ArrayList<Notepad> notePadsList= new ArrayList<>();

    static SharedPreferences mSettings;

    static int lastId = Activity_Notes_List.lastId;

    int id;
    String name;
    long dateCreated;
    long dateModified;

    public Notepad(int id, String name, long dateCreated, long dateModified) {
        this.id = id;
        this.name = name;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    public Notepad() {

        mSettings = Activity_Notes_List.mSettings;

        if (mSettings.contains(APP_PREFERENCES_LAST_NOTEPAD_ID)) {
            // Получаем число из настроек
            lastId = mSettings.getInt(APP_PREFERENCES_LAST_NOTEPAD_ID, 0);
        }

        id = ++lastId;

        notepadCreateDir(id);
    }

    void notepadCreateDir(int id) {
        File path = new File (Environment.getExternalStorageDirectory(), Activity_Note.programDirectoryName +
                File.separator + "notepad" +  id);
        if (! path.exists()){
            if (!path.mkdirs()) {
                Log.d("myLogs", "couldn't create " + path);
                return;
            }
            Log.d("myLogs", "created " + path);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(Activity_Notes_List.APP_PREFERENCES_LAST_NOTEPAD_ID, id);
            editor.apply();

        }

    }

    static void saveNotepads(ArrayList<Notepad> incomingNotepadsList){
        saveNotePadsToFile(incomingNotepadsList);
    }



    static ArrayList<Notepad> loadNotepads(){
        return readNotepadsListFromFile();
    }



    public static ArrayList<Notepad> parseReadFromFileNotepadsListString(String incoming) {
        ArrayList<Notepad> notepadsListParsedFromString = new ArrayList<>();

        String[] stringArray = incoming.split(System.getProperty("line.separator"));
        Log.d ("myLogs", " number of strings parsed from notePadsListFile = " + stringArray.length);

        for (String string:stringArray) {
            String[] array = string.split(STRING_SEPARATOR_IN_FILE);
            Log.d("myLogs", "array of strings, parsed from one string in notepadsListFile length = " + array.length);
            if (array.length == PARSING_ARRAY_STRING_LENGTH) {


                Notepad notePad = new Notepad(Integer.parseInt(array[0].substring( array[0].indexOf("=")+1 )),
                        array[1].substring(array[1].indexOf("=")+1),
                        Long.parseLong(array[2].substring(array[2].indexOf("=")+1)),
                        Long.parseLong(array[3].substring(array[3].indexOf("=")+1)));
                notepadsListParsedFromString.add(notePad);
                Log.d("myLogs"," one more notepad parsed from string. Notepad id = " + notePad.id);
            }
        }


        return notepadsListParsedFromString;
    }

    static void saveNotePadsToFile(ArrayList<Notepad> incomingNotepadsList) {

        try {

            //FileWriter writer = new FileWriter(generateNotePadsFileAndGetFileName(), false);


            OutputStream os = new FileOutputStream(generateNotePadsFileAndGetFileName().substring(8));


            for (Notepad notePad:incomingNotepadsList) {
                String notePadString = "id=" + notePad.id + STRING_SEPARATOR_IN_FILE +
                        "name=" + notePad.name + STRING_SEPARATOR_IN_FILE +
                        "dateCreated=" + notePad.dateCreated + STRING_SEPARATOR_IN_FILE +
                        "dateModified=" + notePad.dateModified + STRING_SEPARATOR_IN_FILE
                        + System.getProperty("line.separator")
                        ;


                os.write(notePadString.getBytes());

            }


            os.close();
        } catch (IOException e) {
            Log.d("myLogs", "Exception when writing notePadsList to file");
            e.printStackTrace();}
    }

    static String generateNotePadsFileAndGetFileName() {
        // Проверяем доступность SD карты
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        // Проверяем и создаем директорию
        File path = new File(Environment.getExternalStorageDirectory(), Activity_Note.programDirectoryName );
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return null;
            }
        }


        // Создаем имя файла
        File newFile = new File(path.getPath() + File.separator + NOTEPADS_LIST_FILE_NAME);

        // создаем файл
        if (!newFile.exists())
            try {
                if (newFile.createNewFile()) {
                    Log.d("myLogs", newFile.getName() + " has been created");
                } else {
                    Log.d("myLogs", newFile.getName() + " already exists");
                }
            } catch (IOException e) {e.printStackTrace();}
        Log.d("myLogs", "notePadsList filename generated " + Uri.fromFile(newFile).toString());

        return Uri.fromFile(newFile).toString();

    }

    static ArrayList<Notepad> readNotepadsListFromFile() {
        File f = new File(generateNotePadsFileAndGetFileName().substring(8));

        String readFromFileNotepadsListString = null;
        try
        {
            FileReader reader = new FileReader(f);

            char[] buffer = new char[(int)f.length()];
            // считаем файл полностью
            reader.read(buffer);
            readFromFileNotepadsListString = new String(buffer);
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }


        if (readFromFileNotepadsListString != null)
            return parseReadFromFileNotepadsListString(readFromFileNotepadsListString);


            // TODO: добавить проверку на null
        else return null;
    }



    static void deleteAlNotepads() {
        // TODO Доработать, чтобы удалялись только данные блокнотов, а не файлы в корневой паке приложения - вроде сделано, проверить
        //deleteFileOrDirRecursievely(new File(Environment.getExternalStorageDirectory(), NoteActivity.programDirectoryName));

        File rootDir = new File(Environment.getExternalStorageDirectory(), Activity_Note.programDirectoryName);

        for (File file:rootDir.listFiles()) {
            if ( (file.isDirectory()) || ( file.getName().contains(Notepad.NOTEPADS_LIST_FILE_NAME) ) )
                Helper_File.deleteFileOrDirRecursievely(file);
        }

        Notepad.lastId = 0;

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_NOTEPADS_LIST, 0);
        editor.putInt(APP_PREFERENCES_LAST_NOTEPAD_ID, 0);
        editor.apply();

       Activity_Notepad_Select.notePadsList = new ArrayList<>();
      // TODO: проврить нужен ли здесь adapterInit();
    }

   static void notePadsListInit() {

        notePadsList = Notepad.readNotepadsListFromFile();

    }


    public static final Parcelable.Creator<Notepad> CREATOR = new Parcelable.Creator<Notepad>() {
        // распаковываем объект из Parcel
        public Notepad createFromParcel(Parcel in) {
            Log.d("myLogs", "create_NotePad_FromParcel");
            return new Notepad(in);
        }

        @Override
        public Notepad[] newArray(int size) {
            return new Notepad[size];
        }
    };

    Notepad (Parcel parcel) {
        Log.d("myLogs","Note(Parcel parcel)");
        id = parcel.readInt();
        name = parcel.readString();
        // TODO разобраться как прочитать массив из PARCEL readArray
        // photosList = parcel.readArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d("myLog", "writingNoteToParcel");
        dest.writeInt(id);
        dest.writeString(name);

        //TODO writeDate

    }

}
