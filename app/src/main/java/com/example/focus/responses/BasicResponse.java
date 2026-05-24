package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;

public class BasicResponse {
    public String status;
    public String msg;

    @SerializedName("xp_ganho")      public int     xpGanho;
    @SerializedName("streak")        public int     streak;
    @SerializedName("scheduling_id") public Integer schedulingId;
    @SerializedName("schedule_id")   public Integer scheduleId;
}
