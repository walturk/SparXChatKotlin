package proximedia.com.au.sparxkotlin.common

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import proximedia.com.au.sparxkotlin.R
import proximedia.com.au.sparxkotlin.common.SessionManager
import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.models.KUser
import proximedia.com.au.sparxkotlin.services.ChatService


/**
 * Created by pc on 28/10/2017.
 */

class AuthActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private var mCustomToken : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d(AppConstants.TAG, "mAuth: " )
        mAuth = FirebaseAuth.getInstance()

        startSignIn()

        /*button_sign_in.isEnabled = true
        button_sign_in.setOnClickListener( View.OnClickListener {
            startSignIn()

        })*/

    }

    private fun startSignIn() {

        progressBar.setVisibility(View.VISIBLE)

        val mToken = FirebaseInstanceId.getInstance().token
        SessionManager.deviceToken = mToken

        Log.d(AppConstants.TAG, "signInWithCustomToken: ${mToken}")
        /*if (mToken != null && mToken.length > 0) {
            mAuth.signInWithCustomToken(mToken)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(AppConstants.TAG, "signInWithCustomToken:success")
                            val user = mAuth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(AppConstants.TAG, "signInWithCustomToken:failure", task.exception)
                            Toast.makeText(this@AuthActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
        }

        else {*/
            // signin with email and password

            val email = user_email.text.toString()
            val password = user_password.text.toString()

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@AuthActivity, OnCompleteListener<AuthResult> { task ->
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressBar.setVisibility(View.GONE)
                        if (!task.isSuccessful) {
                            // there was an error
                            if (password.toString().length < 6) {
                                //inputPassword.setError(getString(R.string.minimum_password))
                            } else {
                                Toast.makeText(this@AuthActivity, getString(R.string.report_issue_login_failed), Toast.LENGTH_LONG).show()
                                Log.d(AppConstants.TAG, "signInWithEmailAndPassword: Failed")
                            }
                        } else {
                            SessionManager.currentUser = mAuth.currentUser!!
                            val user = mAuth.currentUser!!

                            getCurrentKUser()
                            Log.d(AppConstants.TAG, "signInWithEmailAndPassword: ${mAuth.currentUser?.uid}")

                            if (SessionManager.currentUser != null) {
                                val chatIntent = Intent()
                                chatIntent.setClass(this, ChatService::class.java)
                                startService(chatIntent)
                            }



                        }
                    })
    }

    private fun getCurrentKUser() {

        // Access a Cloud Firestore instance from your Activity
        val fireStore = FirebaseFirestore.getInstance()
        val fireStoreUsers = fireStore.collection("Users")

        val dbRefCurrentUserDoc = fireStoreUsers.document(SessionManager.currentUser!!.uid)
        dbRefCurrentUserDoc.get().addOnSuccessListener { documentSnapshot ->

            if (documentSnapshot.exists()) {

                val kuser = documentSnapshot.toObject(KUser::class.java)
                SessionManager.currentKUser = kuser
                SessionManager.currentKUser!!.id = documentSnapshot.id


                if (kuser?.profiles != null && kuser.profiles!!.size > 0) {
                    val myWidth = 512
                    val myHeight = 384

                    val target = object : SimpleTarget<Bitmap>(myWidth, myHeight) {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }
                    }

                    val activeProfile = kuser.profiles!![kuser.activeprofile!!]
                    Glide.with(this).asBitmap().load(activeProfile.photo).into(target)
                }

                val intent = Intent(this@AuthActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

                Log.d(AppConstants.TAG, "id: ${documentSnapshot.id}, name: ${kuser?.name}, age: ${kuser?.age}, profile1: ${kuser?.profiles?.get(0)?.title}" )
            } else {
                Log.d(AppConstants.TAG, "not exist")
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Log.d(AppConstants.TAG, "uid:  ${user.uid}" )
            text_sign_in_status.text = "User ID: ${user.uid}"

        } else {
            Log.d(AppConstants.TAG, "Error:" )
            text_sign_in_status.text = "Error"
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        Log.d(AppConstants.TAG, "onStart: " )

        val currentUser = mAuth.getCurrentUser()
        updateUI(currentUser)
    }

}