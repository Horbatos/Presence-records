package com.example.pmfstevidencija;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Current_Attendance extends AppCompatActivity {

    String[] students;
    ListView simpleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current__attendance);
        students=(String[])getIntent().getSerializableExtra("Students");


        simpleList = findViewById(R.id.simpleListView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, R.layout.activity_listview, R.id.textView, students);
        simpleList.setAdapter(arrayAdapter);

    }
}
