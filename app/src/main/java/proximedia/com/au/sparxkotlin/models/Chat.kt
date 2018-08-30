package proximedia.com.au.sparxkotlin.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Created by pc on 24/10/2017.
 */

class Chat {

    var name: String? = null
    var message: String? = null
    var uid: String? = null
    var profile : Int = 0
    @get:ServerTimestamp
    var timestamp: Date? = null

    constructor() {} // Needed for Firebase

    constructor(name: String, message: String, uid: String, profile: Int) {
        this.name = name
        this.message = message
        this.uid = uid
        this.profile = profile
    }
}