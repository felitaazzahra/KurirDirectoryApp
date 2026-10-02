package com.example.kurirdirectoryapp

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    // Mengambil seluruh koleksi data pengguna (10 data berbentuk array JSON)
    // Tipe balikan wajib Response<List<UserResponse>> karena endpoint mengembalikan array [...]
    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>
}