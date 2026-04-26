package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileResponse {

    public String status;

    @SerializedName("foto_url")
    public String fotoUrl;  // null se não enviou foto
}
