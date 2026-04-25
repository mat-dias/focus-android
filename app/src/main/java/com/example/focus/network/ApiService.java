package com.example.focus.network;

import com.example.focus.responses.BasicResponse;
import com.example.focus.responses.LoginResponse;
import com.example.focus.responses.TaskResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(
            @Field("email") String email,
            @Field("senha") String senha
    );

    @FormUrlEncoded
    @POST("register.php")
    Call<BasicResponse> register(
            @Field("nome") String nome,
            @Field("email") String email,
            @Field("senha") String senha
    );

    @FormUrlEncoded
    @POST("add_task.php")
    Call<BasicResponse> addTask(
            @Field("profile_id") int profileId,
            @Field("title") String title,
            @Field("priority") String priority,
            @Field("tag") String tag
    );

    @FormUrlEncoded
    @POST("get_tasks.php")
    Call<TaskResponse> getTasks(
            @Field("profile_id") int profileId
    );
}