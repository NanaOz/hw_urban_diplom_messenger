package com.example.hw_urban_diplom_messenger.chats

data class Chat(
    var chatId: String = "",
    var chatName: String = "",
    var userId1: String = "",
    var userId2: String = ""
) {
    constructor() : this("", "", "", "")
}