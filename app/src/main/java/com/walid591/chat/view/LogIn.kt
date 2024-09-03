package com.walid591.chat.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.walid591.chat.R
import com.walid591.chat.adapter.UserAdapter
import com.walid591.chat.models.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class LogIn : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogIn: Button
    private lateinit var btnSignUp: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var userAdapter: UserAdapter
    private var userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        initViews()
    
    
      //  updateUserOnlineStatus(false)

        btnSignUp.setOnClickListener {
            navigateToSignUp()
        }

        btnLogIn.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            val currentUser = mAuth.currentUser
            currentUser?.let {
        
            
            }

            if (isValidInput(email, password)) {
                // Start a coroutine for asynchronous login operation

                   login(email, password)
               

            } else {
                showToast("Please enter both email and password.")
            }
        }
    }
    
    override fun onStart()
    {
        super.onStart()
        updateUserOnlineStatus(false)
    
    }
    private fun updateUserOnlineStatus(online: Boolean)
    {
        val currentUser = mAuth.currentUser
        currentUser?.let {
            val userRef = mDbRef.child("users").child(it.uid)
            userRef.child("online").setValue(online)
        }
    }
    
    
    // Initialize views
    private fun initViews() {
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        btnLogIn = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)
        mDbRef = FirebaseDatabase.getInstance().reference
        // Initialize userAdapter here
        // Initialize the userAdapter with the mutable list and context
        userList = mutableListOf()
        userAdapter = UserAdapter(this, userList)
    
    }

    // Validate input fields
    private fun isValidInput(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }

    // Navigate to sign-up screen
    private fun navigateToSignUp() {
        val intent = Intent(this@LogIn, SignUp::class.java)
        startActivity(intent)
        finish()
    }

    // Perform login using Firebase authentication inside a coroutine
    private  fun login(email: String, password: String) {

      
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                
                mAuth.signInWithEmailAndPassword(email, password).await()
               
                withContext(Dispatchers.Main) {
              
                    
                    navigateToMainActivity()
                   
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleLoginError(e)
                }
            }
        }
    
    }
    
   
    
    // Function to navigate to MainActivity
    private fun navigateToMainActivity() {
        val authenticatedUserId = mAuth.currentUser?.uid ?: return
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_ID", authenticatedUserId)
        startActivity(intent)
      //  finish() // Optionally finish the login activity so the user can't navigate back
    }

    // Handle login errors and display appropriate messages
    private fun handleLoginError(exception: Exception) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "User does not exist"
            is FirebaseAuthInvalidCredentialsException -> "Invalid password"
            is FirebaseNetworkException -> "Network error. Please check your connection."
            else -> {
                Log.e("LoginError", "Unhandled exception: ${exception.javaClass.simpleName}: ${exception.message}")
                "Login failed, please try again."
            }
        }
        // Show the error message to the user (e.g., in a Toast or Snackbar)
        // Show the error message to the user
        showToast(errorMessage)
    }
    
    
    // Display a toast message
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
