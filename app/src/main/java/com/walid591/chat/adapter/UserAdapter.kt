package com.walid591.chat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.walid591.chat.R
import com.walid591.chat.models.User
import com.walid591.chat.view.ChatActivity


class UserAdapter(var context: Context, private var userList: MutableList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>()
{
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder
    {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)
        
        return UserViewHolder(view)
    }
    
    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int)
    {
        
        val currentUser = userList[position]
        holder.bind(currentUser)
        
        
        // Set additional properties directly after binding user data
        holder.textName.text = currentUser.name
        // Load and display profile picture using Glide
        currentUser.profilePicture?.let { profilePictureUrl ->
            Glide.with(holder.itemView.context).load(profilePictureUrl).circleCrop()
                .into(holder.profilePictureImageView)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", currentUser.name)
            
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("profilePicture", currentUser.profilePicture)
            intent.putExtra("fcmToken", currentUser.fcmToken.toString())
            intent.putExtra("email", currentUser.email)
            
            
            
            
            
            
            context.startActivity(intent)
            
        }
        // Update status background based on user's online status
        
        if (currentUser.online)
        {
            holder.status.setBackgroundResource(R.drawable.circle) // Online background
        } else
        {
            holder.status.setBackgroundResource(R.drawable.offline) // Offline background
        }
    }
    
    override fun getItemCount(): Int
    {
        return userList.size
        
    }
    fun searchList(filteredList: List<User>) {
        userList.clear()
        userList.addAll(filteredList)
        notifyDataSetChanged()
    }
    
    /*  fun filterList(filteredList: List<User>) {
          userList.clear()
          userList.addAll(filteredList)
          notifyDataSetChanged()
      }
  
  
   fun filterList(filteredList: List<User>)
   {
      
       userList.clear()
       userList.addAll(filteredList)
      // notifyItemRangeChanged(0, userList.size)
       notifyDataSetChanged()
   }
  fun filterList(filteredList: List<User>, fullUserList: List<User>?) {
      if (filteredList.isEmpty() && fullUserList != null) {
          // If the filtered list is empty and fullUserList is provided, restore fullUserList
          userList.clear()
          userList.addAll(fullUserList)
      } else {
          // Otherwise, use the filtered list
          userList.clear()
          userList.addAll(filteredList)
      }
      notifyDataSetChanged()  // Notify the adapter of the changes
  }*/
 
 
 
 
 
 // ViewHolder to hold user item views
 class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
 {
     val textName: TextView = itemView.findViewById(R.id.txt_name)
     val profilePictureImageView: ImageView = itemView.findViewById(R.id.profilePicture)
     var status: TextView = itemView.findViewById(R.id.textView7)
     
     // Bind user data to the ViewHolder
     fun bind(user: User)
     {
         textName.text = user.name
         // Load profile picture using Glide from Firebase Storage URL
         
         
         user.profilePicture?.let { profilePictureUrl ->
             Glide.with(itemView.context).load(profilePictureUrl).circleCrop()
                 .placeholder(R.drawable.profile)
                 .error(R.drawable.profile) // Set your error placeholder image here
                 .into(profilePictureImageView)
         } ?: run {
             // Handle null profile picture case, maybe load a placeholder image
             Glide.with(itemView.context).load(R.drawable.profile).circleCrop()
                 .into(profilePictureImageView)
         }
     }
 }
}



