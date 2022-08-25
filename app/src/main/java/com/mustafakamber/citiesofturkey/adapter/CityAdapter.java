package com.mustafakamber.citiesofturkey.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mustafakamber.citiesofturkey.databinding.RecyclerRowBinding;
import com.mustafakamber.citiesofturkey.model.City;
import com.mustafakamber.citiesofturkey.view.DetailsActivity;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityHolder>
{
 ArrayList<City> cityArrayList;

  public CityAdapter(ArrayList<City> cityArrayList){
      this.cityArrayList = cityArrayList;
  }

    @NonNull
    @Override
    public CityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new CityHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CityAdapter.CityHolder holder, int position) {
         holder.binding.recyclerViewTextView.setText(cityArrayList.get(position).name);
         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(holder.itemView.getContext(), DetailsActivity.class);
                 intent.putExtra("info","old");
                 intent.putExtra("cityId",cityArrayList.get(position).id);
                 holder.itemView.getContext().startActivity(intent);
             }
         });
    }

    @Override
    public int getItemCount() {
        return cityArrayList.size();
    }


    public class CityHolder extends RecyclerView.ViewHolder{
      private RecyclerRowBinding binding;

      public CityHolder(RecyclerRowBinding binding){
          super(binding.getRoot());
          this.binding = binding;
      }
  }
}
