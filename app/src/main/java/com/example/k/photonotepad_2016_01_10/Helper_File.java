package com.example.k.photonotepad_2016_01_10;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Helper_File {

    public static void deleteFileOrDirRecursievely(File file)
    {
        if(!file.exists())
            return;
        if(file.isDirectory())
        {
            for(File f : file.listFiles())
                deleteFileOrDirRecursievely(f);
            file.delete();
        }
        else {
            file.delete();
        }
    }

    static void checkAndCreateProgramDir() {
        File path = new File (Environment.getExternalStorageDirectory(), Data.programDirectoryName);
        if (! path.exists()){
            if (!path.mkdirs()) {
                //   Toast.makeText(Activity_Notes_List.this, "unable to create program dir", Toast.LENGTH_SHORT).show();
                Log.d("myLogs","unable to create program dir");
            }
        }
    }

    static ArrayList<File> listFilesWithSubFolders(File dir) {
        ArrayList<File> files = new ArrayList<File>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                files.addAll(listFilesWithSubFolders(file));
            else
                files.add(file);
        }
        return files;
    }


}
