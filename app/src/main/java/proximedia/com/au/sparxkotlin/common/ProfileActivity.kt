package proximedia.com.au.sparxkotlin.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_profile.*
import proximedia.com.au.sparxkotlin.R
import proximedia.com.au.sparxkotlin.common.SessionManager
import proximedia.com.au.sparxkotlin.constants.AppConstants
import java.io.ByteArrayOutputStream

class ProfileActivity : Activity() {
    // PICK_PHOTO_CODE is a constant integer
    val PICK_PHOTO_CODE = 1046
    var profileNo = -1

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            val photoUri = data.data
            // Do something with the photo based on Uri
            val selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            // Load the selected image into a preview
            header_cover_image.setImageBitmap(selectedImage)

            //gs://sparxkotlin-ec645.appspot.com/spark_profile_images

            // Get spark_profile_images Storage bucket
            val storage = FirebaseStorage.getInstance("gs://sparxkotlin-ec645.appspot.com")

            val fileName = SessionManager.currentUser!!.uid + "_" + profileNo + ".png"

            val profileImageStorageRef = storage.getReference("spark_profile_images").child(fileName)

            header_cover_image.setDrawingCacheEnabled(true)

            header_cover_image.buildDrawingCache()

            val bitmap = header_cover_image.getDrawingCache()

            val baos = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

            val data = baos.toByteArray()

            val uploadTask : UploadTask = profileImageStorageRef.putBytes(data)

            uploadTask.addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    //Log.d(AppConstants.TAG, "upload : ${task.isSuccessful}")

                } else {


                }

                Log.d(AppConstants.TAG, "upload : ${task.isSuccessful}")

            }



        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileNo = intent.getIntExtra("profileNo", -1)

        if (profileNo == -1) {
            return
        }

        /*add_profile_image.setOnClickListener() {

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(packageManager) != null) {

                startActivityForResult(intent, PICK_PHOTO_CODE)

            }
        }*/
    }

    companion object {

        private val INTENT_USER_PROFILENO = "profileNo"

        fun newIntent(context: Context, profileNo: Int): Intent {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(INTENT_USER_PROFILENO, profileNo)
            return intent
        }
    }

}
