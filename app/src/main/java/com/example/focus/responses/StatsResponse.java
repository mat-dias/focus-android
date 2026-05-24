package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class StatsResponse {

    public String status;

    @SerializedName("streak")             public int streak;
    @SerializedName("xp")                 public int xp;
    @SerializedName("nivel")              public int nivel;
    @SerializedName("xp_progresso")       public int xpProgresso;
    @SerializedName("xp_proximo")         public int xpProximo;
    @SerializedName("hoje_total")         public int hojeTotal;
    @SerializedName("hoje_concluidas")    public int hojeConcluidas;
    @SerializedName("xp_hoje")            public int xpHoje;
    @SerializedName("taxa_hoje")          public int taxaHoje;
    @SerializedName("melhor_streak")      public int melhorStreak;
    @SerializedName("dias_ativos")        public int diasAtivos;
    @SerializedName("dias_semana")        public int diasSemana;
    @SerializedName("taxa_conclusao")     public int taxaConclusao;
    @SerializedName("concluidas_periodo") public int concluidasPeriodo;
    @SerializedName("agendadas_periodo")  public int agendadasPeriodo;
    @SerializedName("barras")             public List<BarraItem>      barras;
    @SerializedName("prioridades")        public Map<String, Integer> prioridades;
    @SerializedName("tags")               public List<TagItem>        tags;

    public static class BarraItem {
        @SerializedName("data")  public String data;
        @SerializedName("label") public String label;
        @SerializedName("qtd")   public int    qtd;
    }

    public static class TagItem {
        @SerializedName("tag")     public String tag;
        @SerializedName("qtd")     public int    qtd;
        @SerializedName("percent") public int    percent;
    }
}