package com.example.foodordering.weather.dao;

import android.content.Context;

import java.util.List;

import com.example.foodordering.weather.dao.greendao.Alarms;
import com.example.foodordering.weather.dao.greendao.AlarmsDao;
import com.example.foodordering.weather.dao.greendao.Aqi;
import com.example.foodordering.weather.dao.greendao.AqiDao;
import com.example.foodordering.weather.dao.greendao.HourForeCast;
import com.example.foodordering.weather.dao.greendao.HourForeCastDao;
import com.example.foodordering.weather.dao.greendao.RealWeather;
import com.example.foodordering.weather.dao.greendao.RealWeatherDao;
import com.example.foodordering.weather.dao.greendao.UseArea;
import com.example.foodordering.weather.dao.greendao.UseAreaDao;
import com.example.foodordering.weather.dao.greendao.WeekForeCast;
import com.example.foodordering.weather.dao.greendao.WeekForeCastDao;
import com.example.foodordering.weather.dao.greendao.Zhishu;
import com.example.foodordering.weather.dao.greendao.ZhishuDao;

/**
 * Created by xch on 2018/3/10.
 */
public class WeatherDao extends BaseLocalDao {

    public void insertRealweather(Context context, RealWeather realWeather) {
        RealWeatherDao realWeatherDao = getDaoSession(context).getRealWeatherDao();
        realWeatherDao.insert(realWeather);
    }

    public void delRealweatherByAreaId(Context context, String areaId) {
        RealWeatherDao realWeatherDao = getDaoSession(context).getRealWeatherDao();
        realWeatherDao.queryBuilder().where(RealWeatherDao.Properties.Areaid.eq(areaId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public RealWeather queryRealweatherByAreaId(Context context, String areaId) {
        RealWeatherDao realWeatherDao = getDaoSession(context).getRealWeatherDao();
        return getSingleData(realWeatherDao.queryBuilder().where(RealWeatherDao.Properties.Areaid.eq(areaId)).list());
    }

    public void insertWeekForeCast(Context context, List<WeekForeCast> weekForeCasts) {
        WeekForeCastDao weekForeCastDao = getDaoSession(context).getWeekForeCastDao();
        weekForeCastDao.insertInTx(weekForeCasts);
    }

    public List<WeekForeCast> queryWeekForeCastByAreaId(Context context, String areaId) {
        WeekForeCastDao weekForeCastDao = getDaoSession(context).getWeekForeCastDao();
        return weekForeCastDao.queryBuilder().where(WeekForeCastDao.Properties.Areaid.eq(areaId)).list();
    }

    public void delWeekForeCastByAreaId(Context context, String areaId) {
        WeekForeCastDao weekForeCastDao = getDaoSession(context).getWeekForeCastDao();
        weekForeCastDao.queryBuilder().where(WeekForeCastDao.Properties.Areaid.eq(areaId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void insertHourForeCast(Context context, List<HourForeCast> hourForeCasts) {
        HourForeCastDao hourForeCastDao = getDaoSession(context).getHourForeCastDao();
        hourForeCastDao.insertInTx(hourForeCasts);
    }

    public void delHourForeCastByAreaId(Context context, String areaId) {
        HourForeCastDao hourForeCastDao = getDaoSession(context).getHourForeCastDao();
        hourForeCastDao.queryBuilder().where(HourForeCastDao.Properties.Areaid.eq(areaId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public List<HourForeCast> queryHourForeCastByAreaId(Context context, String areaId) {
        HourForeCastDao hourForeCastDao = getDaoSession(context).getHourForeCastDao();
        return hourForeCastDao.queryBuilder().where(HourForeCastDao.Properties.Areaid.eq(areaId)).list();
    }


    public void delAqiByAreaId(Context context, String areaId) {
        AqiDao aqiDao = getDaoSession(context).getAqiDao();
        aqiDao.queryBuilder().where(AqiDao.Properties.Areaid.eq(areaId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void insertAqi(Context context, Aqi aqi) {
        AqiDao aqiDao = getDaoSession(context).getAqiDao();
        aqiDao.insert(aqi);
    }

    public Aqi queryAqiByAreaId(Context context, String areaId) {
        AqiDao aqiDao = getDaoSession(context).getAqiDao();
        return getSingleData(aqiDao.queryBuilder().where(AqiDao.Properties.Areaid.eq(areaId)).list());
    }

    public void delZhishuByAreaId(Context context, String areaId) {
        ZhishuDao zhishuDao = getDaoSession(context).getZhishuDao();
        zhishuDao.queryBuilder().where(ZhishuDao.Properties.Areaid.eq(areaId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void insertZhishu(Context context, List<Zhishu> zhishus) {
        ZhishuDao zhishuDao = getDaoSession(context).getZhishuDao();
        zhishuDao.insertInTx(zhishus);
    }

    public List<Zhishu> queryZhishuByAreaId(Context context, String areaId) {
        ZhishuDao zhishuDao = getDaoSession(context).getZhishuDao();
        return zhishuDao.queryBuilder().where(ZhishuDao.Properties.Areaid.eq(areaId)).list();
    }


    public void insetNewUseArea(Context context, UseArea useArea) {
        UseAreaDao useAreaDao = getDaoSession(context).getUseAreaDao();
        if (useArea.getMain()) {
            List<UseArea> list = useAreaDao.queryBuilder().list();
            for (UseArea useArea1 : list) {
                useArea1.setMain(false);
            }
            if (list.size() > 0)
                useAreaDao.updateInTx(list);
        }
        useAreaDao.insert(useArea);
    }

    public UseArea queryMainUseArea(Context context) {
        UseAreaDao useAreaDao = getDaoSession(context).getUseAreaDao();
        return getSingleData(useAreaDao.queryBuilder().where(UseAreaDao.Properties.Main.eq(true)).list());
    }

    public void insertNewAlarm(Context context, Alarms alarms) {
        AlarmsDao alarmsDao = getDaoSession(context).getAlarmsDao();
        alarmsDao.queryBuilder().where(AlarmsDao.Properties.Areaid.eq(alarms.getAreaid())).buildDelete().executeDeleteWithoutDetachingEntities();
        alarmsDao.insert(alarms);
    }

    public Alarms getAlarmByArea(Context context, String areaid) {
        AlarmsDao alarmsDao = getDaoSession(context).getAlarmsDao();
        return (Alarms) getSingleData(alarmsDao.queryBuilder().where(AlarmsDao.Properties.Areaid.eq(areaid)).list());
    }

}
