package com.zhuangfei.hputimetable.event;

import com.zhuangfei.hputimetable.api.model.StationModel;

/**
 * Created by Liu ZhuangFei on 2019/2/13.
 */
public class ReloadStationEvent {
    private StationModel stationModel;

    public void setStationModel(StationModel stationModel) {
        this.stationModel = stationModel;
    }

    public StationModel getStationModel() {
        return stationModel;
    }
}
