package com.walid591.chat.adapter


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable


import android.util.Log


import android.view.LayoutInflater
import android.view.View


import com.bumptech.glide.request.target.Target


import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView

import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player


import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager


import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener


import com.google.firebase.auth.FirebaseAuth


import com.google.firebase.database.FirebaseDatabase
import com.walid591.chat.models.Message
import com.walid591.chat.R
import com.walid591.chat.utlilities.formatTimestamp
import com.walid591.chat.view.ChatActivity
import com.walid591.chat.view.FullSizePhotoActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MessageAdapter(
    private val context: Context,
    
    private val messageList: MutableList<Message>,
    private val senderRoomUid: String,
    private val receiverRoomUid: String,
    private var chatRecyclerView: RecyclerView,
    private val activity: ChatActivity   // Inject the activity look methode activity.detachlistenner
                    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    
    private var mDbRef = FirebaseDatabase.getInstance().reference
    private val itemReceive = 1
    private val itemSent = 2
    
    // List to track all active players
    private val activePlayers = mutableListOf<ExoPlayer>()
    
    // private val playerList = mutableListOf<ExoPlayer?>()
    private var isImageMessageJustSent = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // private var playerMap: MutableMap<String, ExoPlayer> = mutableMapOf()
    //  private var currentPlayingPlayer: ExoPlayer? = null
    
    private val playerViews: MutableSet<PlayerView> = mutableSetOf()
    
    // Variables to track active player and playerView
    private var activePlayer: ExoPlayer? = null
    private var activePlayerView: PlayerView? = null
    private var activeViewHolder: RecyclerView.ViewHolder? = null
    private var player: ExoPlayer? = null
    
    
   
    
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val view = if (viewType == itemSent)
        {
            LayoutInflater.from(context).inflate(R.layout.send, parent, false)
        } else
        {
            LayoutInflater.from(context).inflate(R.layout.recive, parent, false)
        }
        
        return if (viewType == itemSent)
        {
            SentViewHolder(view)
        } else
        {
            ReceiveViewHolder(view)
        }
    }
    
    // on bind
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val message = messageList[position]
        
        if (holder is SentViewHolder)
        {
            handleSentMessage(holder, message)
        } else if (holder is ReceiveViewHolder)
        {
            handleReceivedMessage(holder, message, position)
        }
    }
    
    
    // on bind
    
    
    private fun handleSentMessage(holder: SentViewHolder, message: Message)
    {
        
        
        when
        {
            !message.textMessage.isNullOrEmpty() ->
            {
                
                message.audioUrl = null
                message.imageUrl = null
                holder.audioPlayerViewSent.player?.release()
                holder.audioPlayerViewSent.player = null
                // Cle ar any previous image load to avoid flickering or incorrect images
                Glide.with(holder.itemView.context).clear(holder.sentImage)
                holder.sentImage.setImageDrawable(null)
                holder.sentMessages.text = message.textMessage
                holder.sentMessages.visibility = View.VISIBLE
                holder.sentImage.visibility = View.GONE
                holder.audioPlayerViewSent.visibility = View.GONE
                
                holder.deleteSenderButton.visibility = View.GONE
                
                updateStatusText(holder.statusSent, message.viewedByReceiver)
                
                holder.timestampTextViewSent.text = formatTimestamp(message.timestamp)
                
                
                holder.sentMessages.setOnClickListener {
                    
                    
                    showDeleteMessageDialogForText(message, "senderRoom")
                }
                
                
            }
            
            
            !message.imageUrl.isNullOrEmpty() ->
            {
                
                message.textMessage = null
                message.audioUrl = null
                holder.audioPlayerViewSent.player?.release()
                holder.audioPlayerViewSent.player = null
                // Clear any previous image load to avoid flickering or incorrect images
                Glide.with(holder.itemView.context).clear(holder.sentImage)
                holder.sentImage.setImageDrawable(null)
                
                
                // Load the new image using Glide with a listener
                Glide.with(holder.itemView.context).load(message.imageUrl)
                    .listener(object : RequestListener<Drawable>
                    {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                                                 ): Boolean
                        {
                            // Handle load failure
                            return false
                        }
                        
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                                                    ): Boolean
                        {
                            
                            
                            return false
                        }
                    }).into(holder.sentImage)
                
                // Set visibility for different views based on the message type
                holder.sentImage.visibility = View.VISIBLE
                holder.sentMessages.visibility = View.GONE
                holder.audioPlayerViewSent.visibility = View.GONE
                holder.deleteSenderButton.visibility = View.GONE
                
                
                updateStatusText(holder.statusSent, message.viewedByReceiver)
                
                holder.timestampTextViewSent.text = formatTimestamp(message.timestamp)
                
                holder.sentImage.setOnClickListener {
                    // Show photo options dialog for image messages
                    showPhotoOptionsDialog(message, "senderRoom")
                }
            }
            
            !message.audioUrl.isNullOrEmpty() ->
            {
                
                
                message.textMessage = null
                message.imageUrl = null
                
                holder.sentMessages.visibility = View.GONE
                holder.sentImage.visibility = View.GONE
                holder.audioPlayerViewSent.visibility = View.VISIBLE
                holder.deleteSenderButton.visibility = View.VISIBLE
                // If the holder is not already the active one
                //  if message send activePlayer currently is recycled by new holder
                if (holder != activeViewHolder)
                {
                    
                    
                    // Initialize a new player for the current holder
                    holder.player =
                        initializePlayer(holder, holder.audioPlayerViewSent, message.audioUrl!!)
                    
                } else
                {
                    releaseActivePlayer()
                }
                
                holder.timestampTextViewSent.text = formatTimestamp(message.timestamp)
                
                
                //  holder.audioPlayerViewSent.tag = position
                
                
                // Scroll to this position
                /*  chatRecyclerView.post {
                      chatRecyclerView.scrollToPosition(position)
                 }*/
                
                updateStatusText(holder.statusSent, message.viewedByReceiver)
                
                
                
                
                holder.deleteSenderButton.setOnClickListener {
                    showDeleteConfirmationAudioDialog(message, "senderRoom")
                }
                
                // Scroll to the last message when binding the last item
                
            }
            
            
        }
        
        
    }
    
    
    private fun handleReceivedMessage(holder: ReceiveViewHolder, message: Message, position: Int)
    {
        
        
        when
        {
            !message.textMessage.isNullOrEmpty() ->
            {
                // currentMessage.viewedByReceiver = true
                updateMessageStatus(message, position)
                message.audioUrl = null
                message.imageUrl = null
                holder.audioPlayerViewReceived.player?.release()
                holder.audioPlayerViewReceived.player = null
                holder.receiverMessages.text = message.textMessage
                holder.receiverMessages.visibility = View.VISIBLE
                holder.receiverImage.visibility = View.GONE
                holder.audioPlayerViewReceived.visibility = View.GONE
                
                holder.deleteReceiverButton.visibility = View.GONE
                
                updateStatusText(holder.statusReceived, message.viewedByReceiver)
                holder.timestampTextViewReciever.text = formatTimestamp(message.timestamp)
                
                holder.receiverMessages.setOnClickListener {
                    
                    showDeleteMessageDialogForText(message, "receiverRoom")
                }
                
            }
            
            !message.imageUrl.isNullOrEmpty() ->
            {
                //currentMessage.viewedByReceiver = true
                updateMessageStatus(message, position)
                message.audioUrl = null
                
                holder.audioPlayerViewReceived.player?.release()
                holder.audioPlayerViewReceived.player = null
                
                // Clear any previous image load to avoid flickering or incorrect images
                Glide.with(holder.itemView.context).clear(holder.receiverImage)
                holder.receiverImage.setImageDrawable(null)
                Glide.with(holder.itemView.context).load(message.imageUrl)
                    .listener(object : RequestListener<Drawable>
                    {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                                                 ): Boolean
                        {
                            // Handle load failure
                            return false
                        }
                        
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                                                    ): Boolean
                        {
                            
                            
                            return false
                        }
                    }).into(holder.receiverImage)
                
                holder.receiverImage.visibility = View.VISIBLE
                holder.receiverMessages.visibility = View.GONE
                holder.audioPlayerViewReceived.visibility = View.GONE
                holder.deleteReceiverButton.visibility = View.GONE
                
                updateStatusText(holder.statusReceived, message.viewedByReceiver)
                
                holder.timestampTextViewReciever.text = formatTimestamp(message.timestamp)
                
                
                holder.receiverImage.setOnClickListener {
                    // Show photo options dialog for image messages
                    showPhotoOptionsDialog(message, "receiverRoom")
                }
                
            }
            
            !message.audioUrl.isNullOrEmpty() ->
            {
                // currentMessage.viewedByReceiver = true
                updateMessageStatus(message, position)
                
                message.textMessage = null
                message.imageUrl = null
                
                holder.receiverMessages.visibility = View.GONE
                holder.receiverImage.visibility = View.GONE
                holder.deleteReceiverButton.visibility = View.VISIBLE
                holder.audioPlayerViewReceived.visibility = View.VISIBLE
                
                
                //  if message send activePlayer currently is recycled by new holder
                if (holder != activeViewHolder)
                {
                    
                    
                    // Initialize a new player for the current holder
                    holder.player =
                        initializePlayer(holder, holder.audioPlayerViewReceived, message.audioUrl!!)
                    
                } else
                {
                    releaseActivePlayer()
                }
                
                holder.timestampTextViewReciever.text = formatTimestamp(message.timestamp)
                
                
                
                
                
                
                updateStatusText(holder.statusReceived, message.viewedByReceiver)
                
                holder.deleteReceiverButton.setOnClickListener {
                    showDeleteConfirmationAudioDialog(message, "receiverRoom")
                }
                
                
            }
            
        }
    }
    
    
    private fun updateStatusText(statusText: TextView, viewedByReceiver: Boolean)
    {
        statusText.text = if (viewedByReceiver) "√√" else "√√"
        statusText.setTextColor(
            ContextCompat.getColor(
                context, if (viewedByReceiver) R.color.green else R.color.red
                                  )
                               )
    }
    
    @SuppressLint("NotifyDataSetChanged")
    private fun updateMessageStatus(message: Message, position: Int)
    {
        if (message.viewedByReceiver)
        {
            return // If the message is already viewed, return early
        }
        
        val receiverMessageRef = mDbRef.child("chats").child(receiverRoomUid).child("messages")
            .child(message.messageId!!)
        
        val receiverUpdates = mapOf(
            "viewedByReceiver" to true, "status" to "√√"
                                   )
        
        receiverMessageRef.updateChildren(receiverUpdates).addOnSuccessListener {
            Log.d("MessageAdapter", "Receiver message status updated to viewed")
            
            val senderMessageRef = mDbRef.child("chats").child(senderRoomUid).child("messages")
                .child(message.messageId!!)
            
            val senderUpdates = mapOf(
                "viewedByReceiver" to true, "status" to "√√"
                                     )
            
            senderMessageRef.updateChildren(senderUpdates).addOnSuccessListener {
                Log.d("MessageAdapter", "Sender message status updated to viewed")
                
                message.viewedByReceiver = true
                message.status = "view"
                
                notifyItemChanged(position) // Update only the specific item
            }.addOnFailureListener {
                Log.e("MessageAdapter", "Failed to update sender message status", it)
            }
        }.addOnFailureListener {
            Log.e("MessageAdapter", "Failed to update receiver message status", it)
        }
    }
    
    
    private fun findPositionByMessageId(messageId: String): Int
    {
        return messageList.indexOfFirst { it.messageId == messageId }
    }
    
    
    private fun showDeleteMessageDialogForText(message: Message, roomType: String)
    {
        AlertDialog.Builder(context).setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Yes") { _, _ ->
                coroutineScope.launch {
                    deleteMessageFromDb(message, roomType)
                }
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
    
    
    private suspend fun deleteMessageFromDb(message: Message, roomType: String)
    {
        
        
        try
        {
            
            
            withContext(Dispatchers.IO) {
                val roomUid = if (roomType == "senderRoom")
                {
                    "${message.senderUid}_${message.receiverUid}"
                } else
                {
                    "${message.receiverUid}_${message.senderUid}"
                }
                val senderRef = mDbRef.child("chats").child(roomUid).child("messages")
                val receiverRef = mDbRef.child("chats").child(receiverRoomUid).child("messages")
                val messageId = message.messageId
                
                if (messageId != null)
                {
                    Log.d(
                        "DeleteMessage",
                        "Deleting message at path: chats/$roomUid/messages/$messageId"
                         )
                    senderRef.child(messageId).removeValue().await()
                    receiverRef.child(messageId).removeValue().await()
                    Log.d("DeleteMessage", "Message deleted successfully from Firebase")
                } else
                {
                    throw Exception("Message ID is null, cannot delete message")
                }
            }
        } catch (exception: Exception)
        {
            Log.e("DeleteMessage", "Error deleting message: ${exception.message}", exception)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context, "Failed to delete message: ${exception.message}", Toast.LENGTH_SHORT
                              ).show()
            }
        }
    }
    
    
    // delete imageMessage
    
    private fun showPhotoOptionsDialog(message: Message, roomType: String)
    {
        AlertDialog.Builder(context).setTitle("Photo Options")
            .setMessage("What would you like to do with this photo?")
            .setPositiveButton("Delete") { _, _ ->
                coroutineScope.launch {
                    deleteMessageFromDb(message, roomType)
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setNeutralButton("View Full Size") { _, _ ->
                viewFullSizePhoto(message.imageUrl!!)
            }.show()
    }
    
    
    private fun showDeleteConfirmationAudioDialog(message: Message, roomType: String)
    {
        AlertDialog.Builder(context).setTitle("Delete Audio")
            .setMessage("Are you sure you want to delete this Audio Message?")
            .setPositiveButton("Yes") { _, _ ->
                coroutineScope.launch {
                    deleteMessageFromDb(message, roomType)
                }
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
    
    
    private fun viewFullSizePhoto(imageUrl: String)
    {
        val intent = Intent(context, FullSizePhotoActivity::class.java)
        intent.putExtra("imageUrl", imageUrl)
        context.startActivity(intent)
    }

// delete imageMessage


// delete audioMessage
    
    
    override fun getItemViewType(position: Int): Int
    {
        val message = messageList[position]
        return if (message.senderUid == FirebaseAuth.getInstance().currentUser?.uid)
        {
            itemSent
        } else
        {
            itemReceive
        }
    }
    
    
    override fun getItemCount(): Int
    {
        return messageList.size
    }
    
    override fun getItemId(position: Int): Long
    {
        return messageList[position].messageId.hashCode().toLong()
    }
    
    
    private fun initializePlayer(
        holder: RecyclerView.ViewHolder, playerView: PlayerView, audioUrl: String
                                ): ExoPlayer
    {
        // Release all active players to ensure only one is active
        // Initialize a new ExoPlayer instance
        val player = ExoPlayer.Builder(playerView.context).build()
        val mediaItem = MediaItem.fromUri(audioUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        
        playerView.player = player
        
        // Set the new active player and view holder
        activePlayer = player
        activePlayerView = playerView
        activeViewHolder = holder
        
        return player
    }
    
    // Function to release all active players
    private fun releaseActivePlayer()
    {
        activePlayer?.release()
        activePlayer = null
        activePlayerView?.player = null
        activePlayerView = null
        activeViewHolder = null
    }
    
    
    // In your ViewHolder class
    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var sentMessages: TextView = itemView.findViewById(R.id.txt_send_message)
        val statusSent: TextView = itemView.findViewById(R.id.status_sent)
        val sentImage: ImageView = itemView.findViewById(R.id.img_send_message)
        val audioPlayerViewSent: PlayerView = itemView.findViewById(R.id.audioPlayerSent)
        val deleteSenderButton: Button = itemView.findViewById(R.id.delete_sender_audio)
        val timestampTextViewSent: TextView = itemView.findViewById(R.id.timestampTextView)
        var player: ExoPlayer? = null
        fun cleanupTextAndImageSender()
        {
            // Clear the text to avoid showing incorrect data when recycled
            sentMessages.text = ""
            
            // Clear image to avoid loading the wrong image in a recycled view
            Glide.with(itemView.context).clear(sentImage)
        }
    }
    
    inner class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        
        
        var receiverMessages: TextView = itemView.findViewById(R.id.recievertext)
        val statusReceived: TextView = itemView.findViewById(R.id.status_received)
        val receiverImage: ImageView = itemView.findViewById(R.id.img_receive_message)
        val audioPlayerViewReceived: PlayerView = itemView.findViewById(R.id.audioPlayerReceived)
        val deleteReceiverButton: Button = itemView.findViewById(R.id.delete_receiver_audio)
        val timestampTextViewReciever: TextView = itemView.findViewById(R.id.timestampTextView)
        var player: ExoPlayer? = null
        
        fun cleanupTextAndImageRceiverender()
        {
            // Clear the text to avoid showing incorrect data when recycled
            receiverMessages.text = ""
            
            // Clear image to avoid loading the wrong image in a recycled view
            Glide.with(itemView.context).clear(receiverImage)
        }
        
    }
    
    override fun onViewRecycled(holder: RecyclerView.ViewHolder)
    {
        super.onViewRecycled(holder)
        if (holder == activeViewHolder)
        {
            releaseActivePlayer()
            when (activeViewHolder)
            {
                is SentViewHolder ->
                {
                    // Clear text resource if needed
                    (activeViewHolder as SentViewHolder).sentMessages.text = null
                    
                    // Clear image resource
                    Glide.with((activeViewHolder as SentViewHolder).itemView.context)
                        .clear((activeViewHolder as SentViewHolder).sentImage)
                    (activeViewHolder as SentViewHolder).sentImage.setImageDrawable(null)
                }
                
                is ReceiveViewHolder ->
                {
                    // Clear text resource if needed
                    (activeViewHolder as ReceiveViewHolder).receiverMessages.text = null
                    
                    // Clear image resource
                    Glide.with((activeViewHolder as ReceiveViewHolder).itemView.context)
                        .clear((activeViewHolder as ReceiveViewHolder).receiverImage)
                    (activeViewHolder as ReceiveViewHolder).receiverImage.setImageDrawable(null)
                }
            }
        }
        
        /*  releaseAllPlayers()
    
    
    
       when (holder) {
           is SentViewHolder -> {
               // Clear text resource if needed
            holder.sentMessages.text = null
            
               // Clear image resource
               Glide.with(holder.itemView.context).clear(holder.sentImage)
               holder.sentImage.setImageDrawable(null)
           }
        
           is ReceiveViewHolder -> {
               // Clear text resource if needed
               holder.receiverMessages.text = null
            
               // Clear image resource
               Glide.with(holder.itemView.context).clear(holder.receiverImage)
               holder.receiverImage.setImageDrawable(null)
           }
       }*/
    }
    
    
    // Release all players when RecyclerView is detached
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView)
        releaseAllPlayers()
        releaseActivePlayer()
        when (activeViewHolder)
        {
            is SentViewHolder ->
            {
                // Clear text resource if needed
                (activeViewHolder as SentViewHolder).sentMessages.text = null
                
                // Clear image resource
                Glide.with((activeViewHolder as SentViewHolder).itemView.context)
                    .clear((activeViewHolder as SentViewHolder).sentImage)
                (activeViewHolder as SentViewHolder).sentImage.setImageDrawable(null)
            }
            
            is ReceiveViewHolder ->
            {
                // Clear text resource if needed
                (activeViewHolder as ReceiveViewHolder).receiverMessages.text = null
                
                // Clear image resource
                Glide.with((activeViewHolder as ReceiveViewHolder).itemView.context)
                    .clear((activeViewHolder as ReceiveViewHolder).receiverImage)
                (activeViewHolder as ReceiveViewHolder).receiverImage.setImageDrawable(null)
            }
        }
    }
    
    // Release all ExoPlayer instances
    private fun releaseAllPlayers()
    {
        playerViews.forEach { playerView ->
            playerView.player?.release()
            playerView.player = null
        }
        playerViews.clear()
        
        releaseActivePlayer()
        
        
    }
    
    
}