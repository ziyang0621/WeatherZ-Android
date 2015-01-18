package com.ziyang0621.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ziyang0621 on 1/16/15.
 */
public class HourlyWeatherViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.hourLabel)
    TextView mHourLabel;
    @InjectView(R.id.iconImageView)
    ImageView mIconImageView;
    @InjectView(R.id.degreeLabel)
    TextView mDegreeLabel;

    private Context mContext;

    public HourlyWeatherViewHolder(Context context, View view) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = context;
    }

    public void setHourLabel(String hour) {
        mHourLabel.setText(hour);
    }

    public void setDegreeLabel(String text) {
        mDegreeLabel.setText(text);
    }

    public void setIconImageView(int iconId) {
        Drawable drawable = mContext.getResources().getDrawable(iconId);
        mIconImageView.setImageDrawable(drawable);
    }
}
