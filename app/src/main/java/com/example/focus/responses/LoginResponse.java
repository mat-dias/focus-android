package com.example.focus.responses;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    public String status;

    @SerializedName("user_id")
    public int userId;

    @SerializedName("profile_id")
    public int profileId;

    @SerializedName("nome")
    public String nome;

    @SerializedName("email")
    public String email;

    @SerializedName("xp")
    public int xp;

    @SerializedName("streak")
    public int streak;
}