package com.example.focus.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Classe responsável por criar a instância do Retrofit
 * Define a URL base da API
 */
public class RetrofitClient {

    // ⚠️ TROCAR PELO SEU IP LOCAL (XAMPP)
    private static final String BASE_URL = "http://tcc3edsmodetecgr7.hospedagemdesites.ws/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Converte JSON automático
                    .build();
        }
        return retrofit;
    }
}


