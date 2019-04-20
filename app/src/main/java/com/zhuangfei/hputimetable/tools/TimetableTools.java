package com.zhuangfei.hputimetable.tools;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/3/8.
 */

public class TimetableTools {

    /**
     * 获取当前周
     * @param context
     * @return
     */
    public static int getCurWeek(Context context){
        if(context==null) return 1;
        String startTime=ShareTools.getString(context, ShareConstants.STRING_START_TIME,null);
        if(startTime==null){
            ShareTools.putString(context,ShareConstants.STRING_START_TIME,getStartSchoolTime(1));
            return 1;
        }
        int r=ScheduleSupport.timeTransfrom(startTime);
//        int isFirstSunday=ShareTools.getInt(context,"isFirstSunday",0);
//        Calendar calendar=Calendar.getInstance();
//        calendar.setTime(new Date());
//        if(isFirstSunday==1&&calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
//            r-=1;
//        }
        if(r>25) return 25;
        if(r<=0) return 1;
        return r;
    }

    /**
     * 获取开学时间
     * @param curWeek
     * @return
     */
    public static String getStartSchoolTime(int curWeek){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long end=getThisWeekMonday(new Date()).getTime();
        int day=(curWeek-1)*7;
        long seconds=day*24*3600;
        long start=end-seconds*1000;
        return sdf.format(new Date(start))+" 00:00:00";
    }

    public static Date getThisWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    /**
     * 获取今天周几
     * @return
     */
    public static int getThisWeek() {
        //获取周几，1->7
        Calendar now = Calendar.getInstance();
        //一周第一天是否为星期天
        boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
        int weekDay = now.get(Calendar.DAY_OF_WEEK);
        //若一周第一天为星期天，则-1
        if (isFirstSunday) {
            weekDay = weekDay - 1;
            if (weekDay == 0) {
                weekDay = 7;
            }
        }
        return weekDay;
    }

    public static List<Integer> getWeekListThrowExcept(String weeksString) {
        List<Integer> weekList = new ArrayList<>();
        if (weeksString == null || weeksString.length() == 0) return weekList;

        try{
            weeksString = weeksString.replaceAll("[^\\d\\-\\,]", "");
            if (weeksString.indexOf(",") != -1) {
                String[] arr = weeksString.split(",");
                for (int i = 0; i < arr.length; i++) {
                    weekList.addAll(getWeekList2(arr[i]));
                }
            } else {
                weekList.addAll(getWeekList2(weeksString));
            }
            return weekList;
        }catch (Exception e){
            return weekList;
        }
    }

    public static List<Integer> getWeekList(String weeksString) {
        List<Integer> weekList = new ArrayList<>();

        if (weeksString == null || weeksString.length() == 0) return weekList;
//兼容 全周有课这种情况
        if(weeksString.startsWith("全周")){
            for(int i=1;i<=25;i++){
                weekList.add(i);
            }
            return weekList;
        }
        try{
            weeksString = weeksString.replaceAll("[^\\d\\-\\,]", "");
            if (weeksString.indexOf(",") != -1) {
                String[] arr = weeksString.split(",");
                for (int i = 0; i < arr.length; i++) {
                    weekList.addAll(getWeekList2(arr[i]));
                }
            } else {
                weekList.addAll(getWeekList2(weeksString));
            }
            return weekList;
        }catch (Exception e){
            e.printStackTrace();
            return weekList;
        }
    }

    public static List<Integer> getWeekList2(String weeksString) {
        List<Integer> weekList = new ArrayList<>();
        int first = -1, end = -1, index = -1;
        if ((index = weeksString.indexOf("-")) != -1) {
            first = Integer.parseInt(weeksString.substring(0, index));
            end = Integer.parseInt(weeksString.substring(index + 1));
        } else {
            first = Integer.parseInt(weeksString);
            end = first;
        }

        for (int i = first; i <= end; i++)
            weekList.add(i);
        return weekList;
    }

    public static boolean saveTimetable(TimetableResultModel model, String timetableName) {
        Gson gson = new Gson();
        if (model == null || TextUtils.isEmpty(timetableName)) return false;
        List<TimetableModel> haveList = model.getHaveList();
        List<TimetableModel> notimeList = model.getNotimeList();
        if ((haveList == null && notimeList == null) || (haveList.size() == 0 && notimeList.size() == 0)) {
            return false;
        }
        FileTools.writeTimetable(timetableName, gson.toJson(model));
        return true;
    }

    public static TimetableResultModel getTimetable(String timetableName) {
        Gson gson = new Gson();
        if (TextUtils.isEmpty(timetableName)) return null;

        String value = FileTools.readTimetable(timetableName);
        try {
            TimetableResultModel resultModel = new Gson().fromJson(value, TimetableResultModel.class);
            return resultModel;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date getTargetDate(int curWeek,int targetWeek,int curDay,int targetDay){
        Calendar resultCalender=Calendar.getInstance();
        resultCalender.setTime(new Date());
        resultCalender.add(Calendar.DATE,(targetWeek-curWeek)*7+targetDay-curDay);
        return resultCalender.getTime();
    }

    public static boolean getTimeList(Context context,List<String> startTimeList,List<String> endTimeList){
        String time= ShareTools.getString(context,"schedule_time",null);
        if(time==null) return false;
        String[] timeArray=time.split("\\n");
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        if(timeArray==null){
            for(String item:timeArray){
                if(item==null||item.indexOf("-")==-1){
                    return false;
                }
                String[] lineArray=item.split("-");
                if(lineArray==null||lineArray.length!=2){
                    return false;
                }
                try {
                    Date date1=sdf.parse(lineArray[0]);
                    Date date2=sdf.parse(lineArray[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
                }
                startTimeList.add(lineArray[0]);
                endTimeList.add(lineArray[1]);
            }
        }
        return true;
    }
}
