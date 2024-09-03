package com.walid591.chat.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.walid591.chat.R
import com.walid591.chat.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateSenderProfilePictureActivity : AppCompatActivity() {
    
    private lateinit var imageProfile: ImageView
    private lateinit var buttonUpload: Button
    private lateinit var progressBarUpdatePicture: ProgressBar
    private var imageUri: Uri? = null
    private val storageRef: StorageReference by lazy { FirebaseStorage.getInstance().reference }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_update_profile_picture)
        
        // Keeping findViewById as requested
        imageProfile = findViewById(R.id.imageView7)
        buttonUpload = findViewById(R.id.buttonUpload)
        progressBarUpdatePicture = findViewById(R.id.progressBarUpdatePicutre)
        
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        
        userUid?.let { uid ->
            loadCurrentProfilePicture(uid)
        }
        
        buttonUpload.setOnClickListener {
            pickImage.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
        }
        
        findViewById<Button>(R.id.buttonSave).setOnClickListener {
            imageUri?.let {
                showProgressBar(true)
                userUid?.let { uid ->
                    lifecycleScope.launch {
                        uploadImageToStorage(uid)
                    }
                }
            } ?: finish()
        }
    }
    
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                imageUri?.let {
                    loadImageIntoView(it)
                }
            }
        }
    
    private fun loadImageIntoView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.profilee)
            .error(R.drawable.profilee)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageProfile)
    }
    
    private fun loadCurrentProfilePicture(userUid: String) {
        lifecycleScope.launch {
            val imageUrl = getCurrentProfilePictureUrl(userUid)
            loadImageIntoView(Uri.parse(imageUrl))
        }
    }
    
    private suspend fun getCurrentProfilePictureUrl(userUid: String): String? {
        val currentUserReference = FirebaseDatabase.getInstance().getReference("users").child(userUid)
        return withContext(Dispatchers.IO) {
            val snapshot = currentUserReference.get().await()
            val user = snapshot.getValue(User::class.java)
            user?.profilePicture
        }
    }
    
    private suspend fun uploadImageToStorage(userUid: String) {
        val uniqueFileName = "profilePicture_$userUid.jpg"
        val storageReference = storageRef.child("profilePicture/$uniqueFileName")
        
        imageUri?.let {
            try {
                val downloadUri = storageReference.putFile(it).await().storage.downloadUrl.await()
                updateProfilePictureUrlInDatabase(userUid, downloadUri.toString())
              
            } catch (e: Exception) {
                // Handle exceptions (e.g., network issues)
                showError(e.message)
            } finally {
                showProgressBar(false)
            }
        }
    }
    
    private suspend fun updateProfilePictureUrlInDatabase(userUid: String, newImageUrl: String) {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance().getReference("users").child(userUid)
                .child("profilePicture").setValue(newImageUrl).await()
        }
    }
    
   
    
    private fun showProgressBar(show: Boolean) {
        progressBarUpdatePicture.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showError(message: String?) {
        // Check if the message is not null or empty
        message?.let {
            // Assuming you have a CoordinatorLayout or a suitable root view in your layout
            Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_SHORT).show()
        } ?: run {
            // Provide a default error message if none is provided
            Snackbar.make(findViewById(android.R.id.content), "An error occurred", Snackbar.LENGTH_SHORT).show()
        }
    }
    
}
