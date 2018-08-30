package proximedia.com.au.sparxkotlin.common

import com.google.firebase.auth.FirebaseUser
import proximedia.com.au.sparxkotlin.models.KUser

/**
 * Created by pc on 28/10/2017.
 */

object SessionManager {
    var userId = "pj1hrgpECZe97n7wb6EZ"
    var currentUser : FirebaseUser? = null
    var currentKUser : KUser? = null
    var deviceToken : String? = null
    var activeProfile : Int? = 0

}