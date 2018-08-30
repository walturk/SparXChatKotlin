package proximedia.com.au.sparxkotlin.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.utils.RxBus
import proximedia.com.au.sparxkotlin.models.Chat

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class ChatService : IntentService("ChatService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {

            /*val dbUsersDocRef = FirebaseFirestore.getInstance().document("Users/"+ SessionManager.currentUser!!.uid)
            val chatsRef = dbUsersDocRef.collection("Chats").orderBy("timestamp", Query.Direction.ASCENDING)
            */

            val dbUsersDocRef = FirebaseFirestore.getInstance().document("Users/ZA8dYnNDt8eX3oxrlbJS9TEIlrI3")
            val chatsRef = dbUsersDocRef.collection("Wx08F5xMBHUcMgeQyjYWVq1xYIY2").orderBy("timestamp", Query.Direction.ASCENDING)

            //ZA8dYnNDt8eX3oxrlbJS9TEIlrI3
            //Wx08F5xMBHUcMgeQyjYWVq1xYIY2
            chatsRef.addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(snapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.w(AppConstants.TAG, "listen:error", e)
                        return
                    }

                    for (dc in snapshots?.documentChanges!!) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.d(AppConstants.TAG, "New: ${dc.document.id} | " + dc.document.data)
                                RxBus.publish(dc.document.toObject(Chat::class.java))
                            }
                            DocumentChange.Type.MODIFIED -> {

                                Log.d(AppConstants.TAG, "Modified: ${dc.document.id} | " + dc.document.data)


                            }
                            DocumentChange.Type.REMOVED -> Log.d(AppConstants.TAG, "Removed: ${dc.document.id} | " + dc.document.data)
                        }
                    }

                }


            })
            /*val action = intent.action
            if (ACTION_FOO == action) {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionFoo(param1, param2)
            } else if (ACTION_BAZ == action) {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionBaz(param1, param2)
            }*/
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFoo(param1: String, param2: String) {
        // TODO: Handle action Foo
        throw UnsupportedOperationException("Not yet implemented")
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        // TODO: Handle action Baz
        throw UnsupportedOperationException("Not yet implemented")
    }

    companion object {
        // TODO: Rename actions, choose action names that describe tasks that this
        // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
        private val ACTION_FOO = "proximedia.com.au.sparxkotlin.services.action.FOO"
        private val ACTION_BAZ = "proximedia.com.au.sparxkotlin.services.action.BAZ"

        // TODO: Rename parameters
        private val EXTRA_PARAM1 = "proximedia.com.au.sparxkotlin.services.extra.PARAM1"
        private val EXTRA_PARAM2 = "proximedia.com.au.sparxkotlin.services.extra.PARAM2"

        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        fun startActionFoo(context: Context, param1: String, param2: String) {
            val intent = Intent(context, ChatService::class.java)
            intent.action = ACTION_FOO
            intent.putExtra(EXTRA_PARAM1, param1)
            intent.putExtra(EXTRA_PARAM2, param2)
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        fun startActionBaz(context: Context, param1: String, param2: String) {
            val intent = Intent(context, ChatService::class.java)
            intent.action = ACTION_BAZ
            intent.putExtra(EXTRA_PARAM1, param1)
            intent.putExtra(EXTRA_PARAM2, param2)
            context.startService(intent)
        }
    }


}
