package com.example.assistantapi_kotlin

import android.os.Parcel
import android.os.Parcelable

class User(private val userId: String, private val nickname: String) : Parcelable {
    private val mUserId = userId
    private val mNickname = nickname

    // Parcelableインターフェースの実装
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mUserId)
        parcel.writeString(mNickname)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
    fun getUserId(): String {
        return mUserId
    }

    fun getNickname(): String {
        return mNickname
    }
}