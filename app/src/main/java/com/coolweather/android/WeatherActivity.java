package com.coolweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpadateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private LinearLayout forecastLayout;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){//判断系统版本号
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(//改变系统UI显示，这里表示活动布局会显示在状态栏上面
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏设为透明色
        }
        setContentView(R.layout.activity_weather);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpadateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);//文件存储
        String weatherString=preferences.getString("weather",null);//在本地缓存中读取天气数据；
        if (weatherString!=null){//如果有缓存，直接解析
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);//处理实体类的数据并展示；
        }else {//无缓存时去服务器查询；
            String weatherId=getIntent().getStringExtra("weather_id");//获得传递进来的数据;
            weatherLayout.setVisibility(View.INVISIBLE);//隐藏界面
            requestWeather(weatherId);//根据ID请求城市天气信息；
        }
        String bingPic=preferences.getString("bing_pic",null);
        if (bingPic!=null){//如果缓存中有图片，直接加载。
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();//加载必应每日一图
        }
    }
    public void requestWeather(final String weatherId){//根据ID请求城市天气信息；
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//网络请求回调函数；
            @Override
            public void onFailure(Call call, IOException e) {//网络请求失败处理结果
                e.printStackTrace();
                runOnUiThread(new Runnable() {//回主线程
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "天气获取失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {//数据返回处理
                final String responsetext=response.body().string();//数据实例化
                final Weather weather=Utility.handleWeatherResponse(responsetext);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();//缓存在文件中
                            editor.putString("weather",responsetext);//文件名为weather
                            editor.apply();;
                            showWeatherInfo(weather);//处理实体类的数据并展示；
                        }else {
                            Toast.makeText(WeatherActivity.this, "天气获取失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }

    private void loadBingPic() {//加载必应每日一图
        String requestBingPic="http:guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    public void showWeatherInfo(Weather weather){//处理实体类的数据并展示；
        String cityName=weather.basic.cityName;
        String update=weather.basic.update.updateTime;
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpadateTime.setText(update);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){//加载子布局；
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度:"+weather.suggestion.comfort.info;
        String carWash="洗车指数:"+weather.suggestion.carWash.info;
        String sport="运动建议:"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }
}
