package com.zhuangfei.hputimetable.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.ValuePair;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Liu ZhuangFei on 2018/11/7.
 */
public class UpdateTools {

    public static void checkUpdate(final Context context,boolean isUserTodo) throws Exception{
        if(context==null) return;
        String id="e98b58875e902084a93a1daeae1ccbf7";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String s = sdf.format(new Date()) + VersionTools.getVersionName();
        String store = ShareTools.getString(context, "app_update_info", null);
        int isIgnoreUpdate = ShareTools.getInt(context, "isIgnoreUpdate", 0);
        if (isUserTodo||(isIgnoreUpdate == 0 && (store == null || !store.equals(s)))) {
            TimetableRequest.getValue(context, id, new Callback<ObjResult<ValuePair>>() {
                @Override
                public void onResponse(Call<ObjResult<ValuePair>> call, Response<ObjResult<ValuePair>> response) {
                    ObjResult<ValuePair> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            ValuePair pair = result.getData();
                            if (pair != null) {
                                String value = pair.getValue();
                                String[] vals = value.split("#");
                                if (vals.length >= 3) {
                                    int v = Integer.parseInt(vals[0]);
                                    int isIgnoreUpdate = ShareTools.getInt(context, "isIgnoreUpdate", 0);
                                    if (isIgnoreUpdate == 0 && v > VersionTools.getVersionNumber()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                                .setTitle("发现新版本-v" + vals[1])
                                                .setMessage("你可以在 工具箱->自动检查更新 中关闭提醒!\n\n更新日志:\n" + vals[2])
                                                .setPositiveButton("去看看", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent intent = new Intent();
                                                        intent.setAction("android.intent.action.VIEW");
                                                        intent.setData(Uri.parse("https://www.coolapk.com/apk/com.zhuangfei.hputimetable"));
                                                        context.startActivity(intent);
                                                        if (dialogInterface != null) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("明天提醒", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        ShareTools.putString(context, "app_update_info", s);
                                                    }
                                                });
                                        builder.create().show();
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
                }
            });
        }
    }
}
