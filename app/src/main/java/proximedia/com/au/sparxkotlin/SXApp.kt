package proximedia.com.au.sparxkotlin

import android.app.Application
import android.content.Context
import android.content.Intent
import android.support.multidex.MultiDex
import android.util.Log
import com.bumptech.glide.request.target.ViewTarget

import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.utils.MyFirebaseInstanceIdService

/**
 * Created by pc on 21/09/2017.
 */

public class SXApp : Application()
{
    public val TAG = "SXAPP"
    val context : Context? = this

    override fun onCreate() {
        super.onCreate()
        //ViewTarget.setTagId(R.id.g    lide_request)

        val i : Intent? = Intent( context, MyFirebaseInstanceIdService::class.java)
        if (i != null) {

            if (context != null) {
                Log.d(AppConstants.TAG, "Starting service")
                context.startService(i)
            } else
            {
                Log.d(AppConstants.TAG, "context = null")
            }
        }




    }

    protected override fun attachBaseContext(paramContext: Context) {
        super.attachBaseContext(paramContext)
        MultiDex.install(this)
    }

}
