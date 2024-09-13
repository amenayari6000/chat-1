package com.walid591.chat.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.walid591.chat.R
import com.walid591.chat.models.User
import com.walid591.chat.adapter.UserAdapter

import java.util.Locale

class MainActivity : AppCompatActivity()
{
    
    private lateinit var userAdapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    
    private lateinit var userList: MutableList<User>
  //  private lateinit var fullUserList: MutableList<User>
  private val originalUserList = mutableListOf<User>()
    
    
    
    
    
    
    
    private val userMap = HashMap<String, User>()  // To track users by ID
    // To store the list of users
    
    
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var userValueEventListener: ValueEventListener? = null
    
    override fun onCreate(savedInstanceState: Bundle?)
    
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    
        FirebaseApp.initializeApp(this)
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        
        
        // Initialize RecyclerView
        userRecyclerView = findViewById(R.id.userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userList = mutableListOf()
       // fullUserList = mutableListOf()
        userAdapter = UserAdapter(this, userList)
        userRecyclerView.adapter = userAdapter
    
      
        
        // Initialize SearchView
        searchView = findViewById(R.id.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String?): Boolean
            {
                return false
            }
            
            override fun onQueryTextChange(query: String?): Boolean
            {
                filterUserList(query)
                return true
            }
        })
        
        // Check authentication status
        val user = mAuth.currentUser?.uid
        if (user == null)
        {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LogIn::class.java))
            finish()
        } else
        {
            updateUserListFromDatabase()
            // handell the case when user is diconted and after if network is cut or any other reason  that track disconnected
            setupUserStatusOnDisconnect()
        }
    }
    
    override fun onStart()
    {
        super.onStart()
        if (mAuth.currentUser != null)
        {
            updateUserOnlineStatus(true) // Set user as online
            updateUserListFromDatabase() // Refresh the user list
            setupUserStatusOnDisconnect() // Set up disconnection handling
        }
    }
    
   
    
    
  //  val filteredList = mutableListOf<User>()
 
    
      private fun filterUserList(query: String?) {
          if (query.isNullOrEmpty()) {
              // When query is empty, reset to the original full list
              userAdapter.searchList(originalUserList)
          } else {
              // Filter the original list based on the query
              val filteredList = originalUserList.filter { user ->
                  user.name?.lowercase(Locale.getDefault())
                      ?.contains(query.lowercase(Locale.getDefault())) == true
              }
              // Update the adapter with the filtered list
              userAdapter.searchList(filteredList)
          }
      }
    
    
    private fun updateUserListFromDatabase()
    {
       
        mDbRef.child("users").addChildEventListener(object : ChildEventListener
        {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {
                val newUser = snapshot.getValue(User::class.java) ?: return
                val authenticatedUserId = mAuth.currentUser?.uid ?: return
                
                // Check if newUser.uid is not null
                newUser.uid?.let { userId ->
                    if (authenticatedUserId != userId && userMap[userId] == null)
                    {
                        userMap[userId] = newUser
                        // Add to both original and current display list
                        originalUserList.add(newUser)
                        userList.add(newUser)
                        userAdapter.notifyItemInserted(userList.size - 1)
                    }
                }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                val updatedUser = snapshot.getValue(User::class.java) ?: return
                val authenticatedUserId = mAuth.currentUser?.uid ?: return
    
                updatedUser.uid?.let { userId ->
                    if (authenticatedUserId != userId) {
                        // Find the index of the updated user in userList
                        val index = userList.indexOfFirst { it.uid == userId }
            
                        if (index != -1) {
                            // Update the user in the list
                            userList[index] = updatedUser
                
                            // Notify only the specific item that has changed
                            userAdapter.notifyItemChanged(index)
                        }
                    }
                }
            }
    
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedUser = snapshot.getValue(User::class.java) ?: return
                val authenticatedUserId = mAuth.currentUser?.uid ?: return
        
                removedUser.uid?.let { userId ->
                    if (authenticatedUserId != userId) {
                        userMap.remove(userId)?.let {
                            val index = userList.indexOf(it)
                            if (index != -1) {
                                userList.removeAt(index)
                                userAdapter.notifyItemRemoved(index)
                            }
                        }
                    } else {
                        // Handle removal of the authenticated user if necessary
                        userMap.remove(userId)
                        userList.clear() // Optionally clear the list if needed
                        userAdapter.notifyDataSetChanged()
                    }
                }
            }
    
    
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?)
            {
                // Optionally handle user reordering
                
                
                
            }
            
            override fun onCancelled(error: DatabaseError)
            {
                // Handle errors
            }
        })
    }
    
    private fun updateUserOnlineStatus(online: Boolean)
    {
        val currentUser = mAuth.currentUser
        currentUser?.let {
            val userRef = mDbRef.child("users").child(it.uid)
            userRef.child("online").setValue(online)
        }
    }
    
    
    private fun setupUserStatusOnDisconnect()
    {
        val currentUser = mAuth.currentUser
        currentUser?.let {
            val userRef = mDbRef.child("users").child(it.uid)
            // Create a reference to the user's status
            val userStatusRef = userRef.child("online")
            userStatusRef.onDisconnect().setValue(false)
        }
    }
    
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if (item.itemId == R.id.logout)
        {
// Remove the database listener to prevent memory leaks and unnecessary data fetching
            // Remove the database listener to prevent memory leaks and unnecessary data fetching
            userValueEventListener?.let {
                mDbRef.child("users").removeEventListener(it)
            }
            
            val currentUser = mAuth.currentUser
            currentUser?.let {
                updateUserOnlineStatus(false)
               
            }
            
            // Sign out from Firebase Auth
            mAuth.signOut()
            
            // Redirect to LogIn activity
            startActivity(Intent(this, LogIn::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
