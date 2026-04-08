package com.example.focus;

/**
 * Classe que representa a resposta da API de login
 * Retrofit converte automaticamente o JSON para esse objeto
 */
public class LoginResponse {
    public String status;
    public int id;
    public String nome;
    public String username;
    public int xp;
    public int streak;
}