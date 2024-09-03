package com.walid591.chat.models


class User
{
    
    
    var name: String? = null
    var profilePicture: String? = null
    var email: String? = null
    var uid: String? = null
    var fcmToken: String? = null
    
    var online: Boolean = true
    
    
    constructor()
    
    
    // Updated constructor
    constructor(
        name: String?,
        uid: String?,
        email: String?,
        profilePicture: String?,
        fcmToken: String?,
        online: Boolean // Parameter matches the property name
               )
    {
        this.name = name
        this.uid = uid
        this.email = email
        this.profilePicture = profilePicture
        this.fcmToken = fcmToken
        this.online = online // Assignment is correct
    }
}