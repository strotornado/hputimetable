package com.zhuangfei.hputimetable.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Liu ZhuangFei on 2019/2/11.
 */
public class ImportTools {
    public static void showDialogOnApply(final Context context, final ScheduleName name) {
        if (name == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("你导入的数据已存储在多课表[" + name.getName() + "]下!\n是否直接设置为当前课表?")
                .setTitle("课表导入成功")
                .setCancelable(false)
                .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ToastTools.show(context,"设置为当前课表!");
                        ScheduleDao.applySchedule(context, name.getId());
                        EventBus.getDefault().post(new UpdateScheduleEvent());
                        BroadcastUtils.refreshAppWidget(context);
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("稍后设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ToastTools.show(context,"数据已保存在多课表中，未设置为当前课表!");
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.create().show();
    }

    public static void showDialogOnApply(final Activity context, final ScheduleName name, final boolean isFinish) {
        if (name == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("你导入的数据已存储在多课表[" + name.getName() + "]下!\n是否直接设置为当前课表?")
                .setTitle("课表导入成功")
                .setCancelable(false)
                .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ToastTools.show(context,"导入课表成功!");
                        ScheduleDao.applySchedule(context, name.getId());
                        EventBus.getDefault().post(new UpdateScheduleEvent());
                        BroadcastUtils.refreshAppWidget(context);
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                        if(isFinish){
                            context.finish();
                        }
                    }
                })
                .setNegativeButton("稍后设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                        if(isFinish){
                            context.finish();
                        }
                    }
                });
        builder.create().show();
    }
}
