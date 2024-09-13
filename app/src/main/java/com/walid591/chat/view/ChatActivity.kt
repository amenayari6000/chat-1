package com.walid591.chat.view


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
//import android.graphics.Rect
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
//import android.os.Parcelable
import android.util.Log
import android.view.View

import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

import com.google.firebase.storage.FirebaseStorage
import com.walid591.chat.models.Message
import com.walid591.chat.adapter.MessageAdapter
import com.walid591.chat.utlilities.NotificationData
import com.walid591.chat.utlilities.PushNotification
import com.walid591.chat.R
import com.walid591.chat.messages_senders.AudioMessageSender
import com.walid591.chat.messages_senders.ImageMessageSender
import com.walid591.chat.messages_senders.MessageSender
import com.walid591.chat.messages_senders.TextMessageSender
import com.walid591.chat.utlilities.RetrofitInstance
import com.walid591.chat.models.User

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File


class ChatActivity : AppCompatActivity()
{
    // Variable to store the action to be performed after permission is granted
    private var pendingAction: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    
    private lateinit var messageRefSender: DatabaseReference
    private lateinit var messageRefReceiver: DatabaseReference
    private var textMessage: String? = null
    
    
    private var senderChildEventListener: ChildEventListener? = null
    private var receiverChildEventListener: ChildEventListener? = null
    private var listener: ValueEventListener? = null
    
    
   
    
    
    private var mediaRecorder: MediaRecorder? = null
    private var state: String = "√√"
    private var RECORDING_STATE = "RECORDING_STATE"
    private var audioUri: Uri? = null
    private lateinit var filePath: String
    private var mediaPlayer: MediaPlayer? = null
    
    
    private lateinit var messageAdapter: MessageAdapter
    private var messageList = mutableListOf<Message>()
    private lateinit var senderRoomUid: String
    private lateinit var receiverRoomUid: String
    
    private lateinit var chatRecyclerView: RecyclerView
    
    
    private lateinit var senderProfileImageView: ImageView
    private lateinit var receiverprofileImageView: ImageView
    private var selectedImageUri: Uri? = null
    
    
    lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var receiverName: String
    // private lateinit var receiverEmail: String
    
    
    private lateinit var linearLayoutProgressBar: LinearLayout
    private lateinit var progressBarRecording: ProgressBar
    private lateinit var progressbarImage: ProgressBar
    // private lateinit var progressBarChatActivity: ProgressBar
    
    private lateinit var btnSendAudio: ImageButton
    private lateinit var btnSendImage: Button
    private lateinit var textView4: TextView
    private lateinit var imgMic: ImageView
    
