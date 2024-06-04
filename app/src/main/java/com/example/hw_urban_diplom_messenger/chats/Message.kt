package com.example.hw_urban_diplom_messenger.chats

//class Message(var id: String, var ownerId: String, var text: String)

data class Message(var id: String, var ownerId: String, var text: String, var imageUri: String? = null)