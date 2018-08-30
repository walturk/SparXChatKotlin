package proximedia.com.au.sparxkotlin.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser

/**
 * Created by pc on 4/10/2017.
 */

class User() : Any(), Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

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


}