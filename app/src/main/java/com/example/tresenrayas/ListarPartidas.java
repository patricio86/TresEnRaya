package com.example.tresenrayas;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListarPartidas extends AppCompatActivity {

    private ListView lv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_partidas);

        lv1 = (ListView)findViewById(R.id.lv1);

        ArrayList<String> ranking = new ArrayList<>();

        AdminSQLiteOpenHelper helperBBDD = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = helperBBDD.getReadableDatabase();

        Cursor fila = db.rawQuery("select * from partidas", null);
        if(fila.moveToFirst()){
            do{
                ranking.add(fila.getString(0) + " - " + fila.getString(1) + " - " +
                        fila.getString(2) + " - " + fila.getString(3) + " - " + fila.getString(4));
            }while(fila.moveToNext());
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ranking);
        lv1.setAdapter(adapter);

        db.close();

    }
}