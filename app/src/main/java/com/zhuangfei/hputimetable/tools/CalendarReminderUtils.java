package com.zhuangfei.hputimetable.tools;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.CalendarContract;
import android.text.TextUtils;

import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.listener.OnExportProgressListener;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class CalendarReminderUtils {

    private static String CALENDER_URL = "content://com.android.calendar/calendars";
    private static String CALENDER_EVENT_URL = "content://com.android.calendar/events";
    private static String CALENDER_REMINDER_URL = "content://com.android.calendar/reminders";

    private static String CALENDARS_NAME = "boohee";
    private static String CALENDARS_ACCOUNT_NAME = "BOOHEE@boohee.com";
    private static String CALENDARS_ACCOUNT_TYPE = "com.android.boohee";
    private static String CALENDARS_DISPLAY_NAME = "BOOHEE账户";

    /**
     * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
     * 获取账户成功返回账户id，否则返回-1
     */
    private static int checkAndAddCalendarAccount(Context context) {
        try{
            int oldId = checkCalendarAccount(context);
            if( oldId >= 0 ){
                return oldId;
            }else{
                long addId = addCalendarAccount(context);
                if (addId >= 0) {
                    return checkCalendarAccount(context);
                } else {
                    return -1;
                }
            }
        }catch (Exception e){
            ToastTools.show(context,"请务必授予日历权限，错误信息:"+e.getMessage());
        }
        return -1;
    }

    /**
     * 检查是否存在现有账户，存在则返回账户id，否则返回-1
     */
    private static int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
        try {
            if (userCursor == null) { //查询返回空值
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) { //存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    /**
     * 返回所有的日历账户
     */
    public static List<Map<String,String>> listCalendarAccount(Context context) {
        List<Map<String,String>> result=new ArrayList<>();
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
        try {
            if (userCursor!=null&&userCursor.getCount() > 0) {
                for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()){
                    String account=userCursor.getString(userCursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME));
                    String name=userCursor.getString(userCursor.getColumnIndex(CalendarContract.Calendars.DEFAULT_SORT_ORDER));
                    int calId=userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
                    Map<String,String> map=new HashMap<>();
                    map.put("name",name);
                    map.put("account",account);
                    map.put("calId",String.valueOf(calId));
                    result.add(map);
                }
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
        if(result.isEmpty()){
            long addId = addCalendarAccount(context);
            if (addId >= 0) {
                Map<String,String> map=new HashMap<>();
                map.put("name","新增账户");
                map.put("account","新增账户");
                map.put("calId",String.valueOf(addId));
                result.add(map);
            }
        }
        return result;
    }

    /**
     * 添加日历账户，账户创建成功则返回账户id，否则返回-1
     */
    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = Uri.parse(CALENDER_URL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }

    public static void addScheduleToCalender(Context context,int calId, Schedule model,
                                             boolean qinglv,
                                             List<String> startTimeList,
                                             List<String> endTimeList, int curWeek, OnExportProgressListener listener){
        if(model==null||model.getWeekList()==null||startTimeList==null
                ||endTimeList==null||model.getWeekList().size()==0
                ||model.getStep()==0||model.getStart()==0
                ||(model.getStart()-1)>=startTimeList.size()
                ||(model.getStart()+model.getStep()-2)>=endTimeList.size()) {
            if(listener!=null){
                listener.onError("数据为空|时间没有覆盖全部课程");
            }
            return;
        }
        Set<Integer> weekSet= new HashSet<>();
        weekSet.addAll(model.getWeekList());
        String startTime=startTimeList.get(model.getStart()-1);
        String endTime=endTimeList.get(model.getStart()+model.getStep()-2);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int thisDay=TimetableTools.getThisWeek();
        int nowIndex=0;
        String prefix="";
        if(qinglv) prefix="[情侣]";
//        if(listener!=null&&weekSet.size()==0){
//            listener.onError(model.getName()+" weekList size is 0");
//        }

        for(Integer i:weekSet){
            nowIndex++;
            Date date=TimetableTools.getTargetDate(curWeek,i,thisDay,model.getDay());
            String dateString=sdf.format(date);
            try {
                Date realStartDate=sdf2.parse(dateString+" "+startTime);
                Date realEndDate=sdf2.parse(dateString+" "+endTime);
                addCalendarEvent(context,calId,
                        prefix+" "+model.getName(),
                        "第"+i+"周 | "+model.getStart()+"-"+(model.getStart()+model.getStep()-1)+"节上 | "
                                +model.getTeacher()+" | "
                                +model.getWeekList().toString()+" | "
                                +"add by 怪兽课表",
                        model.getRoom(),
                        realStartDate,realEndDate,listener);
                if(listener!=null){
                    listener.onProgress(weekSet.size(),nowIndex);
                }
            } catch (ParseException e) {
                if(listener!=null){
                    listener.onError(e.getMessage());
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加日历事件
     */
    public static void addCalendarEvent(Context context,int calId, String title, String description, String location, Date startDate, Date endDate,OnExportProgressListener listener) {
        if (context == null) {
            if(listener!=null){
                listener.onError("context is null");
            }
            return;
        }
//        int calId = checkAndAddCalendarAccount(context); //获取日历账户的id
        if (calId < 0) { //获取账户id失败直接返回，添加日历事件失败
            ToastTools.show(context,"添加日历账户失败，可能没有授予日历权限或者没有日历账户");
            if(listener!=null){
                listener.onError("添加日历账户失败，可能没有授予日历权限或者没有日历账户");
            }
            return;
        }

        //添加日历事件
//        Calendar mCalendar = Calendar.getInstance();
//        mCalendar.setTimeInMillis(reminderTime);//设置开始时间
//        long start = mCalendar.getTime().getTime();
//        mCalendar.setTimeInMillis(start + 10 * 60 * 1000);//设置终止时间，开始时间加10分钟
//        long end = mCalendar.getTime().getTime();
        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", description);
        event.put("calendar_id", calId); //插入账户的id
        event.put(CalendarContract.Events.EVENT_LOCATION,location);
        event.put(CalendarContract.Events.DTSTART, startDate.getTime());
        event.put(CalendarContract.Events.DTEND, endDate.getTime());
        event.put(CalendarContract.Events.HAS_ALARM, 0);//设置有闹钟提醒,1：有提醒
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");//这个是时区，必须有
        Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDER_EVENT_URL), event); //添加事件
        if (newEvent == null) { //添加日历事件失败直接返回
            if(listener!=null){
                listener.onError("添加日历日程失败");
            }
            return;
        }

        //事件提醒的设定
//        ContentValues values = new ContentValues();
//        values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
//        values.put(CalendarContract.Reminders.MINUTES, previousDate * 24 * 60);// 提前previousDate天有提醒
//        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
//        Uri uri = context.getContentResolver().insert(Uri.parse(CALENDER_REMINDER_URL), values);
//        if(uri == null) { //添加事件提醒失败直接返回
//            return;
//        }
    }

    /**
     * 删除日历事件
     */
    public static void deleteCalendarEvent(Context context,String title) {
        if (context == null) {
            return;
        }
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDER_EVENT_URL), null, null, null, null);
        try {
            if (eventCursor == null) { //查询返回空值
                return;
            }
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDER_EVENT_URL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) { //事件删除失败
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }

    /**
     * 删除日历事件
     */
    public static void deleteCalendarSchedule(final Context context, Handler handler,OnExportProgressListener listener) {
        if (context == null||handler==null) {
            return;
        }
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDER_EVENT_URL), null, null, null, null);
        try {
            if (eventCursor == null) { //查询返回空值
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastTools.show(context,"删除失败：未查询到事件!");
                    }
                });
                return;
            }
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                int i=0;
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    i++;
                    String description = eventCursor.getString(eventCursor.getColumnIndex("description"));
                    if (!TextUtils.isEmpty(description) && description.endsWith("add by 怪兽课表")) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDER_EVENT_URL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if(listener!=null){
                            listener.onProgress(eventCursor.getCount(),i);
                        }
                        if (rows == -1) { //事件删除失败
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ToastTools.show(context,"删除失败!");
                                }
                            });
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastTools.show(context,"删除成功，课程已经清理完毕!");
            }
        });
    }
}
