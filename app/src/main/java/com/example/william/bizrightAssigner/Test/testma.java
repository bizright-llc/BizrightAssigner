package com.example.william.bizrightAssigner.Test;

import com.example.william.bizrightAssigner.Bean.EventLog;
import com.example.william.bizrightAssigner.Bean.SmallLocationPool;
import com.example.william.bizrightAssigner.Bean.SmallPutLocation;
import com.example.william.bizrightAssigner.Util.EventLogProcess;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class testma {
    public static void main(String[] args) {
//        Gson g=new Gson();
//        SmallLocationPool smallLocationPool=new SmallLocationPool();
//        SmallPutLocation smallPutLocation=new SmallPutLocation();
//        smallPutLocation.setId(10);
//
//        smallLocationPool.setId(10);
//        smallLocationPool.setSku("sfsdfsf");
//        smallLocationPool.setAssigned(22);
//
//        smallPutLocation.setSmallLocationPool(smallLocationPool);
//        String jsonString = g.toJson(smallPutLocation);
//        System.out.println(jsonString);


       // java.util.Date date = new java.util.Date(Calendar.getInstance().getTime().getTime());

        EventLogProcess.getTime();

    }

}
