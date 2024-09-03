package com.walid591.chat.view


import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.walid591.chat.R


class FullSizePhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_size_layout)

        val imageUrl = intent.getStringExtra("imageUrl")

        val fullSizeImageView: ImageView = findViewById(R.id.fullSizeImageView)

        // Load the image into the ImageView using Glide
        imageUrl?.let {
            Glide.with(this)
                .load(imageUrl)
                .into(fullSizeImageView)
        }
    }
}
