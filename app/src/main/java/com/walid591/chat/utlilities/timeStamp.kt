package com.walid591.chat.utlilities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    // Create a Date object from the timestamp
    val date = Date(timestamp)
    
    // Define the format you want to display the date in
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    
    // Return the formatted date string
    return dateFormat.format(date)
}
