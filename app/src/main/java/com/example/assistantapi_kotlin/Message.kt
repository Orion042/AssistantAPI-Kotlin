package com.example.assistantapi_kotlin

import android.os.Parcel
import android.os.Parcelable

class Message(
    private val messageId: String,
    private val message: String,
    private val sender: User,
    private val createdAt: String
) : Parcelable {
    var mMessageId = messageId
    var mMessage = message
    var mSender = sender
    var mCreatedAt = createdAt

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(User::class.java.classLoader) ?: User("", ""),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mMessageId)
        parcel.writeString(mMessage)
        parcel.writeParcelable(mSender, flags)
        parcel.writeString(mCreatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }

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