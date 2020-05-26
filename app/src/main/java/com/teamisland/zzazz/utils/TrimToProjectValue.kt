//"""
//Descriptions:
//    we have to get duration and uri parse from trim activity to project activity
//    functions should be overridden like describeContetns
//"""

package com.teamisland.zzazz.utils

import android.os.Parcel
import android.os.Parcelable

class TrimToProjectValue(var duration: Int, var uri: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(duration)
        parcel.writeString(uri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrimToProjectValue> {
        override fun createFromParcel(parcel: Parcel): TrimToProjectValue {
            return TrimToProjectValue(parcel)
        }

        override fun newArray(size: Int): Array<TrimToProjectValue?> {
            return arrayOfNulls(size)
        }
    }
}