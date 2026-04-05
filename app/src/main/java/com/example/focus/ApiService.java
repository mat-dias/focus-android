package com.example.focus;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    Call<String> login(
            @Field("email") String email,
            @Field("senha") String senha
    );

    @FormUrlEncoded
    @POST("register.php")
    Call<String> register(
            @Field("nome") String nome,
            @Field("email") String email,
            @Field("senha") String senha,
            @Field("data_nasc") String dataNasc,
            @Field("sexo") String sexo
    );
}