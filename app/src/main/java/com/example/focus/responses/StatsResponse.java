package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatsResponse {

    public String status;

    @SerializedName("streak")
    public int streak;

    @SerializedName("xp")
    public int xp;

    @SerializedName("total_tasks")
    public int totalTasks;

    @SerializedName("total_concluidas")
    public int totalConcluidas;

    @SerializedName("hoje_total")
    public int hojeTotal;

    @SerializedName("hoje_concluidas")
    public int hojeConcluidas;

    @SerializedName("semana")
    public List<Integer> semana; // índice 0=Dom, 1=Seg ... 6=Sab

    @SerializedName("tags")
    public List<TagItem> tags;

    public static class TagItem {
        @SerializedName("tag")
        public String tag;

        @SerializedName("qtd")
        public int qtd;

        @SerializedName("percent")
        public int percent;
    }
}