    // EditText for typing messages
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var pause: ImageButton
    
    
    // ImageView for sending messages
    //  private lateinit var sendButton: ImageView
    private val TAG = "ChatActivity"
    //private val PERMISSIONS_REQUEST_CODE = 1001
    
    
    // result of  notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
                                                                     ) { isGranted: Boolean ->
        if (isGranted)
        {
            
            CoroutineScope(Dispatchers.Main).launch {
                
                sendNotification()
            }
            
        } else
        {
            // Permission is denied
            handleNotificationPermissionDenied()
        }
    }
    
    
    //--------------------- On Create ()  ---------------------
    
   
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
       // Toast.makeText(this, "onCreate called", Toast.LENGTH_SHORT).show()
        FirebaseApp.initializeApp(this)
        // Call initialization methods
        initializeFirebaseAndUserIDs()
        
        initializeViews()
        // Fetch the current user's data and set it to the TextView
        fetchCurrentUserDetails()
        // Setup RecyclerView
        setupRecyclerView()
        
        
        setupSendOnClickItem()
        
        
        //fun update token
        updateFcmToken()
        //-----------------------------------------------------------------------------------------------------------------------------------
        
        // upload sender profile picture in view
        loadSenderProfilePicture()
        //-----------------------------------------------------------------------------------------------------------------------------------
        
        // Load receiver profile picture
        loadProfilePicture(
            receiverprofileImageView, intent.getStringExtra("profilePicture").toString()
                          )
        
        
        // Set ActionBar title to receiver's name
        setActionBarTitle()
        
        
        //history message discussion all messages
        loadInitialMessageHistory()
        // just new message not all list for this we use notifyItemInserted
        // loadNewMessagesSend()
        
        
        // Setup Mic Button and Send Audio Button
        setupMicAndSendAudioButtons()
        
        
    }
    //*****end on create*********************
    
    
    //*******************************************************************************************
    
    //******inalize view with data******
    
    private fun initializeFirebaseAndUserIDs()
    {
        mDbRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        receiverName = intent.getStringExtra("name").toString()
        senderUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        receiverUid = intent.getStringExtra("uid").toString()
        
        
        senderRoomUid = "${senderUid}_${receiverUid}"
        receiverRoomUid = "${receiverUid}_${senderUid}"
        
        
        messageRefSender = mDbRef.child("chats").child(senderRoomUid).child("messages")
        messageRefReceiver = mDbRef.child("chats").child(receiverRoomUid).child("messages")
        //------------------------------------------------------------------------------------------------------------------------------------------
        // getting info intent user-receiver from intent put extra user_adapter
        
        
    }
    
    private fun initializeViews()
    {
        
        progressbarImage = findViewById(R.id.progressBarChatActivity)
        messageBox = findViewById(R.id.messageBox)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        senderProfileImageView = findViewById(R.id.imageSenderProfile)
        receiverprofileImageView = findViewById(R.id.imageReceiverProfile)
        btnSendImage = findViewById(R.id.btnSendImage)
        sendButton = findViewById(R.id.sentbutton)
        imgMic = findViewById(R.id.img_microphone1)
        btnSendAudio = findViewById(R.id.btnSendAudio)
        linearLayoutProgressBar = findViewById(R.id.linearLayoutProgressBar)
        pause = findViewById(R.id.pause)
        progressBarRecording = findViewById(R.id.progressBarRecording)
        textView4 = findViewById(R.id.textView4)
        
        
    }
    
    
    private fun setupRecyclerView()
    {
        // Initialize the adapter with the required parameters
        
        // Initialize messageList
        messageList = mutableListOf()
        // Initialize the adapter:last this presente an instance of chatactivity to use
        // as parameter  named activity in adapter it is  instance =activity to use any method from Chatactivity
        //manuelle dependency injection exp :activity.any methode in this class look parameter adapter
        
        messageAdapter = MessageAdapter(
            this, messageList, senderRoomUid, receiverRoomUid, chatRecyclerView, this
                                       )
        // PURETRUBTION OF Chat position item
        /* chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
             stackFromEnd = true  // Ensures new items are added at the end
         }*/
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        
        chatRecyclerView.adapter = messageAdapter
        
        
      
        //Assuming chatRecyclerView is your RecyclerView instance
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)

// Adding spacing decoration
        chatRecyclerView.addItemDecoration(SpaceItemDecoration(spacing))



    
    }
    
    
    //*********************************************************************************************


