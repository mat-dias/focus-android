package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;

public class RelatorioResponse {
    public String status;
    public String msg;

    @SerializedName("relatorio") public String relatorio;
}