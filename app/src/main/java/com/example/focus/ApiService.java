package com.example.focus;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Interface de comunicação com a API (backend PHP)
 * Define todos os endpoints usados pelo app
 */
public interface ApiService {

    // LOGIN
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(
            @Field("email") String email,
            @Field("senha") String senha
    );

    // CADASTRO
    @FormUrlEncoded
    @POST("register.php")
    Call<BasicResponse> register(
            @Field("nome") String nome,
            @Field("email") String email,
            @Field("senha") String senha
    );
}