//******************* fetch data ************************************************************
    
    private fun fetchCurrentUserDetails()
    {
        
        val recViewProfilePictureUrl = intent.getStringExtra("profilePicture").toString()
        loadProfilePicture(receiverprofileImageView, recViewProfilePictureUrl)
        
        // Retrieve the current user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        
        // Check if the current user's UID is not null
        currentUserUid?.let { uid ->
            // Construct the reference to the current user's data under the 'users' node
            val userRef = mDbRef.child("users").child(uid)
            
            // Add a listener to fetch the user's data
            userRef.addListenerForSingleValueEvent(object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    // Get the user object from the dataSnapshot
                    val user = snapshot.getValue(User::class.java)
                    
                    // Check if the user object is not null
                    user?.let { userData ->
                        // Access the user's name
                        val name = userData.name
                        
                        // Set the user's name to the TextView
                        textView4.text = name
                    }
                }
                
                override fun onCancelled(error: DatabaseError)
                {
                    // Handle error
                    Log.e(TAG, "Failed to read user data.", error.toException())
                }
            })
        }
    }
    
    //*****************cliclistener*************************************
    
   
    private fun setupSendOnClickItem()
    {
        
        
        imgMic = findViewById(R.id.img_microphone1)
        
        // Set up click listeners for sender and receiver profile image views
        senderProfileImageView.setOnClickListener {
            openUpdateSenderProfilePictureActivity()
        }
        
        receiverprofileImageView.setOnClickListener {
            openUserReceiverProfileActivity()
        }
        
        // Set up click listener for sending images
        btnSendImage.setOnClickListener {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else
            {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            checkPermissionForImage(permissions) {
                pickGalleryLauncher.launch("image/*")
                
            }
        }
        
        
        // Set up click listener for sending text messages
        
        
        sendButton.setOnClickListener {
            val textMessage = messageBox.text.toString().trim()
            if (textMessage.isEmpty())
            {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show()
                messageBox.requestFocus()
            } else
            {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        sendMessage(TextMessageSender(), textMessage = textMessage)
                    }
                    
                }
            }
        }
        
        
    }
    
    
    private val pickGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null)
            {
                selectedImageUri = uri
                handleImageSelection(uri)
            }
        }
    
    private fun handleImageSelection(imageUri: Uri)
    {
        lifecycleScope.launch {
            progressbarImage.visibility = View.VISIBLE
            // Upload image in background
            val imageUrl = withContext(Dispatchers.IO) {
                uploadImageToStorage(imageUri)
            }
            if (imageUrl != null)
            {
                // Send message in background
                withContext(Dispatchers.IO) {
                    sendMessage(ImageMessageSender(), imageUrl = imageUrl)
                    
                }
                
            } else
            {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChatActivity, "Error uploading image", Toast.LENGTH_SHORT
                                  ).show()
                }
            }
            // Hide progress bar on the main thread
            withContext(Dispatchers.Main) {
                progressbarImage.visibility = View.GONE
            }
        }
    }
    
    
    private suspend fun uploadImageToStorage(imageUri: Uri): String?
    {
        val storage = FirebaseStorage.getInstance()
        val timestamp = System.currentTimeMillis().toString()
        val uniqueFileName = "image_$timestamp.jpg"
        val storageReference = storage.reference.child("image_messages/$uniqueFileName")
        
        return try
        {
            storageReference.putFile(imageUri).await()
            storageReference.downloadUrl.await().toString()
            
            
        } catch (e: Exception)
        {
            Log.e("ChatActivity", "Error uploading image", e)
            null
        }
    }
    
    
    private fun setupMicAndSendAudioButtons()
    {
        
        
        // Initially hide the send audio button and progress bar
        btnSendAudio.visibility = View.GONE
        // linearLayoutProgressBar = findViewById(R.id.linearLayoutProgressBar)
        linearLayoutProgressBar.visibility = View.GONE
        
        imgMic.setOnClickListener {
            checkPermissionForAudio() {
                
                
                startRecording()
            }
        }
        
        
        
        
        
        
        btnSendAudio.setOnClickListener {
            audioUri?.let { uri ->
                lifecycleScope.launch {
                    // Show the ProgressBar and update UI on the main thread
                    withContext(Dispatchers.Main) {
                        progressbarImage.visibility = View.VISIBLE
                        
                    }
                    
                    
                    
                    try
                    {
                        // Upload the audio file in the background
                        val audioUrl = withContext(Dispatchers.IO) {
                            uploadAudioToStorage(uri)
                        }
                        
                        // Check if the upload was successful
                        if (audioUrl == null)
                        {
                            // Handle upload failure on the main thread
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@ChatActivity,
                                    "Error uploading audio",
                                    Toast.LENGTH_SHORT
                                              ).show()
                            }
                        } else
                        {
                            // Send the message in the background if upload was successful
                            withContext(Dispatchers.IO) {
                                sendMessage(AudioMessageSender(), audioUrl = audioUrl)
                            }
                        }
                    } catch (e: Exception)
                    {
                        // Handle any errors that occur during upload
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ChatActivity,
                                "Error uploading audio",
                                Toast.LENGTH_SHORT
                                          ).show()
                        }
                    } finally
                    {
                        // Update UI on the main thread after all operations are complete
                        withContext(Dispatchers.Main) {
                            progressbarImage.visibility = View.GONE
                            imgMic.setImageResource(R.drawable.microphone1)
                            linearLayoutProgressBar.visibility = View.INVISIBLE
                            btnSendAudio.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        
        
    }
    
    
    //*****************cliclistener*************************************
    
    
    //*********************sendMessage **************
//**************fun sotory message *******************
    
  private fun loadInitialMessageHistory()
    {
        listener = object : ValueEventListener
        {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot)
            {
                messageList.clear()
                
                for (messageSnapshot in snapshot.children)
                {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let { messageList.add(it) }
                }
                
                // Sort messages
                messageList.sortBy { it.timestamp }
                //loadInitialMessageHistory(), you use messageAdapter.notifyDataSetChanged() because it reloads the entire message list
                messageAdapter.notifyDataSetChanged()
                // scrollToLastMessage()
                
                
            }
            
            override fun onCancelled(error: DatabaseError)
            {
                // Handle possible errors
            }
        }
        
        messageRefSender.addListenerForSingleValueEvent(listener!!)
        messageRefReceiver.addListenerForSingleValueEvent(listener!!)
    }
    
    
    
    
    //**************fun sotory message *******************
    
    // use loadInitialMessageHistory()
    
    
    //****new sendMessage****
    
    private suspend fun sendMessage(
        sender: MessageSender,
        textMessage: String? = null,
        imageUrl: String? = null,
        audioUrl: String? = null
                                   )
    {
        // Stop listeners before sending a message
        
        //  detachListeners()
        
        
        val message = when
        {
            imageUrl != null -> createMessage(
                textMessage = null, imageUrl = imageUrl, audioUrl = null
                                             )
            
            audioUrl != null -> createMessage(
                textMessage = null, imageUrl = null, audioUrl = audioUrl
                                             )
            
            textMessage != null -> createMessage(
                textMessage = textMessage, imageUrl = null, audioUrl = null
                                                )
            
            else -> null
        }
        
        if (message != null)
        {
            try
            {
                withContext(Dispatchers.Main) {
                    clearTextInput()
                    clearImageInput()
                    clearAudioInput()
                }
                
                // Perform database operation in background
                withContext(Dispatchers.IO) {
                    sender.sendMessage(mDbRef, message)
                    
                    
                }
                
                
                // Notify user and update UI
                withContext(Dispatchers.Main) {
                    
                    
                    // scrollToLastMessage()
                    askNotificationPermission()
                    playNotificationSound()
                }
            } catch (e: Exception)
            {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChatActivity, "Error sending message: ${e.message}", Toast.LENGTH_SHORT
                                  ).show()
                }
            }
        } else
        {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ChatActivity,
                    "Error creating message: user data is null",
                    Toast.LENGTH_SHORT
                              ).show()
            }
        }
    }
    
    private suspend fun createMessage(
        textMessage: String? = null, imageUrl: String? = null, audioUrl: String? = null
                                     ): Message?
    {
        val userTask = mDbRef.child("users").child(senderUid).get().await()
        val user = userTask.getValue(User::class.java)
        return user?.name?.let {
            Message(
                textMessage = textMessage,
                senderUid = senderUid,
                receiverUid = receiverUid,
                senderName = it,
                receiverName = receiverName,
                timestamp = System.currentTimeMillis(),
                imageUrl = imageUrl,
                audioUrl = audioUrl,
                status = "√√",
                viewedByReceiver = false,
                
                
                )
        }
    }
    
    private fun clearAudioInput()
    {
        mediaPlayer?.let {
            if (it.isPlaying)
            {
                it.stop()  // Stop playback if it is currently playing
            }
            it.release()  // Release resources
        }
        mediaPlayer = null  // Clear the MediaPlayer instance
        audioUri = null  // Clear the audio URI
        
    }
    
    
    private fun clearTextInput()
    {
        messageBox.text = null // Clear the text input field
        textMessage = null    // Reset the textMessage variable
    }
    
    
    private fun clearImageInput()
    {
        
        selectedImageUri = null // Reset the selected image URI
    }
    
    
    //********fun for listen to any new message
    
    private fun loadNewMessagesSend()
    {
    
        // Clear the list to ensure it only contains the current data
     
        senderChildEventListener = object : ChildEventListener
        {
            // Clear the list to ensure it only contains the current data
           
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {
             
    
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    if (messageList.none { existingMessage -> existingMessage.messageId == message.messageId })
                    {
                        val index = messageList.binarySearch {
                            compareValues(
                                it.timestamp,
                                message.timestamp
                                         )
                        }
                        val insertIndex = if (index < 0) -(index + 1) else index
                        
                        messageList.add(insertIndex, message)
                        messageAdapter.notifyItemInserted(insertIndex)
                        
                        // If the new message is inserted at the end, scroll to it
                        if (insertIndex == messageList.size - 1)
                        {
                            scrollToLastMessage()
                        } else
                        {
                            // If it's inserted somewhere in the middle, update subsequent items
                            messageAdapter.notifyItemRangeChanged(
                                insertIndex + 1,
                                messageList.size - insertIndex - 1
                                                                 )
                        }
                    }
                }
            }
            
           
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    val index = messageList.indexOfFirst { it.messageId == message.messageId }
                    if (index != -1)
                    {
                        messageList[index] = message
                        
                        messageAdapter.notifyItemChanged(index)
                        
                        if (index == messageList.size - 1)
                        {
                            scrollToLastMessage()
                        } else
                        {
                            // If it's inserted somewhere in the middle, update subsequent items
                            messageAdapter.notifyItemRangeChanged(
                                index + 1,
                                messageList.size - index - 1
                                                                 )
                        }
                    }
                }
            }
            
            
            override fun onChildRemoved(snapshot: DataSnapshot)
            {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    runOnUiThread {
                        deleteMessage(it)
                    }
                }
            }
            
            
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?)
            {
                // Handle if necessary
            }
            
            override fun onCancelled(error: DatabaseError)
            {
                // Handle possible errors
            }
            
        }
        
        messageRefSender.addChildEventListener(senderChildEventListener!!)
        
        
        
        
        
        receiverChildEventListener = object : ChildEventListener
        {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    if (messageList.none { existingMessage -> existingMessage.messageId == message.messageId })
                    {
                        val index = messageList.binarySearch {
                            compareValues(
                                it.timestamp,
                                message.timestamp
                                         )
                        }
                        val insertIndex = if (index < 0) -(index + 1) else index
                        
                        messageList.add(insertIndex, message)
                        messageAdapter.notifyItemInserted(insertIndex)
                        
                        // If the new message is inserted at the end, scroll to it
                        if (insertIndex == messageList.size - 1)
                        {
                            scrollToLastMessage()
                        } else
                        {
                            // If it's inserted somewhere in the middle, update subsequent items
                            messageAdapter.notifyItemRangeChanged(
                                insertIndex + 1,
                                messageList.size - insertIndex - 1
                                                                 )
                        }
                    }
                }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    val index = messageList.indexOfFirst { it.messageId == message.messageId }
                    if (index != -1)
                    {
                        messageList[index] = message
                        
                        messageAdapter.notifyItemChanged(index)
                        
                        if (index == messageList.size - 1)
                        {
                            scrollToLastMessage()
                        } else
                        {
                            // If it's inserted somewhere in the middle, update subsequent items
                            messageAdapter.notifyItemRangeChanged(
                                index + 1,
                                messageList.size - index - 1
                                                                 )
                        }
                    }
                }
            }
            
            override fun onChildRemoved(snapshot: DataSnapshot)
            {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    message.let {
                        // Call the method to handle view update
                        deleteMessage(message)
                    }
                }
            }
            
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?)
            {
                // Handle if necessary
            }
            
            override fun onCancelled(error: DatabaseError)
            {
                // Handle possible errors
            }
            
            
        }
        
        messageRefReceiver.addChildEventListener(receiverChildEventListener!!)
        
        
    }
    
    
    //****new sendMessage****
    // Method in ChatActivity to handle view update after a message is deleted
    private fun deleteMessage(message: Message)
    {
        synchronized(messageList) {
            val index = messageList.indexOfFirst { it.messageId == message.messageId }
            if (index != -1)
            {
                messageList.removeAt(index)
                runOnUiThread {
                    messageAdapter.notifyItemRemoved(index)
                    
                    if (messageList.size > 0)
                    {
                        messageAdapter.notifyItemRangeChanged(index, messageList.size - index)
                    } else
                    {
                        messageAdapter.notifyDataSetChanged()
                        
                    }
                    
                }
            }
        }
    }
    
    // lifecycle methode *****
    override fun onStart()
    {
        super.onStart()
        
        // Detach the initial listener if it was added
        listener?.let {
            messageRefSender.removeEventListener(it)
            messageRefReceiver.removeEventListener(it)
            listener = null
        }
        
        
        loadNewMessagesSend()
        
    }
    
    
   
    private fun scrollToLastMessage()
    {
        chatRecyclerView.post {
            chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }
    
    
    // lifecycle methode *****
    
    
    //**************************************************************************************************************
    
    // permission image and audio ///**********************
    
    
    // Function to check and request audio recording permission
    private fun checkPermissionForAudio(action: () -> Unit)
    {
        val permission = Manifest.permission.RECORD_AUDIO
        val rationaleMessage = "We need audio permission to record audio messages."
        val requestCode = 101
        
        if (ContextCompat.checkSelfPermission(
                this,
                permission
                                             ) != PackageManager.PERMISSION_GRANTED
        )
        {
            // If permission is not granted, store the action to be performed later
            pendingAction = action
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            {
                // Show rationale dialog if needed
                showPermissionRationaleDialog(rationaleMessage) {
                    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
                }
            } else
            {
                // Directly request permission
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        } else
        {
            // If permission is already granted, execute the action immediately
            action()
        }
    }
    
    
    // Function to check and request image access permission
    private fun checkPermissionForImage(permissions: Array<String>, action: () -> Unit)
    {
        val rationaleMessage = "We need storage permission to access images."
        val requestCode = 102
        
        if (permissions.any {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                                                 ) != PackageManager.PERMISSION_GRANTED
            })
        {
            // If permission is not granted, store the action to be performed later
            pendingAction = action
            if (permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) })
            {
                // Show rationale dialog if needed
                showPermissionRationaleDialog(rationaleMessage) {
                    ActivityCompat.requestPermissions(this, permissions, requestCode)
                }
            } else
            {
                // Directly request permission
                ActivityCompat.requestPermissions(this, permissions, requestCode)
            }
        } else
        {
            // If permission is already granted, execute the action immediately
            action()
        }
    }
    
    // Function to show rationale dialog
    private fun showPermissionRationaleDialog(message: String, onPositiveButtonClick: () -> Unit)
    {
        AlertDialog.Builder(this).setMessage(message)
            .setPositiveButton("OK") { _, _ -> onPositiveButtonClick() }
            .setNegativeButton("Cancel", null).create().show()
    }
    
    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
                                           )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == 101 || requestCode == 102)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                pendingAction?.invoke()
            } else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    
    // permission image and audio ///**********************
