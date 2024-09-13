package com.walid591.chat.view



import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.walid591.chat.R
import com.walid591.chat.models.User


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SignUp : AppCompatActivity()
{
    
    
    private lateinit var mAuth: FirebaseAuth
    private lateinit var edtEmail: EditText
    private lateinit var edtName: EditText
    private lateinit var edtPassword: EditText
    private lateinit var signUpButton: Button
    private lateinit var profilePicture: ImageView
    
    private var selectedImageUri: Uri? = null
    private var permissionRequestCode = 1001
 
    
    
    // result after pick image
    private val pickGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null)
            {
                
                selectedImageUri = uri
                Glide.with(this).load(selectedImageUri).circleCrop()
                    .into(findViewById(R.id.senderProfilePicture))
            } else
            {
                Toast.makeText(this, "Error to load selectImageUri", Toast.LENGTH_SHORT).show()
            }
        }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        
        Log.d("signup", "before action ")
        FirebaseApp.initializeApp(this)
        
        // Initialize views
        edtEmail = findViewById(R.id.edt_email)
        edtName = findViewById(R.id.edt_name)
        edtPassword = findViewById(R.id.edt_password)
        signUpButton = findViewById(R.id.signUp)
        profilePicture = findViewById(R.id.senderProfilePicture)
        
        
        Log.d("mAuth", "mAuth before")
        mAuth = FirebaseAuth.getInstance()
        Log.d("mAuth", "mAuth after")
        
        // Set onClickListener for edtProfilePicture to handle image selection
        profilePicture.setOnClickListener {
            Log.d(" profilePicture", "mAuth before")
            checkPermissionsAndPickImage()
            Log.d("profilePicture", "mAuth after")
        }
        
        // Hide the action bar
        supportActionBar?.hide()
        
        // Set onClickListener for signUpButton to perform sign-up process
        signUpButton.setOnClickListener {
            
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            signUp(name, email, password)
        }
        
        
    }
    
    
    private fun checkPermissionsAndPickImage()
    {
        
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                if (ActivityCompat.checkSelfPermission(
                        this, READ_MEDIA_IMAGES
                                                      ) != PackageManager.PERMISSION_GRANTED
                )
                {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(READ_MEDIA_IMAGES), permissionRequestCode
                                                     )
                    return
                }
            } else
            {
                if (ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.READ_EXTERNAL_STORAGE
                                                     ) != PackageManager.PERMISSION_GRANTED
                )
                {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        permissionRequestCode
                                                     )
                    return
                }
            }
            
            
        } catch (e: Exception)
        {
            // Handle the exception here
            Toast.makeText(this, "Failed to check permissions: $e", Toast.LENGTH_SHORT).show()
        }
        
        // The user has granted the necessary permissions, so launch the gallery picker
        launchGalleryPicker()
    }
    
    
    private fun launchGalleryPicker()
    {
        Log.d("initialisation", "requestGalleryPicker Successful before action fun")
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickGalleryLauncher.launch("image/*")
        
        Log.d("initialisation", "requestGalleryPicker Successful after action fun")
    }
    
    
    private fun signUp(name: String, email: String, password: String)
    {
        // Input validation
        if (name.isEmpty())
        {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show()
            return
        }
    
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return
        }
    
        if (password.isEmpty() || password.length < 6)
        {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT)
                .show()
            return
        }
    
        // Show loading indicator
        showLoadingIndicator(true)
    
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    val currentUser = mAuth.currentUser
                    currentUser?.let { user ->
                        lifecycleScope.launch {
                            try
                            {
                                val fcmToken =
                                    getFcmToken() ?: "" // Default to empty string if null
                                val profilePictureUrl = selectedImageUri?.let { uri ->
                                    uploadProfilePicture(user.uid, uri)
                                } ?: "" // Default to empty string if no image
                            
                                // Perform database operations
                                val isAddedToDatabase = withContext(Dispatchers.IO) {
                                    try
                                    {
                                        addUserToDatabase(
                                            name, email, user.uid, profilePictureUrl, fcmToken, true
                                                         )
                                        true
                                    } catch (e: Exception)
                                    {
                                        Log.e("SignUpError", "Error adding user to database", e)
                                        false
                                    }
                                }
                            
                                // Navigate to MainActivity if the user was added to the database
                                withContext(Dispatchers.Main) {
                                    if (isAddedToDatabase)
                                    {
                                        navigateToMainActivity()
                                    } else
                                    {
                                        Toast.makeText(
                                            this@SignUp,
                                            "Error adding user to database.",
                                            Toast.LENGTH_SHORT
                                                      ).show()
                                    }
                                    showLoadingIndicator(false)
                                }
                            } catch (e: Exception)
                            {
                                Log.e("SignUpError", "Error during sign-up", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@SignUp, "Error during sign-up.", Toast.LENGTH_SHORT
                                                  ).show()
                                    showLoadingIndicator(false)
                                }
                            }
                        }
                    }
                } else
                {
                    val exception = task.exception
                    Log.e("SignUpError", "Registration failed", exception)
                    val errorMessage = when (exception)
                    {
                        is FirebaseAuthUserCollisionException -> "This email is already registered. Please use a different email or try logging in."
                        is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use a stronger password."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format. Please check your email and try again."
                        else -> "Registration failed: ${exception?.message ?: "Unknown error"}. Please try again later."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    showLoadingIndicator(false)
                }
            
            }
    }
    
    
    private suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e("SignUpError", "Failed to retrieve FCM token", e)
            null
        }
    }
    
    private fun showLoadingIndicator(show: Boolean) {
        val progressBarSignUp = findViewById<ProgressBar>(R.id.progressBar2)
        progressBarSignUp.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
                                           )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == permissionRequestCode)
        {
            // The user has granted both permissions
            launchGalleryPicker()
        } else
        {
            // The user has denied either permission
            Toast.makeText(
                this, "You need to grant  permissions to pick an image.", Toast.LENGTH_SHORT
                          ).show()
        }
    }
    
    
    //storage the picture in data base
    private suspend fun uploadProfilePicture(uid: String, selectedImageUri: Uri): String
    {
        val storage = FirebaseStorage.getInstance()
        val uniqueFileName = "profilePicture_$uid.jpg" // Append user's UID to the file name
        
        val storageReference = storage.reference.child("profilePictures/${uniqueFileName}")
        val uploadTask = storageReference.putFile(selectedImageUri)
        uploadTask.await() // Wait until the upload is complete
        return storageReference.downloadUrl.await().toString() // Get and return the download URL
    }
    
    
    
    
    
    private suspend fun addUserToDatabase(
        name: String,
        email: String,
        uid: String,
        profilePictureUrl: String,
        fcmToken: String,
        online: Boolean
                                         ) {
        withContext(Dispatchers.IO) {
            try {
                val user = User(
                    name = name,
                    uid = uid,
                    email = email,
                    profilePicture = profilePictureUrl,
                    fcmToken = fcmToken,
                    online = online
                               )
                val mDbRef = FirebaseDatabase.getInstance().getReference("users")
                mDbRef.child(uid).setValue(user).await() // Ensure database write is completed
                
                Log.d("SignUp", "User data successfully added to the database.")
            } catch (e: Exception) {
                Log.e("SignUpError", "Error adding user to database", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUp, "Error adding user to database", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this@SignUp, MainActivity::class.java)
        startActivity(intent)
        finish() // Close SignUpActivity
    }
    
    
}
    












