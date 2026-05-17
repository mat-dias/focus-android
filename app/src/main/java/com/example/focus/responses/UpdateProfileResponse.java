package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileResponse {
    public String status;
    public String msg;        // ← adicione esta linha

    @SerializedName("foto_url")
    public String fotoUrl;
}