//*********onCliclistenner**********
    
    
    private fun setActionBarTitle()
    {
        supportActionBar?.title = receiverName
    }
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    
    
    //**************permission for audio message to record begin end  **************************************************************************
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    
    
    // starRecording inside stopRecording
    private fun startRecording()
    {
        val handler = Handler(Looper.getMainLooper())
        val MAX_RECORDING_DURATION = 60000L // 1 mn
        
        
        val timestamp = System.currentTimeMillis()
        filePath = getOutputFilePath(timestamp)
        
        try
        {
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                MediaRecorder(this@ChatActivity) // Use constructor with Context for API 31+
            } else
            {
                MediaRecorder() // Use default constructor for older versions
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(filePath)
                prepare()
                start()
                state = RECORDING_STATE
            }
            
            // UI updates
            imgMic.setImageResource(R.drawable.record_sound)
            linearLayoutProgressBar.visibility = View.VISIBLE
            progressBarRecording.isIndeterminate = true
            btnSendAudio.visibility = View.GONE
            
            // Stop recording after the maximum duration
            handler.postDelayed({
                if (state == RECORDING_STATE)
                {
                    stopRecording()
                }
            }, MAX_RECORDING_DURATION)
            
            // Listen for user input to stop recording
            val pause: ImageButton = findViewById(R.id.pause)
            pause.setOnClickListener {
                
                // Cancel the automatic stop
                
                handler.removeCallbacksAndMessages(null)
                stopRecording()
            }
            
            
        } catch (e: Exception)
        {
            Log.e(TAG, "Error starting recording", e)
            showToast("Error starting recording")
            // Ensure UI is reset if recording fails
            resetRecordingUI()
        }
    }
    
    private fun resetRecordingUI()
    {
        imgMic.setImageResource(R.drawable.microphone1)
        linearLayoutProgressBar.visibility = View.GONE
        progressBarRecording.isIndeterminate = false
        btnSendAudio.visibility = View.GONE
    }
    
    // Update UI elements
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    //stopRecording create audioUri to use in upload fun
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    //stopRecording create audioUri to use in upload fun
    
    
    private fun stopRecording()
    {
        handler.removeCallbacksAndMessages(null) // Clear any pending tasks
        
        
        mediaRecorder?.apply {
            try
            {
                // Stop recording and release resources
                stop()
                release()
                
                // Reset mediaRecorder
                mediaRecorder = null
                
                // Check if filePath is not null
                // Convert audio file path to URI
                // to convert filePath to uri we used abstraction methode from object File
                audioUri = Uri.fromFile(File(filePath))
                
                
                imgMic.setImageResource(R.drawable.ic_stop)
                linearLayoutProgressBar.visibility = View.VISIBLE
                
                progressBarRecording.isIndeterminate = false
                // Make send audio button visible
                btnSendAudio.visibility = View.VISIBLE
                btnSendAudio.setImageResource(R.drawable.sendaudio)  // Ensure 'sendaudio' drawable is correct
                
                
                
                // Show success message on the main thread
              //  showToast("Audio recording stopped. Ready to upload.")
                
            } catch (e: Exception)
            {
                Log.e(TAG, "Error stopping recording or uploading audio", e)
                showToast("Error stopping recording or uploading audio")
            }
        }
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // create out put file path
    private fun getOutputFilePath(timestamp: Long): String
    {
        val uniqueFileName = "audio_$timestamp.mp3"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File(storageDir, uniqueFileName).absolutePath
    }
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // use audioUri create on stopRecording to sstorge in storge firbase and get audioUrl to use in send-message audio
    
    
    private suspend fun uploadAudioToStorage(audioUri: Uri): String?
    {
        val storage = FirebaseStorage.getInstance()
        val timestamp = System.currentTimeMillis().toString()
        val uniqueFileName = "audio_$timestamp.mp3"
        val storageReference = storage.reference.child("audio_messages/$uniqueFileName")
        
        return try
        {
            storageReference.putFile(audioUri).await()
            storageReference.downloadUrl.await().toString()
        } catch (e: Exception)
        {
            Log.e("ChatActivity", "Error uploading audio", e)
            null
        }
    }
    
    
    private fun playNotificationSound()
    {
        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = MediaPlayer.create(this, notificationSoundUri)
        mediaPlayer?.start()
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // create activity info for user receiver from his profile picutre
    
    private fun openUserReceiverProfileActivity()
    {
        val receiverEmail: String = intent.getStringExtra("email").toString()
        val receiverProfilePicture: String = intent.getStringExtra("profilePicture").toString()
        val intent = Intent(this, UserReceiverProfile::class.java).apply {
            putExtra("uid", receiverUid)
            putExtra("name", receiverName)
            putExtra("profilePicture", receiverProfilePicture)
            putExtra("email", receiverEmail)
        }
        startActivity(intent)
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // create activity info for user sender from his profile picutre
    
    private fun openUpdateSenderProfilePictureActivity()
    {
        val intent = Intent(this, UpdateSenderProfile::class.java)
        intent.putExtra("userUid", senderUid)
        startActivity(intent)
    }
    
    
    private fun updateFcmToken()
    {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val token = task.result
                    FirebaseDatabase.getInstance().getReference("users").child(uid)
                        .child("fcmToken").setValue(token)
                }
            }
        }
    }
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // Function to show a Toast message
    private fun showToast(message: String)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        // This assumes you're working within an Activity or Fragment
        // Adjust the context accordingly if needed
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    
    //**************permission for notification begin   **************************************************************************
    
    //permission for notification
    
    fun askNotificationPermission()
    {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                                                 ) == PackageManager.PERMISSION_GRANTED
            )
            {
                // Permission is already granted
                CoroutineScope(Dispatchers.Main).launch {
                    sendNotification()
                }
            } else
            {
                // Permission not granted, check if rationale should be shown
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
                {
                    showRationaleDialog()
                } else
                {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else
        {
            // For API levels below 33, no need to handle notification permission
            CoroutineScope(Dispatchers.Main).launch {
                sendNotification()
            }
        }
    }
    
    
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun showRationaleDialog()
    {
        
        AlertDialog.Builder(applicationContext).setTitle("Permission Required")
            .setMessage("To send notifications, please grant the notification permission.")
            .setPositiveButton("OK") { _, _ ->
                
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            
            .setNegativeButton("No thanks") { _, _ ->
                Log.d(
                    "fcm", "User declined notification permission. Unable to send the message."
                     )
                Toast.makeText(
                    applicationContext,
                    "Notification permission denied. Unable to send the message.",
                    Toast.LENGTH_SHORT
                              ).show()
            }.show()
    }
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // denied permission
    fun handleNotificationPermissionDenied()
    {
        Log.d(TAG, "Notification permission denied. Unable to send the message.")
        Toast.makeText(
            applicationContext,
            "Notification permission denied. Unable to send the message.",
            Toast.LENGTH_SHORT
                      ).show()
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // send notificatio
    private fun sendNotification()
    {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        
        userId?.let {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(it)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    if (snapshot.exists())
                    {
                        val userName = snapshot.child("name").getValue(String::class.java)
                        CoroutineScope(Dispatchers.Main).launch {
                            if (userName != null)
                            {
                                val receiverFcmToken = intent.getStringExtra("fcmToken")
                                // Print the FCM token to the console
                                Log.d("fcm", "Receiver FCM Token: $receiverFcmToken")
                                
                                val body = "you have message"
                                if (receiverFcmToken != null)
                                {
                                    val notificationData = NotificationData(userName, body)
                                    val pushNotification =
                                        PushNotification(notificationData, receiverFcmToken)
                                    sendFcmMessage(
                                        pushNotification
                                                  )
                                } else
                                {
                                    Log.e(
                                        TAG,
                                        "Receiver FCM Token is null or empty. Unable to send the notification."
                                         )
                                    // Handle the case where the FCM token is null or empty
                                }
                                
                                
                            }
                        }
                    }
                }
                
                
                override fun onCancelled(error: DatabaseError)
                {
                    // Handle onCancelled if needed
                }
            })
        }
    }
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // fcm Message ()
    
    suspend fun sendFcmMessage(
        pushNotification: PushNotification
                              )
    {
        
        
        val sendMessageNotification = RetrofitInstance.api
        
        try
        {
            
            sendMessageNotification.postNotification(pushNotification)
            
            
        } catch (e: Exception)
        {
            // Handle exceptions
            Log.e(TAG, "Error sending FCM message: ${e.message}")
        }
    }
    
    //----------------------------------------------------------------------------------------------------
    //load update profile picture for sender
    
    fun loadProfilePicture(imageView: ImageView, imageUrl: String? = null)
    {
        Glide.with(this).load(imageUrl).circleCrop().placeholder(R.drawable.profile)
            .error(R.drawable.profile).into(imageView)
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------
    // update profile picture from data base
 
    private fun loadSenderProfilePicture() {
        val userReference = FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userProfile = snapshot.getValue(User::class.java)
                    val profilePictureUrl = userProfile?.profilePicture
                    
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        loadProfilePicture(senderProfileImageView, profilePictureUrl)
                    } else {
                        senderProfileImageView.setImageResource(R.drawable.profile)
                    }
                } else {
                    // Handle the case where user data doesn't exist
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle database error if necessary
            }
        })
    }
    
    
    
}







