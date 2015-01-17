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
public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherViewHolder> {
    private List<Weather> mWeatherList;
    private Context mContext;

    public DailyWeatherAdapter(Context context, List<Weather> weatherList) {
        mContext = context;
        mWeatherList = weatherList;
    }

    @Override
    public DailyWeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dailyweather_list_item, parent, false);
        DailyWeatherViewHolder dailyWeatherViewHolder = new DailyWeatherViewHolder(mContext, view);
        return dailyWeatherViewHolder;
    }

    @Override
    public void onBindViewHolder(DailyWeatherViewHolder holder, int position) {
        Weather weather = mWeatherList.get(position);
        holder.setDayLabel(weather.getDayOfWeek());
        holder.setIconImageView(weather.getIconId());
        holder.setDegreeLabel(weather.getTemperatureMin() + " / " + weather.getTemperatureMax());
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
