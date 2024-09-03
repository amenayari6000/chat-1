package com.walid591.chat.messages_senders

import com.google.firebase.database.DatabaseReference
import com.walid591.chat.models.Message

abstract class MessageSender
{
    
    // Base class
    
    
    abstract suspend fun sendMessage(mDbRef: DatabaseReference, message: Message)
}