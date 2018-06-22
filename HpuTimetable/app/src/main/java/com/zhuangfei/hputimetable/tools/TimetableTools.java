package com.zhuangfei.hputimetable.tools;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ListView;

import com.google.gson.Gson;
import com.zhuangfei.expandedittext.ExpandEditText;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Liu ZhuangFei on 2018/3/8.
 */

public class TimetableTools {

    public static List<Integer> getWeekList(String weeksString) {
        List<Integer> weekList = new ArrayList<>();
        if (weeksString == null || weeksString.length() == 0) return weekList;

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
}
