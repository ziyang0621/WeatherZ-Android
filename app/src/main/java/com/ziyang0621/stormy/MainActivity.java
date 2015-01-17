package com.ziyang0621.stormy;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity implements LocationProvider.LocationCallback {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Weather mCurrentWeather;
    private List<Weather> mDailyWeatherList;
    private LocationProvider mLocaitonProvider;
    private Location mLocation;
    private DailyWeatherAdapter mDailyWeatherAdapter;

    @InjectView(R.id.timeLabel)
    TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue)
    TextView mHumidityValue;
    @InjectView(R.id.precipValue)
    TextView mPrecipValue;
    @InjectView(R.id.summaryLabel)
    TextView mSummaryLabel;
    @InjectView(R.id.iconImageView)
    ImageView mIconImageView;
    @InjectView(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @InjectView(R.id.locationLabel)
    TextView mLocationLabel;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.relativeLayout)
    RelativeLayout mRelativeLayout;
    @InjectView(R.id.imageView)
    ImageView mImageView;
    @InjectView(R.id.dailyList)
    RecyclerView mDailyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mRelativeLayout.getBackground().setAlpha(130);

        LinearLayoutManager layoutManager  = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDailyList.setLayoutManager(layoutManager);

        mDailyWeatherAdapter = new DailyWeatherAdapter(MainActivity.this, new ArrayList<Weather>());
        mDailyList.setAdapter(mDailyWeatherAdapter);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mDailyList.getLayoutParams();
        params.height = WeatherConstants.NUMBER_OF_FUTURE_DAYS * getResources().getDimensionPixelSize(R.dimen.dailyweather_list_item_row_height);
        mDailyList.setLayoutParams(params);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mRelativeLayout.getLayoutParams().height = mRelativeLayout.getLayoutParams().height + mDailyList.getLayoutParams().height;

        mProgressBar.setVisibility(View.INVISIBLE);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocation != null) {
                    getForecast(mLocation.getLatitude(), mLocation.getLongitude());
                }
            }
        });

        mLocaitonProvider = new LocationProvider(this, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get tracker.
        Tracker t = ((WeatherApplication) this.getApplication()).getTracker(
                WeatherApplication.TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName(TAG);

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        mLocaitonProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocaitonProvider.disconnect();
    }

    public void handleNewLocation(Location location) {
        Log.d(TAG, "handle new location " + location.toString());
        mLocation = location;
        getForecast(mLocation.getLatitude(), mLocation.getLongitude());
    }

    private void getCityImage(double latitude, double longitude) {
        String apiKey = getResources().getString(R.string.flickr_api_key);

        String searchUrl = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key="
                + apiKey + "&text=weather&accuracy=11&safe_search=1&lat=" + latitude
                + "&lon=" + longitude + "&in_gallery=true&per_page=1&format=json&nojsoncallback=1";

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(searchUrl)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    try {
                        JSONObject data = new JSONObject(jsonData);
                        JSONObject photos = data.getJSONObject("photos");
                        JSONArray photo = photos.getJSONArray("photo");

                        if (photo.length() > 0) {
                            JSONObject photoDetail = photo.getJSONObject(0);
                            String farmId = photoDetail.getString("farm");
                            String serverId = photoDetail.getString("server");
                            String photoId = photoDetail.getString("id");
                            String secret = photoDetail.getString("secret");

                            final String imageUrl = "https://farm" + farmId + ".staticflickr.com/" + serverId + "/"
                                    + photoId + "_" + secret + ".jpg";

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso.with(MainActivity.this).load(imageUrl).placeholder(R.drawable.weather_place_image).into(mImageView);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
    }


    private void getForecast(double latitude, double longitude) {
        String apiKey = getResources().getString(R.string.forecast_api_key);

        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            mDailyWeatherList = getDailyWeather(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                    mDailyWeatherAdapter.loadNewData(mDailyWeatherList);
                                    getCityImage(mLocation.getLatitude(), mLocation.getLongitude());
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());
        mLocationLabel.setText(mCurrentWeather.getPlaceName());

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private List<Weather> getDailyWeather(String jsonData) throws JSONException {
        List<Weather> weatherList = new ArrayList<Weather>();

        JSONObject forecast = new JSONObject(jsonData);

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray dailyData = daily.getJSONArray("data");

        Log.d(TAG, "daily data: " + dailyData.toString());

        for (int i = 0; i < WeatherConstants.NUMBER_OF_FUTURE_DAYS; i++) {
            JSONObject object = dailyData.getJSONObject(i);

            Weather weather = new Weather();
            weather.setTemperatureMin(object.getDouble("temperatureMin"));
            weather.setTemperatureMax(object.getDouble("temperatureMax"));
            weather.setIcon(object.getString("icon"));
            weather.setTime(object.getLong("time"));

            weatherList.add(weather);
        }

        return weatherList;
    }

    private Weather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        Weather currentWeather = new Weather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                currentWeather.setPlaceName(address.getLocality());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

}
