package com.example.foodordering.weather.manager;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.foodordering.weather.dao.CityDao;
import com.example.foodordering.weather.dao.WeatherDao;
import com.example.foodordering.weather.dao.bean.City;
import com.example.foodordering.weather.dao.greendao.Alarms;
import com.example.foodordering.weather.dao.greendao.Aqi;
import com.example.foodordering.weather.dao.greendao.HourForeCast;
import com.example.foodordering.weather.dao.greendao.RealWeather;
import com.example.foodordering.weather.dao.greendao.UseArea;
import com.example.foodordering.weather.dao.greendao.WeekForeCast;
import com.example.foodordering.weather.dao.greendao.Zhishu;
import com.example.foodordering.weather.util.Constant;
import com.example.foodordering.weather.util.DecodeUtil;
import com.example.foodordering.weather.util.HttpRetriever;
import com.example.foodordering.weather.util.NetworkUtil;

/**
 * Created by xch on 2018/3/10.
 */
public class WeatherManager extends BaseManager {
    private WeatherDao weatherDao = new WeatherDao();
    private CityDao cityDao = new CityDao(_context);

    public WeatherManager(Context context) {
        super(context);
    }

    /**
     * @param areaID
     * @param handler
     */
    public void refreshWeather(final String areaID, final boolean useLocal, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (areaID) {
                    WeatherInfo weatherInfo = new WeatherInfo();
                    RealWeather realWeather = weatherDao.queryRealweatherByAreaId(_context, areaID);
                    Aqi aqi = weatherDao.queryAqiByAreaId(_context, areaID);
                    List<WeekForeCast> weekForeCasts = weatherDao.queryWeekForeCastByAreaId(_context, areaID);
//                    if (weekForeCasts != null && weekForeCasts.size() > 1)
//                        weekForeCasts.remove(weekForeCasts.size() - 1);
                    List<HourForeCast> hourForeCasts = weatherDao.queryHourForeCastByAreaId(_context, areaID);
                    List<Zhishu> zhishus = weatherDao.queryZhishuByAreaId(_context, areaID);
                    Alarms alarms = weatherDao.getAlarmByArea(_context, areaID);
                    if (realWeather == null || aqi == null || weekForeCasts.size() == 0 || hourForeCasts.size() == 0 || zhishus.size() == 0 || !useLocal)
                        if (NetworkUtil.checkNetwork(_context))
                            loadWeatherFromNet(areaID, handler);
                        else
                            sendEmptyMessage(handler, Constant.MSG_ERROR);
                    else {
                        weatherInfo.setRealWeather(realWeather);
                        weatherInfo.setWeekForeCasts(weekForeCasts);
                        weatherInfo.setHourForeCasts(hourForeCasts);
                        weatherInfo.setAqi(aqi);
                        weatherInfo.setZhishu(zhishus);
                        weatherInfo.setAlarms(alarms);
                        sendMessage(handler, weatherInfo);
                    }
                }
            }
        }).start();
    }

    /**
     * @param areaID
     * @param handler
     */
    public void loadWeatherFromNet(final String areaID, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                City city = cityDao.getCityByWeatherID(areaID);
                if (city == null) {
                    sendEmptyMessage(handler, Constant.MSG_ERROR);
                    return;
                }
                String url = String.format(Constant.URL_WEATHER_2345, city.getAreaId());
                String urlFlyme = String.format(Constant.URL_FORECAST_FLYME, city.getWeatherId());
                try {
                    String response = DecodeUtil.decodeResponse(HttpRetriever.retrieve(url));
                    String flymeresponse = HttpRetriever.retrieve(urlFlyme);

                    Log.d("flymeresponse", urlFlyme);

                    if (TextUtils.isEmpty(response) || TextUtils.isEmpty(flymeresponse)) {
                        sendEmptyMessage(handler, Constant.MSG_ERROR);
                        return;
                    }

                    JSONObject weather2345 = JSON.parseObject(response);
                    JSONObject weatherflyme = JSON.parseObject(flymeresponse);

                    //天气预警
                    Alarms alarm = null;
                    JSONArray alarms = weatherflyme.getJSONArray("alarms");
                    if (alarms.size() > 0) {
                        JSONObject jsonObject = alarms.getJSONObject(0);
                        alarm = new Alarms();
                        alarm.setAlarmContent(jsonObject.getString("alarmContent"));
                        alarm.setAlarmId(jsonObject.getString("alarmId"));
                        alarm.setAlarmLevelNo(jsonObject.getString("alarmLevelNo"));
                        alarm.setAlarmLevelNoDesc(jsonObject.getString("alarmLevelNoDesc"));
                        alarm.setAlarmType(jsonObject.getString("alarmType"));
                        alarm.setAlarmTypeDesc(jsonObject.getString("alarmTypeDesc"));
                        alarm.setPublishTime(jsonObject.getString("publishTime"));
                        alarm.setAreaName(city.getAreaName());
                        alarm.setAreaid(city.getWeatherId());
                        weatherDao.insertNewAlarm(_context, alarm);
                    }

                    //小时天气
                    List<HourForeCast> hourForeCasts = new ArrayList<HourForeCast>();
                    JSONArray hourForecastJson = weatherflyme.getJSONObject("weatherDetailsInfo").getJSONArray("weather24HoursDetailsInfos");

                    weatherDao.delHourForeCastByAreaId(_context, areaID);
                    for (int i = 0; i < hourForecastJson.size(); i++) {

                        HourForeCast hourForeCast = new HourForeCast();
                        JSONObject jsonObject = hourForecastJson.getJSONObject(i);

                        hourForeCast.setAreaid(areaID);
                        String time = jsonObject.getString("startTime").split(" ")[1];
                        hourForeCast.setHour(time.split(":")[0] + ":00");
                        hourForeCast.setWeatherCondition(jsonObject.getString("weather"));
                        hourForeCast.setTemp(jsonObject.getInteger("lowerestTemperature"));
                        hourForeCasts.add(hourForeCast);
                    }
                    weatherDao.insertHourForeCast(_context, hourForeCasts);

                    //实时天气
                    weatherDao.delRealweatherByAreaId(_context, areaID);
                    JSONObject realtime = weatherflyme.getJSONObject("realtime");
                    RealWeather realWeather = new RealWeather();
                    realWeather.setAreaName(city.getAreaName());
                    realWeather.setLastUpdate(new Date(System.currentTimeMillis()));
                    realWeather.setAreaid(areaID);
                    realWeather.setSunrise(weather2345.getJSONObject("sunrise").getString("todayRise"));
                    realWeather.setSundown(weather2345.getJSONObject("sunrise").getString("todaySet"));
                    JSONObject sk = weather2345.getJSONObject("sk");
                    String shidu = sk.getString("humidity").replace("%", "");
                    realWeather.setShidu(Integer.parseInt(shidu));
                    realWeather.setFx(sk.getString("windDirection"));
                    realWeather.setFj(realtime.getString("wS"));
                    realWeather.setTemp(sk.getInteger("sk_temp"));
                    realWeather.setFeeltemp(realtime.getInteger("sendibleTemp"));
                    realWeather.setWeatherCondition(realtime.getString("weather"));
                    weatherDao.insertRealweather(_context, realWeather);
                    //realWeather.setWeatherCondition(weather2345.getString("sk").g);

                    //aqi
                    weatherDao.delAqiByAreaId(_context, areaID);
                    Aqi aqido = new Aqi();
                    aqido.setAreaid(areaID);
                    JSONObject aqiObj = weather2345.getJSONObject("aqi");
                    if (aqiObj == null || (aqiObj != null && TextUtils.isEmpty(aqiObj.getString("PM25"))) || "--".equals(aqiObj)) {
                        aqiObj = weatherflyme.getJSONObject("pm25");
                        aqido.setAqi(aqiObj.getInteger("aqi"));
                        aqido.setSo2(aqiObj.getInteger("so2"));
                        aqido.setNo2(aqiObj.getInteger("no2"));
                        aqido.setPm2_5(aqiObj.getInteger("pm25"));
                        aqido.setPm10(aqiObj.getInteger("pm10"));
                        aqido.setQuality(aqiObj.getString("quality"));
                    } else {
                        try {
                            aqido.setAqi(aqiObj.getInteger("AQI"));
                            aqido.setSo2(aqiObj.getInteger("SO2"));
                            aqido.setNo2(aqiObj.getInteger("NO2"));
                            aqido.setPm2_5(aqiObj.getInteger("PM25"));
                            aqido.setPm10(aqiObj.getInteger("PM10"));
                            aqido.setQuality(aqiObj.getString("aqiLevelString"));
                        } catch (Exception e) {
                            aqiObj = weatherflyme.getJSONObject("pm25");
                            aqido.setAqi(aqiObj.getInteger("aqi"));
                            aqido.setSo2(aqiObj.getInteger("so2"));
                            aqido.setNo2(aqiObj.getInteger("no2"));
                            aqido.setPm2_5(aqiObj.getInteger("pm25"));
                            aqido.setPm10(aqiObj.getInteger("pm10"));
                            aqido.setQuality(aqiObj.getString("quality"));
                        }

                    }
                    weatherDao.insertAqi(_context, aqido);

                    //一周预报
                    JSONArray weekForecastJson = weather2345.getJSONArray("days7");
                    weatherDao.delWeekForeCastByAreaId(_context, areaID);
                    List<WeekForeCast> weekForeCasts = new ArrayList<WeekForeCast>();
                    for (int i = 0; i < weekForecastJson.size(); i++) {
                        JSONObject jsonObject = weekForecastJson.getJSONObject(i);
                        WeekForeCast weekForeCast = new WeekForeCast();
                        weekForeCast.setAreaid(areaID);
                        weekForeCast.setFx(jsonObject.getString("dayWindDirection"));
                        weekForeCast.setFj(jsonObject.getString("dayWindLevel"));
                        String tmpMin = jsonObject.getString("wholeTemp").split("～")[0];
                        String tmpMax = jsonObject.getString("wholeTemp").split("～")[1];
                        weekForeCast.setTempL(Integer.parseInt(tmpMin));
                        weekForeCast.setTempH(Integer.parseInt(tmpMax));
                        weekForeCast.setRainPerCent(0);
                        weekForeCast.setWeatherConditionStart(jsonObject.getString("dayWea"));
                        weekForeCast.setWeatherConditionEnd(jsonObject.getString("nightWea"));
                        weekForeCast.setWeatherDate(new Date(jsonObject.getLong("time") * 1000));

                        weekForeCasts.add(weekForeCast);
                    }
                    weatherDao.insertWeekForeCast(_context, weekForeCasts);

                    //指数
                    weatherDao.delZhishuByAreaId(_context, areaID);
                    List<Zhishu> zhishus = new ArrayList<Zhishu>();
//                    JSONArray jsonArrayzss = weather2345.getJSONArray("zs");
//                    for (int i = 0; i < jsonArrayzss.size(); i++) {
//                        Zhishu zhishu = new Zhishu();
//                        JSONObject jsonObject = jsonArrayzss.getJSONObject(i);
//                        zhishu.setAreaid(areaID);
//                        zhishu.setName(jsonObject.getString("name"));
//                        zhishu.setLevel(jsonObject.getString("level"));
//                        zhishu.setText(jsonObject.getString("text"));
//                        zhishu.setDetail(jsonObject.getString("detail"));
//                        zhishus.add(zhishu);
//                    }

                    JSONArray jsonArrayzss = weatherflyme.getJSONArray("indexes");
                    for (int i = 0; i < jsonArrayzss.size(); i++) {
                        Zhishu zhishu = new Zhishu();
                        JSONObject jsonObject = jsonArrayzss.getJSONObject(i);
                        zhishu.setAreaid(areaID);
                        zhishu.setName(jsonObject.getString("name"));
                        if (Constant.ZHISHU.get(zhishu.getName()) == null)
                            continue;

                        zhishu.setLevel(jsonObject.getString("level"));
                        zhishu.setText(jsonObject.getString("alias"));
                        zhishu.setDetail(jsonObject.getString("content"));
                        zhishus.add(zhishu);
                    }
                    weatherDao.insertZhishu(_context, zhishus);


                    WeatherInfo weatherInfo = new WeatherInfo();
                    weatherInfo.setRealWeather(realWeather);
                    weatherInfo.setWeekForeCasts(weekForeCasts);
                    weatherInfo.setHourForeCasts(hourForeCasts);
                    weatherInfo.setAqi(aqido);
                    weatherInfo.setZhishu(zhishus);
                    weatherInfo.setAlarms(alarm);
                    sendMessage(handler, weatherInfo);

                    //Log.d("respose", response);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendEmptyMessage(handler, Constant.MSG_ERROR);
                }
            }
        }).start();
    }

    public void insertNewUseArea(City city, boolean main) {
        UseArea useArea = new UseArea();
        useArea.setAreaid(city.getWeatherId());
        useArea.setAreaid2345(city.getAreaId());
        useArea.setAreaName(city.getAreaName());
        useArea.setMain(main);
        weatherDao.insetNewUseArea(_context, useArea);
    }

    public UseArea queryMianUseArea() {
        return weatherDao.queryMainUseArea(_context);
    }

}
