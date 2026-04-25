package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TaskResponse {

    public String status;

    @SerializedName("total")
    public int total;

    @SerializedName("concluidas")
    public int concluidas;

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
    }
}
