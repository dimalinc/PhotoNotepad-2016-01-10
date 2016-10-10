package com.example.k.photonotepad_2016_01_10;

import android.content.Intent;
import android.media.Image;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Note implements Parcelable {

    public static final String STRING_SEPARATOR_IN_FILE = "_-_";
    public static final int PARSING_ARRAY_STRING_LENGTH = 4;

    int id;
    String sku;
    String name;
    String category;
    String description;

    ArrayList<Image> noteImagesList = new ArrayList<>();

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Note(int id) {
        this.id = id;
    }

    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
        // распаковываем объект из Parcel
        public Note createFromParcel(Parcel in) {
            Log.d("myLogs", "createFromParcel");
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    private Note (Parcel parcel) {
        Log.d("myLogs","Note(Parcel parcel)");
        id = parcel.readInt();
        sku = parcel.readString();
        name = parcel.readString();
        category = parcel.readString();
        description = parcel.readString();
        // TODO разобраться как прочитать массив из PARCEL readArray
        // photosList = parcel.readArray();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d("myLog", "writingNoteToParcel");
        dest.writeInt(id);
        dest.writeString(sku);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeString(description);
//        dest.writeArray(photosList.toArray());

        //TODO writeDate

    }

   static ArrayList<Note> readNotesFromFileSystem() {

        File path = new File (Environment.getExternalStorageDirectory(), Data.programDirectoryName +
                File.separator + Activity_Notes_List.notepad.name );

        return readNotesFromPath(path);


    }

   static ArrayList<Note> readNotesFromPath(File file) {
        if(!file.exists())
            return null;
        if(file.isDirectory())
        {
            for(File notePath : file.listFiles()) {

                // парсим имя заметки и создаем заметку с таким именем
                Note note = new Note( Integer.parseInt(notePath.getName()) );

                for (File descriptionNoteFile: notePath.listFiles() ) {
                    if (descriptionNoteFile.getName().contains("description"))
                        noteDescriptionParse(note, descriptionNoteFile);
                }

                //

               Activity_Notes_List.noteList.add(note);
            }
        }
        else {

        }

        return Activity_Notes_List.noteList;
    }

    static void noteDescriptionParse(Note note, File file) {

        String readFromFileNoteString = null;
        try
        {
            FileReader reader = new FileReader(file);

            char[] buffer = new char[(int)file.length()];
            // считаем файл полностью
            reader.read(buffer);
            readFromFileNoteString = new String(buffer);
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }


        if (readFromFileNoteString != null)
            parseReadFromFileNoteString(note, readFromFileNoteString);
    }

    static void parseReadFromFileNoteString(Note note, String readFromFileNoteString) {

        String[] stringArray = readFromFileNoteString.split("\n" + System.getProperty("line.separator"));
        Log.d ("myLogs", " number of strings parsed from FileNoteString = " + stringArray.length);

        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i].startsWith("NoteID = "))
                note.id = Integer.parseInt(stringArray[i].substring(9));

            if (stringArray[i].startsWith("NoteNAME = "))
                note.name = stringArray[i].substring(11);

            if (stringArray[i].startsWith("NoteSKU = "))
                note.sku = stringArray[i].substring(10);

            if (stringArray[i].startsWith("NoteCATEGORY = "))
                note.category = stringArray[i].substring(15);

            if (stringArray[i].startsWith("NoteDESCRIPTION = "))
                note.description = stringArray[i].substring(17);

        }
    }


}
