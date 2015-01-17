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
public class DailyWeatherViewHolder extends RecyclerView.ViewHolder{
    @InjectView(R.id.dayLabel)
    TextView mDayLabel;
    @InjectView(R.id.iconImageView)
    ImageView mIconImageView;
    @InjectView(R.id.degreeLabel)
    TextView mDegreeLabel;

    private Context mContext;

    public DailyWeatherViewHolder(Context context, View view) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = context;
    }

    public void setDayLabel(String dayOfWeek) {
        mDayLabel.setText(dayOfWeek);
    }

    public void setDegreeLabel(String text) {
        mDegreeLabel.setText(text);
    }

    public void setIconImageView(int iconId) {
        Drawable drawable = mContext.getResources().getDrawable(iconId);
        mIconImageView.setImageDrawable(drawable);
    }
}
