package com.example.hw_urban_diplom_messenger.users

data class User(var name: String = "", var profileImageUri: String = "") {
    constructor() : this("", "")
}