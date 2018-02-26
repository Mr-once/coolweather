package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by 17773 on 2018/2/24.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Forecast forecast;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast>forecastList;
}
