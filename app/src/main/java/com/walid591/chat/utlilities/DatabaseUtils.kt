package com.walid591.chat.utlilities

import com.google.firebase.database.DatabaseReference
import com.walid591.chat.models.Message

import kotlinx.coroutines.tasks.await


suspend fun sendToDatabase(mDbRef: DatabaseReference, message: Message)
{
    val senderRoomUid = "${message.senderUid}_${message.receiverUid}"
    val receiverRoomUid = "${message.receiverUid}_${message.senderUid}"
    
    
    // Generate a unique key for the message
    val senderMessageRef = mDbRef.child("chats").child(senderRoomUid).child("messages").push()
    message.messageId = senderMessageRef.key
    
    
    // Save the sender's message
    senderMessageRef.setValue(message).await()
    
    // Save the receiver's message
    val receiverMessageRef =
        mDbRef.child("chats").child(receiverRoomUid).child("messages").child(senderMessageRef.key!!)
    receiverMessageRef.setValue(message).await()
    
    
}
