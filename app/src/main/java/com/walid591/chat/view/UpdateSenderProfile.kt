package com.walid591.chat.view

import android.content.Intent
import android.os.Bundle

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.walid591.chat.R
import com.walid591.chat.models.User

class UpdateSenderProfile : AppCompatActivity() {

    private lateinit var imageSenderFromUpdateSenderProfileActivity: ImageView
    private lateinit var senderNameInProfile: TextView
    private lateinit var   senderEmailInProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_sender_profile)  // Replace with the layout file used in your activity

        imageSenderFromUpdateSenderProfileActivity = findViewById(R.id.imageView4)
        senderNameInProfile = findViewById(R.id.textName)
        senderEmailInProfile = findViewById(R.id.textEmail)

        // Assume you have the Firebase user UID
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        // Get a reference to the "users" node in the database
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        // Query the database for the specific user UID
        val userQuery = databaseReference.child(userUid!!)

        // Add a ValueEventListener to listen for changes in the data
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the user exists in the database
                if (snapshot.exists()) {
                    // Retrieve user data from the snapshot
                    val user = snapshot.getValue(User::class.java)

                    // Now 'user' contains the retrieved data (name, profilePicture, email)
                    if (user != null) {
                        val name = user.name
                        val profilePicture = user.profilePicture
                        val email = user.email

                        // Use the retrieved data to update UI
                        senderNameInProfile.text = name
                        senderEmailInProfile.text = email

                        // Load the profile picture using an image loading library (e.g., Glide)
                        Glide.with(this@UpdateSenderProfile)
                            .load(profilePicture)
                            .circleCrop()
                            .into(imageSenderFromUpdateSenderProfileActivity)
                    }
                } else {
                    // User does not exist in the database
                    Toast.makeText(
                        this@UpdateSenderProfile,
                        "Failed to retrieve  data ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occurred during the data retrieval
            }
        })


        // Set click listeners for profile picture, name, and email
        imageSenderFromUpdateSenderProfileActivity.setOnClickListener {
            // Open an activity or fragment for updating the profile picture
            openUpdateSenderProfilePictureActivity(userUid)


        }

    }




// ... (Previous code for retrieving user data and loadProfilePicture)

    private fun openUpdateSenderProfilePictureActivity(userUid: String?) {
        val intent = Intent(this, UpdateSenderProfilePictureActivity::class.java)
        intent.putExtra("userUid", userUid)

        startActivity(intent)
    }



}



