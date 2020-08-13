package com.nowcent.volleyball.utils;

import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.spider.Spider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import javax.script.ScriptException;

public class Utils {
    private static final String PRE_URL = "https://lhwtt.ydmap.cn";
//        private static final String PRE_URL = "http://192.168.31.226:8080";
    private static final String SAVE_URI = "/v2/sportPlatform/save.do";
    private static final String ORDER_URI = "/v2/sportPlatformUser/queryByDealPlatform.do";
    private static final String METHOD = "post";

    private static int MIN_HOUR = 9;
    private static int MAX_HOUR = 22;


    public static String getSaveData(Date startDate, Date endDate, int teamId) throws ParseException {
//        Date startDate = DateFormat.getDateTimeInstance().parse("2013-01-01 15:00:00");
//        Date endDate = DateFormat.getDateTimeInstance().parse("2013-01-01 16:00:00");
//        Date currentDate = DateFormat.getDateInstance().parse(LocalDate.now().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date currentDate = sdf.parse(sdf.format(new Date()));

        int index = startDate.getHours() + 6 - 15;

        assert currentDate != null;
        return "{\n" +
                "  " +
                "\"sportPlatformList\":" +
                " \n" +
                "  [\n" +
                "    {\"salesId\":\"101765\",\n" +
                "      \"platformParentId\":0,\n" +
                "      \"platformId\":104128,\n" +
                "      \"orderDate\":" + currentDate.getTime() + ",\n" +
                "      \"startTime\":" + startDate.getTime() +",\n" +
                "      \"endTime\":" + endDate.getTime() + ",\n" +
                "      \"colspan\":1,\n" +
                "      \"rowspan\":1,\n" +
                "      \"colIndex\":0,\n" +
                "      \"rowIndex\":" + index + ",\n" +
                "      \"selectPubStudy\":false,\n" +
                "      \"sportTeamId\":" + teamId + ",\n" +
                "      \"sportTeamColor\":100,\n" +
                "      \"fightDeclaration\":null,\n" +
                "      \"fightMobile\":null}\n" +
                "  ],\n" +
                "  \"sportPlatformUserList\":[]}";

    }

    public static String getOrderData(Date startDate, Date endDate) throws ParseException {
//        Date currentDate = DateFormat.getDateInstance().parse(LocalDate.now().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date currentDate = sdf.parse(sdf.format(new Date()));
        int index = startDate.getHours() + 6 - 15;

        assert currentDate != null;
        return "{\n" +
                "  " +
                "\"dealPlatformList\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"salesId\":\"101765\",\n" +
                "      \"platformParentId\":0,\n" +
                "      \"platformId\":104128,\n" +
                "      \"orderDate\":" + currentDate.getTime() + ",\n" +
                "      \"startTime\":" + startDate.getTime() + ",\n" +
                "      \"endTime\":" + endDate.getTime() + ",\n" +
                "      \"colspan\":1,\n" +
                "      \"rowspan\":1,\n" +
                "      \"colIndex\":0,\n" +
                "      \"rowIndex\":" + index + ",\n" +
                "      \"selectPubStudy\":false\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public static boolean isNumber(String text){
        return Pattern.compile("[0-9]*").matcher(text).matches();
    }

    public static int getTeamId(String token, Date startDate, Date endDate) throws ParseException, NoSuchMethodException, ScriptException, IOException, JSONException {
        //初始化蜘蛛类
        Spider spider = Spider.getInstance();
        String data = getOrderData(startDate, endDate);
        String result = spider.run(PRE_URL, ORDER_URI, METHOD, data, token);

        //解析数据
        JSONObject jsonObject = new JSONObject(result);

        int code = jsonObject.getInt("code");
        if(code != 200){
            return -1;
        }
        JSONObject dataObject = jsonObject.getJSONObject("data");
        JSONArray sportTeamList = (JSONArray) dataObject.getJSONArray("sportTeamList");
        JSONObject sportTeamListObject = sportTeamList.getJSONObject(0);
        return sportTeamListObject.getInt("sportTeamId");
    }

    public static String save(String token, Date startDate, Date endDate, int teamId) throws ParseException, NoSuchMethodException, ScriptException, IOException, JSONException {
        //初始化蜘蛛类
        Spider spider = Spider.getInstance();
        String data = getSaveData(startDate, endDate, teamId);
        System.out.println(data);
        String result;
        try{
            result = spider.run(PRE_URL, SAVE_URI, METHOD, data, token);
        }catch (Exception e){
            e.printStackTrace();
            return "栈溢出错误";
        }

        //解析数据
        JSONObject jsonObject = new JSONObject(result);

        int code = jsonObject.getInt("code");
        if(code == 200){
            return null;
        }
        else{
            return result;
        }

    }

    public static String getTimeString(){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
        return dateTimeFormatter.format(LocalDateTime.now());
    }

    public static String getTimeString(Date date){
        if(date == null){
            return null;
        }
        SimpleDateFormat sdf =new SimpleDateFormat("MM-dd HH:mm:ss" );
        Date d= new Date();
        return sdf.format(date);
    }


    public static boolean isValidInput(EditText fromEditText, EditText toEditText, Runnable invalidDate){
        if(fromEditText.getText() == null || toEditText.getText() == null || fromEditText.getText().toString().isEmpty() ||
                toEditText.getText().toString().isEmpty()){
            invalidDate.run();;
            return false;
        }

        //检查文本框的信息是否正确（日期）
        if(!isNumber(fromEditText.getText().toString()) || !isNumber(toEditText.getText().toString())){
            invalidDate.run();
            return false;
        }

        int startHour = Integer.parseInt(fromEditText.getText().toString());
        int endHour = Integer.parseInt(toEditText.getText().toString());

        if(startHour < MIN_HOUR ||
                startHour > MAX_HOUR ||
                endHour < MIN_HOUR ||
                endHour > MAX_HOUR ||
                endHour - startHour != 1){
            invalidDate.run();
            return false;
        }
        return true;
    }

    public static void conveyToCorrectDate(EditText fromEditText, EditText toEditText, BiConsumer<Date, Date> consumer){
        final Date startDate;
        final Date endDate;
        final String pattern = "yyyy-MM-dd HH:mm:ss";
        final SimpleDateFormat dateFormat=new SimpleDateFormat(pattern);
        try {
//            startDate = DateFormat.getDateTimeInstance().parse("2013-01-01 " + startDateEditText.getText().toString() + ":00:00");
//            endDate = DateFormat.getDateTimeInstance().parse("2013-01-01 " + endDateEditText.getText().toString() + ":00:00");
            startDate = dateFormat.parse("2013-01-01 " + Integer.parseInt(fromEditText.getText().toString()) + ":00:00");
            endDate = dateFormat.parse("2013-01-01 " + Integer.parseInt(toEditText.getText().toString()) + ":00:00");
            consumer.accept(startDate, endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
