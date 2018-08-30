package proximedia.com.au.sparxkotlin.utils

import com.google.firebase.iid.FirebaseInstanceIdService
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import proximedia.com.au.sparxkotlin.SXApp
import proximedia.com.au.sparxkotlin.constants.AppConstants


/**
 * Created by pc on 7/10/2017.
 */

class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {

    val TAG = MyFirebaseInstanceIdService::class.java.simpleName!!

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(AppConstants.TAG, "Refreshed token: $refreshedToken - send this to server?")
        // we want to send messages to this application instance and manage this apps subscriptions on the server side
        // so now send the Instance ID token to the app server
        refreshedToken?.let {
            sendRegistrationToServer(it)
        }
        
    }

    private fun sendRegistrationToServer(refreshedToken: String) {
        Log.d(TAG, "Refreshed token: $refreshedToken - send this to server?")
    }
}