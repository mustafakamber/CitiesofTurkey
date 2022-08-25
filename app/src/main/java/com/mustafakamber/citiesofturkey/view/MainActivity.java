package com.mustafakamber.citiesofturkey.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mustafakamber.citiesofturkey.R;
import com.mustafakamber.citiesofturkey.adapter.CityAdapter;
import com.mustafakamber.citiesofturkey.databinding.ActivityMainBinding;
import com.mustafakamber.citiesofturkey.model.City;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<City> cityArrayList;
    CityAdapter cityAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        cityArrayList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityAdapter = new CityAdapter(cityArrayList);
        binding.recyclerView.setAdapter(cityAdapter);

        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void getData(){
        try{
            SQLiteDatabase database = this.openOrCreateDatabase("Cities",MODE_PRIVATE,null);

            Cursor cursor = database.rawQuery("SELECT * FROM cities", null);
            int nameIx = cursor.getColumnIndex("cityName");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                City city = new City(name,id);
                cityArrayList.add(city);

            }

            cityAdapter.notifyDataSetChanged();

            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }


    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_city){
            Intent intent = new Intent(MainActivity.this,DetailsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}