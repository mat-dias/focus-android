package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TaskResponse {

    public String status;

    @SerializedName("total")
    public int total;

    @SerializedName("concluidas")
    public int concluidas;

    @SerializedName("today")
    public String today;

    @SerializedName("tasks")
    public List<TaskItem> tasks;

    public static class TaskItem {

        @SerializedName("task_id")
        public int taskId;

        @SerializedName("title")
        public String title;

        @SerializedName("priority")
        public String priority;

        @SerializedName("done")
        public boolean done;

        @SerializedName("tag")
        public String tag;

        // IDs necessários para marcar done no scheduling correto
        @SerializedName("scheduling_id")
        public Integer schedulingId; // null se ainda não foi marcada hoje

        @SerializedName("schedule_id")
        public Integer scheduleId;   // null se não tem schedule hoje ainda
    }
}