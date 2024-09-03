package com.walid591.chat.utlilities




import com.walid591.chat.utlilities.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body

import retrofit2.http.POST

// new http v1 request https://firebase.google.com/docs/cloud-messaging/migrate-v1

 interface NotificationAPI {


     @POST("/send")

     suspend fun postNotification(

         @Body body: PushNotification
     ): Response<ResponseBody>


}
