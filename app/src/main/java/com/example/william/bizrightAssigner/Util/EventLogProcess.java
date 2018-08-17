package com.example.william.bizrightAssigner.Util;

import android.app.Activity;

import com.example.william.bizrightAssigner.Bean.EventLog;
import com.example.william.bizrightAssigner.Bean.SmallLocationPool;
import com.example.william.bizrightAssigner.Info.GlobalInfo;
import com.example.william.bizrightAssigner.SerOkHttpClient;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class EventLogProcess extends Activity {

    MediaType JSON =MediaType.parse("application/json; charset=utf-8");
    okhttp3.Request requestSmallData;
    Gson g = new Gson();
    String jsonString;


    public void addEventLog(EventLog eventLog,String token,OkHttpClient client){


        jsonString = g.toJson(eventLog);
        RequestBody body = RequestBody.create(JSON, jsonString);


        requestSmallData = new okhttp3.Request.Builder()
                .url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/EventLogs/PostEventLog")
                .header("Authorization", "Bearer " + token)
                .post(body)
                .build();
        okhttp3.Response response2 = null;

        try {
            response2 = client.newCall(requestSmallData).execute();
            String oj = response2.body().string();

            EventLog s = g.fromJson(oj, EventLog.class);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static String getTime(){
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        calendar.setTimeZone(tz);
        Date date=new Date(calendar.getTime().getTime());
        SimpleDateFormat format = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(format.format(date));
        return format.format(date);
    }

}
