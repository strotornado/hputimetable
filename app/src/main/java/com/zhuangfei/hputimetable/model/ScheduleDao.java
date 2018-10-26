package com.zhuangfei.hputimetable.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;

import com.tencent.bugly.crashreport.BuglyLog;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by Liu ZhuangFei on 2018/8/17.
 */
public class ScheduleDao {
    private static final String TAG = "ScheduleDao";

    public static void changeStatus(Context context,boolean isNeedUpdate){
        int v=isNeedUpdate==true?1:0;
        ShareTools.putInt(context,"schedule_is_need_update",1);
    }

    public static boolean isNeedUpdate(Context context){
        int v= ShareTools.getInt(context,"schedule_is_need_update",0);
        if(v==1) return true;
        return false;
    }

    public static List<TimetableModel> getAllWithScheduleId(int id) {
        Log.d(TAG, "getAllWithScheduleId: "+id);
        ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
        if(scheduleName==null) return null;
        return scheduleName.getModels();
    }

    public static int getApplyScheduleId(Context context) {
        return ShareTools.getInt(context, ShareConstants.INT_SCHEDULE_NAME_ID, -1);
    }

    public static int getScheduleCount(List<TimetableModel> models, ScheduleName nameBean) {
        int count = 0;
        for (TimetableModel model : models) {
            if (model.getScheduleName() != null && model.getScheduleName().getId() == nameBean.getId()) {
                count++;
            }
        }
        return count;
    }

    public static ScheduleName saveSuperShareLessons(List<SuperLesson> lessons){
        if(lessons==null)return null;
        ScheduleName newName=new ScheduleName();
        SimpleDateFormat sdf=new SimpleDateFormat("来自超表的导入-HHmm");
        newName.setName(sdf.format(new Date()));
        newName.setTime(System.currentTimeMillis());
        newName.save();

        saveLessons(lessons,newName);
        return newName;
    }

    public static ScheduleName saveSuperLessons(List<SuperLesson> lessons){
        if(lessons==null)return null;
        ScheduleName newName=new ScheduleName();
        SimpleDateFormat sdf=new SimpleDateFormat("来自超表的分享-HHmm");
        newName.setName(sdf.format(new Date()));
        newName.setTime(System.currentTimeMillis());
        newName.save();
        saveLessons(lessons,newName);
        return newName;
    }

    public static void saveLessons(List<SuperLesson> lessons,ScheduleName newName){
        List<TimetableModel> modelList=new ArrayList<>();
        for(SuperLesson lesson:lessons){
            TimetableModel model=new TimetableModel();
            model.setName(lesson.getName());
            model.setDay(lesson.getDay());
            model.setRoom(lesson.getLocale());
            model.setStart(lesson.getSectionstart());
            model.setStep(lesson.getSectionend()-lesson.getSectionstart()+1);
            model.setTeacher(lesson.getTeacher());
            model.setWeekList(splitToWeekList(lesson.getPeriod()));
            model.setScheduleName(newName);
            modelList.add(model);
        }
        DataSupport.saveAll(modelList);
    }

    private static List<Integer> splitToWeekList(String period){
        BuglyLog.d("ScheduleDao",period);
        List<Integer> weekList=new ArrayList<>();
        if(TextUtils.isEmpty(period)) return weekList;

        String[] arr=period.trim().split(" ");
        for(int i=0;i<arr.length;i++){
            weekList.add(Integer.valueOf(arr[i]));
        }
        return weekList;
    }

    public static void applySchedule(Context context,int id){
        ShareTools.put(context, "course_update", 1);
        ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, id);
    }
}
