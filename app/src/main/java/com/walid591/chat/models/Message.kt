package com.walid591.chat.models

import androidx.media3.exoplayer.ExoPlayer


class Message {

    var textMessage: String? = null
    var senderUid: String? = null
    var receiverUid: String? = null
    var senderName: String? = null
    var receiverName: String? = null
    var timestamp: Long = 0
    var imageUrl: String? = null
    var audioUrl: String? = null
    var status :String?=null
    var viewedByReceiver: Boolean = false
    var messageId: String? = null
    var exoPlayer: ExoPlayer? = null
    
    constructor()


    constructor(textMessage: String?, senderUid: String?, receiverUid: String?, senderName: String?, receiverName: String?, timestamp: Long, imageUrl: String?, audioUrl: String?, status: String?, viewedByReceiver:Boolean) {
        this.textMessage = textMessage
        this.senderUid = senderUid
        this.receiverUid = receiverUid
        this.senderName = senderName
        this.receiverName = receiverName
        this.timestamp = timestamp
        this.imageUrl = imageUrl
        this.audioUrl=audioUrl
        this.status=status
        this.viewedByReceiver = viewedByReceiver
        this.messageId = messageId
        this.exoPlayer=exoPlayer

    }


}



