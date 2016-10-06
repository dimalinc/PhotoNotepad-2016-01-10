package com.example.k.photonotepad_2016_01_10;

import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class Helper_Adapter {

   static ListAdapter adapter;
    static ListView listView;
    public static ArrayList<Notepad> notePadsList= new ArrayList<>();





    static ArrayList<HashMap<String, Object>> createDataArrayList() {

        // Упаковываем данные

        ArrayList<HashMap<String, Object>> data = new ArrayList<>(
                notePadsList.size());
        HashMap<String, Object> map;
        for (int i = 0; i < notePadsList.size(); i++) {
            map = new HashMap<>();
            map.put("name", notePadsList.get(i).name);

            data.add(map);
        }

        return data;
    }
}
