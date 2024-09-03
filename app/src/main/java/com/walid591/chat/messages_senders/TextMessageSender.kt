package com.walid591.chat.messages_senders

import com.google.firebase.database.DatabaseReference
import com.walid591.chat.models.Message
import com.walid591.chat.utlilities.sendToDatabase

// TextMessageSender class
class TextMessageSender : MessageSender()
{
    override suspend fun sendMessage(mDbRef: DatabaseReference, message: Message)
    {
        // Implement sending logic for text messages
        sendToDatabase(mDbRef, message)
    }
}