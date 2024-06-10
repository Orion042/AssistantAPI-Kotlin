package com.example.assistantapi_kotlin

class Message constructor(private val messageId: String, private val message: String, private val sender: User, private val createdAt: String) {
    var mMessageId = messageId
    var mMessage = message
    var mSender = sender
    var mCreatedAt = createdAt

    fun getMessageId(): String {
        return mMessageId
    }
    fun getMessage(): String {
        return mMessage
    }
    fun getSender(): User {
        return mSender
    }
    fun getCreatedAt(): String {
        return mCreatedAt
    }
}