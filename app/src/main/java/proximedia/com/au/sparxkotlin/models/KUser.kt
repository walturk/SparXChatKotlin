package proximedia.com.au.sparxkotlin.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Created by pc on 16/10/2017.
 */

@IgnoreExtraProperties
class KUser() : Parcelable {

    var id: String? = ""
    var name: String? = null
    var age: Int? = null
    var email: String? = null
    var password: String? = null
    var location: GeoPoint? = null
    var activeprofile: Int? = 0
    var online: Boolean? = false
    var deviceId: String? = null
    var profiles: ArrayList<Profile>? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        age = parcel.readValue(Int::class.java.classLoader) as? Int
        email = parcel.readString()
        password = parcel.readString()
        activeprofile = parcel.readValue(Int::class.java.classLoader) as? Int
        online = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        deviceId = parcel.readString()
    }

    constructor(id: String, name: String, age: Int, email: String, password: String, location: GeoPoint, activeprofile: Int,
                online: Boolean, deviceId: String, profiles: ArrayList<Profile>) : this() {
        this.id = id
        this.name = name
        this.age = age
        this.email = email
        this.password = password
        this.location = location
        this.activeprofile = activeprofile
        this.online = online
        this.deviceId = deviceId
        this.profiles = profiles
    }



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeValue(age)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeValue(activeprofile)
        parcel.writeValue(online)
        parcel.writeString(deviceId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KUser> {
        override fun createFromParcel(parcel: Parcel): KUser {
            return KUser(parcel)
        }

        override fun newArray(size: Int): Array<KUser?> {
            return arrayOfNulls(size)
        }
    }
}