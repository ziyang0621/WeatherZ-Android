package com.ziyang0621.stormy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by ziyang0621 on 1/16/15.
 */
public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherViewHolder> {
    private List<Weather> mWeatherList;
    private Context mContext;

    public HourlyWeatherAdapter(Context context, List<Weather> weatherList) {
        mContext = context;
        mWeatherList = weatherList;
    }

    @Override
    public HourlyWeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourlyweather_list_item, parent, false);
        HourlyWeatherViewHolder hourlyWeatherViewHolder = new HourlyWeatherViewHolder(mContext, view);
        return hourlyWeatherViewHolder;
    }

    @Override
    public void onBindViewHolder(HourlyWeatherViewHolder holder, int position) {
        Weather weather = mWeatherList.get(position);
        holder.setHourLabel(weather.getHour());
        holder.setIconImageView(weather.getIconId());
        holder.setDegreeLabel(""+weather.getTemperature());
    }

    @Override
    public int getItemCount() {
        return mWeatherList.size();
    }

    public void loadNewData(List<Weather> weatherList) {
        mWeatherList = weatherList;
        notifyDataSetChanged();
    }

}
