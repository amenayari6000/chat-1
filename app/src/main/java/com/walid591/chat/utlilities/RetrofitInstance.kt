package com.walid591.chat.utlilities



import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl("http://Send-env.eba-stkgacbs.eu-west-2.elasticbeanstalk.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()


        }

        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}

