package com.walid591.chat.view

import android.os.Bundle

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.walid591.chat.R

class UserReceiverProfile : AppCompatActivity() {

    private lateinit var imageView2: ImageView
    private lateinit var textView: TextView
    private lateinit var textView2: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_receiver_profile)



            imageView2 = findViewById(R.id.imageProfile)
            textView = findViewById(R.id.textName)
            textView2 = findViewById(R.id.textEmail)

            // Retrieve user data from intent extras
            val userReceiverName = intent.getStringExtra("name")
            val userReceiverProfilePicture = intent.getStringExtra("profilePicture")
            val userReceiverEmail = intent.getStringExtra("email")
        val userReceiverUid = intent.getStringExtra("uid")

            // Use the retrieved data to update your UI

        if (userReceiverName != null && userReceiverUid != null && userReceiverProfilePicture != null && userReceiverEmail != null) {
            // Proceed with updating the UI

            textView.text = userReceiverName
            textView2.text = userReceiverEmail

            // Load the user profile picture using Glide
            Glide.with(this)
                .load(userReceiverProfilePicture)
                .circleCrop()
                .into(imageView2)
        }else {
            // Handle the case where some data is missing
            Toast.makeText(this, "Some data is missing", Toast.LENGTH_SHORT).show()
        }


    }
    }
