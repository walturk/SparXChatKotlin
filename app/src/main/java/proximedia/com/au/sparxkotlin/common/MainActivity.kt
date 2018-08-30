package proximedia.com.au.sparxkotlin.common

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*
import proximedia.com.au.sparxkotlin.R
import proximedia.com.au.sparxkotlin.R.id.*
import proximedia.com.au.sparxkotlin.R.layout.activity_main
import proximedia.com.au.sparxkotlin.R.string.*
import proximedia.com.au.sparxkotlin.common.SessionManager.currentKUser
import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.constants.AppConstants.ACTIVATE_PROFILE
import proximedia.com.au.sparxkotlin.constants.AppConstants.DELETE_PROFILE
import proximedia.com.au.sparxkotlin.constants.AppConstants.EDIT_PROFILE
import proximedia.com.au.sparxkotlin.fragments.MyProfilesFragment
import proximedia.com.au.sparxkotlin.fragments.NearbyCardsFragment
import proximedia.com.au.sparxkotlin.models.KUser
import proximedia.com.au.sparxkotlin.utils.CardsRecyclerViewAdapter
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

//If using emulator then send the location from Emulator's Extended Controls. :)
class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, NearbyCardsFragment.OnFragmentInteractionListener, MyProfilesFragment.OnListFragmentInteractionListener,
        ResultCallback<LocationSettingsResult>, GeoQueryEventListener {



    //If using emulator then send the location from Emulator's Extended Controls. :)
    override fun onConnected(p0: Bundle?) {
        Log.d(AppConstants.TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.

        //step 2
        //createLocationRequest();
        //step 3
        //buildLocationSettingsRequest();

        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION);
            } else {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date());
                Log.d(AppConstants.TAG, "mCurrentLocation: ${mCurrentLocation.toString()}")
                runOnUiThread {
                    //updateLocationUI(); TODO UPDATE LOCATION ON SCREEN
                }
            }
        }
    }

    //If using emulator then send the location from Emulator's Extended Controls. :)
    override fun onLocationChanged(location: Location?) {

        Log.d(AppConstants.TAG, "onLocationChanged [${c}, ${r}]: ${location.toString()}")
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(Date());

        val gl = GeoLocation(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
        this.geoQuery?.setCenter(gl)

        /*runOnUiThread {
        updateLocationUI();
        //Toast.makeText(this, "Location updated ${location.toString()}", Toast.LENGTH_SHORT).show()
    }*/
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.d(AppConstants.TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode())

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onResult(locationSettingsResult: LocationSettingsResult) {

        val status: Status = locationSettingsResult.getStatus();
        Log.d(AppConstants.TAG, "Status: ${status}");

        when (status.getStatusCode()) {
            LocationSettingsStatusCodes.SUCCESS -> {
                Log.d(AppConstants.TAG, "All location settings are satisfied.");
                //If using emulator then send the location from Emulator's Extended Controls. :)
                Toast.makeText(this, "Location is already on.", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
            }

            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                Log.d(AppConstants.TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    runOnUiThread {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        Toast.makeText(this, "Location dialog will be open", Toast.LENGTH_SHORT).show();

                        //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                        status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                    }
                } catch (e: IntentSender.SendIntentException) {
                    Log.d(AppConstants.TAG, "PendingIntent unable to execute request.");
                }
            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                Log.d(AppConstants.TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
            }
        }
    }



    override fun onListFragmentInteraction(profileNo: Int, action: Int) {
        Log.d(AppConstants.TAG, "onListFragmentInteraction: item = ${profileNo}, ${action}")

        performProfileAction(profileNo, action)

    }


    override fun onFragmentInteraction(uri: Uri) {
        Log.d(AppConstants.TAG, "uri = ${uri.authority}")
    }

    override fun onGeoQueryReady() {
        Log.d(AppConstants.TAG, "onGeoQueryReady: ")

    }

    override fun onKeyEntered(key: String, location: GeoLocation?) {

        getUserByKey(key)
        Log.d(AppConstants.TAG, "Key entered: ${key}")
    }

    fun performProfileAction(profileNo: Int, action: Int) {

        when (action) {

            ACTIVATE_PROFILE -> {

                val profileRef = fireStoreUsers!!.document(SessionManager.currentUser!!.uid)

                profileRef.update("activeprofile", profileNo).addOnCompleteListener() { task ->

                    if (task.isSuccessful) {

                        updateProfileUI(profileNo)

                        Log.d(AppConstants.TAG, "currentKUser!!.activeprofile : ${currentKUser!!.activeprofile}")

                    } else {
                        Log.d(AppConstants.TAG, "task success: ${task.isSuccessful}")

                    }

                }

            }

            EDIT_PROFILE -> {

                val intent = ProfileActivity.newIntent(this, profileNo)
                startActivity(intent)


            }

            DELETE_PROFILE -> {


            }

            else -> Log.d(AppConstants.TAG, "no action")
        }
    }

    fun updateProfileUI(profileNo: Int) {
        currentKUser!!.activeprofile = profileNo

        // change profile image
        Glide.with(profile_image.getContext())
                .load(currentKUser!!.profiles!![profileNo].photo)
                .into(profile_image)

        // change profile title
        active_user_name.setText(currentKUser!!.profiles!![profileNo].title)

    }

    fun getUserByKey(key: String) {

        val doc = fireStoreUsers?.document(key) as DocumentReference

        doc.get().addOnSuccessListener { documentSnapshot ->

            if (documentSnapshot.exists()) {

                val kuser = documentSnapshot.toObject(KUser::class.java)

                if (mUserCards != null) {
                    kuser!!.id = documentSnapshot.id
                    if (kuser!!.id != SessionManager.currentUser!!.uid) {
                        mUserCards.add(kuser)
                        Log.d(AppConstants.TAG, "mUserCards size= ${mUserCards.size}")
                        mCardsAdapter?.notifyDataSetChanged()
                    }

                } else {
                    Log.d(AppConstants.TAG, "mUserCards: null")
                }
                Log.d(AppConstants.TAG, "name: ${kuser!!.name}, age: ${kuser!!.age}, profile1: ${kuser.profiles?.get(0)?.title}, mUserCards: ${mUserCards.size}")
            } else {
                Log.d(AppConstants.TAG, "not exist")
            }
        }
    }

    override fun onKeyMoved(key: String?, location: GeoLocation?) {
        Log.d(AppConstants.TAG, "onKeyMoved: ${key}")

    }

    override fun onKeyExited(key: String?) {
        Log.d(AppConstants.TAG, "onKeyExited: ${key}")
    }

    override fun onGeoQueryError(error: DatabaseError?) {
        Log.d(AppConstants.TAG, "Key error: ${error}")
    }

    //Ui widgets
    val context: Context? = this
    private var thisUser: KUser? = null
    private var btn_location: Button? = null;
    private var txt_location: TextView? = null;
    private var mLatitudeTextView: TextView? = null;
    private var mLongitudeTextView: TextView? = null;
    private var mLastUpdateTimeTextView: TextView? = null;
    //New views for city and pincode
    private var tv_pincode: TextView? = null;
    private var tv_city: TextView? = null;

    //Any random number you can take
    val REQUEST_PERMISSION_LOCATION: Int = 10;

    // GeoFire:
    private val GEO_FIRE_REF = "geofire"
    private val GEO_FIRE_INITIAL_CENTER = GeoLocation(-33.751130, 150.833901)
    private val GEO_RADIUS = 10.0
    private var geoQuery: GeoQuery? = null
    private var c: Int = 0
    private var r: Double = 1.0
    var fireStore: FirebaseFirestore? = null
    var fireStoreUsers: CollectionReference? = null
    val mQuery: Query? = null

    var mUserCards = ArrayList<KUser>()
    var mCardsAdapter: CardsRecyclerViewAdapter? = null

    // --

    /**
     * Constant used in the location settings dialog.
     */
     val REQUEST_CHECK_SETTINGS: Int = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
     final val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
     final val KEY_LOCATION = "location";
     final val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides the entry point to Google Play services.
     */

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
     var mLocationRequest: LocationRequest? = null

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
     var mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Represents a geographical location.
     */
     var mCurrentLocation: Location? = null
    var mGoogleApiClient: GoogleApiClient? = null

    // Labels.
     var mLatitudeLabel: String = "";
     var mLongitudeLabel: String = "";
     var mLastUpdateTimeLabel: String = ""

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
     var mRequestingLocationUpdates: Boolean = false

    /**
     * Time when the location was updated represented as a String.
     */

     var mLastUpdateTime: String = ""

    var RQS_GooglePlayServices = 0;

    // Firebase

    // --Firebase


    fun updateUI(cu: FirebaseUser?) {

        Log.d(AppConstants.TAG, "user: ${cu}")

    }

    override fun onResume() {
        super.onResume()
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        /*Log.d(AppConstants.TAG, "onResume")
        if (mGoogleApiClient?.isConnected() == true && mRequestingLocationUpdates) {
            //  Toast.makeText(FusedLocationWithSettingsDialog.this, "location was already on so detecting location now", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        } else {
            Log.d(AppConstants.TAG, "mGoogleApiClient not connected")
        }*/


    }

    override fun onPause() {
        super.onPause()
        /*Log.d(AppConstants.TAG, "onPause")
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient?.isConnected() == true) {
            stopLocationUpdates();
        }*/

    }

    override fun onStop() {
        super.onStop()
        /*Log.d(AppConstants.TAG, "onStop")
        mGoogleApiClient?.disconnect()
        this.geoQuery?.removeAllListeners()*/

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        fireStore = FirebaseFirestore.getInstance()
        fireStoreUsers = fireStore?.collection("Users")

        // temporary:
        //SessionManager.currentUser.uid = "Wx08F5xMBHUcMgeQyjYWVq1xYIY2"
        if (savedInstanceState != null) {
            return;
        }

        var nearbyCards: NearbyCardsFragment? = null

        supportFragmentManager
                .beginTransaction()
                .add(R.id.frame, NearbyCardsFragment.newInstance("", ""), "nearbyCards")
                .commit()

        // Access a Cloud Firestore instance from your Activity
        /*fireStore = FirebaseFirestore.getInstance()
        fireStoreUsers = fireStore?.collection("Users")*/

        /*val p0 = Profile("title 0", "description0", "category0", "photo0")
        val p1 = Profile("title 1", "description1", "category1", "photo1")
        val al = ArrayList<Profile>()
        al.add(p0)
        al.add(p1)
        SessionManager.currentKUser!!.profiles = al*/

        currentKUser!!.profiles!![1].title = "test update 2"

        val profileRef = fireStoreUsers!!.document(SessionManager.currentUser!!.uid)

        profileRef.set(currentKUser!!.profiles!!).addOnCompleteListener() { task ->

            if (task.isSuccessful) {

                Log.d(AppConstants.TAG, "currentKUser!!.activeprofile : ${currentKUser!!.activeprofile}")

            } else {
                Log.d(AppConstants.TAG, "task success: ${task.isSuccessful}")

            }

        }

        val p = currentKUser!!.profiles!!
        val cp = currentKUser!!.activeprofile
        if (p.size > 0 && cp!! > -1) { // we have profiles and an active profile number

            val myWidth = 512
            val myHeight = 384

            val target = object : SimpleTarget<Bitmap>(myWidth, myHeight) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    profile_image.setImageBitmap(resource)
                }

                
            }

            Glide.with(this).asBitmap().load(p[cp]).into(target)

            active_user_name.setText(SessionManager.currentUser?.uid)
            navigation.setOnNavigationItemSelectedListener(
                    object : BottomNavigationView.OnNavigationItemSelectedListener {
                        override fun onNavigationItemSelected(item: MenuItem): Boolean {
                            when (item.itemId) {
                                navigation_home -> {
                                    Log.d(AppConstants.TAG, title_home.toString())

                                    val nearbyCards = NearbyCardsFragment.newInstance("", "")
                                    val transaction = supportFragmentManager.beginTransaction()
                                    transaction.replace(R.id.frame, nearbyCards)
                                    transaction.commit()
                                }
                                navigation_myprofiles -> {
                                    Log.d(AppConstants.TAG, title_myprofiles.toString())
                                    val myProfielsFragment = MyProfilesFragment.newInstance(1)
                                    val transaction = supportFragmentManager.beginTransaction()
                                    transaction.replace(R.id.frame, myProfielsFragment)
                                    transaction.commit()
                                }
                                navigation_notifications -> {

                                    Log.d(AppConstants.TAG, title_notifications.toString())
                                }
                            }
                            return true
                        }
                    })

            /*
        OK: retrieve Class KUser
        */

            /* TRY: RxBus

         */

            /*// Listen for MessageEvents only
        RxBus.listen(Chat::class.java).subscribe({
            Log.d(AppConstants.TAG, "Im a Message event ${it.message}")
        })

// Listen for String events only
        RxBus.listen(String::class.java).subscribe({
            println("Im a String event $it")
        })

// Listen for Int events only
        RxBus.listen(Int::class.java).subscribe({
            println("Im an Int event $it")
        })*/


            /*@Subscribe(threadMode = ThreadMode.MAIN)
        public void onMessageEvent(EventMessage event) {
            Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        }*/


            /* TRY: GeoFire
        */


            /* TRY: Query on GeoFire

        */

            /* val dbGeoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(GEO_FIRE_REF)
        val geoFire = GeoFire(dbGeoRef)

        this.geoQuery = geoFire.queryAtLocation(GEO_FIRE_INITIAL_CENTER, GEO_RADIUS)*/

            /*btn_location = findViewById<Button>(R.id.button)
        //total six textviews
        txt_location = findViewById<TextView>(R.id.mLatitudeTextView)
        mLatitudeTextView = findViewById<TextView>(R.id.mLatitudeTextView)
        mLongitudeTextView = findViewById<TextView>(R.id.mLongitudeTextView)
        mLastUpdateTimeTextView = findViewById<TextView>(R.id.mLastUpdateTimeTextView)
        tv_city = findViewById<TextView>(R.id.tv_city)
        tv_pincode = findViewById<TextView>(R.id.tv_pincode)

        // Set labels.
        mLatitudeLabel = "lat"//getResources().getString(R.string.latitude_label);
        mLongitudeLabel = "long"//getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = "lastUpdatedTime"//getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";*/

            // Kick off the process of building the GoogleApiClient, LocationRequest, and
            // LocationSettingsRequest objects.

            /*       Log.d(AppConstants.TAG, "Building GoogleApiClient")
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (mGoogleApiClient != null)
            mGoogleApiClient!!.connect()
        else
            Log.d(AppConstants.TAG, "Building GoogleApiClient")

        Log.d(AppConstants.TAG, "createLocationRequest")
        mLocationRequest = LocationRequest()
        mLocationRequest?.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest?.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()

        val result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        )
        result.setResultCallback(this)

        btn_location?.setOnClickListener(View.OnClickListener {
            checkLocationSettings();
        })*/

            /*lv_recycler.layoutManager = LinearLayoutManager(this@MainActivity)

        mCardsAdapter = CardsRecyclerViewAdapter(mUserCards)

        lv_recycler.setHasFixedSize(true)
        lv_recycler.adapter = mCardsAdapter*/


            /*OK: real-time event listener on a document
        val dbDocRef = FirebaseFirestore.getInstance().document("Users/BYcZarYdyijCCT3c8GGY")

        dbDocRef.addSnapshotListener(object : com.google.firebase.firestore.EventListener<DocumentSnapshot> {
            override fun onEvent(snapshot: DocumentSnapshot?,
                        e: FirebaseFirestoreException?) {
                if (e != null) {
                    Log.w(AppConstants.TAG, "Listen failed.", e)
                    return
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.d(AppConstants.TAG, "Current data: " + snapshot.data)
                } else {
                    Log.d(AppConstants.TAG, "Current data: null")
                }
            }
        })

        dbDocRef.update("age", 22)*/

            /*
        OK: retrieve Class KUser


        val dbDocRef = FirebaseFirestore.getInstance().document("Users/BYcZarYdyijCCT3c8GGY")
        dbDocRef.get().addOnSuccessListener { documentSnapshot ->

            if (documentSnapshot.exists()) {

                val kuser = documentSnapshot.toObject(KUser::class.java)

                Log.d(AppConstants.TAG, "name: ${kuser.name}, age: ${kuser.age}, profile1: ${kuser.profiles?.get(0)?.title}" )
            } else {
                Log.d(AppConstants.TAG, "not exist")
            }
        }*/

            /*
        OK: create and save KUser with Profiles array with GeoPoint saved to firebase geofire


        var profile1 = Profile("player6", "soccer pro2", "personal", "https://firebasestorage.googleapis.com/v0/b/sparxkotlin-ec645.appspot.com/o/IMG_20161124_143817.jpg?alt=media&token=65ed6459-808c-4605-8621-d7c56bfd026c")
        var profile2 = Profile("teacher7", "math tutor2", "business", "https://firebasestorage.googleapis.com/v0/b/sparxkotlin-ec645.appspot.com/o/kebab1.png?alt=media&token=c5906a70-4193-405f-b74e-f1aca45bc3a7")

        var ar = ArrayList<Profile>()

        ar.add(profile1)
        ar.add(profile2)

        val loc = GeoPoint(-33.7465798,150.8318639)

        val location = GeoLocation(-33.715275, 150.789985)

        val mtoken = SessionManager.deviceToken

        val user = KUser("Ray7", 22, "mark7@g.com", "pass", loc, 0, false, mtoken!!, ar)

        val cuser = SessionManager.currentUser
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Users").document(cuser!!.uid).set(user)
                .addOnSuccessListener {
                    documentReference -> Log.d(AppConstants.TAG, "DocumentSnapshot added with ID: " + cuser.uid)
                    geoFire.setLocation(cuser.uid, location)
                }
                .addOnFailureListener { e -> Log.w(AppConstants.TAG, "Error adding document", e) }

                */

            /*
        OK: getting a value

        var dbDocRef = FirebaseFirestore.getInstance().document("Profiles/gObrxMpzL4KaAf0tus9K")
        dbDocRef.get().addOnSuccessListener { documentSnapshot ->

            if (documentSnapshot.exists()) {

                Log.d(AppConstants.TAG, "first: " + documentSnapshot.getString("pro_image"))
            } else {
                Log.d(AppConstants.TAG, "not exist")
            }
        }*/

            /*
       OK: retrieve All KUsers to ArrayList


       val dbColRef = FirebaseFirestore.getInstance().collection("Users")
        dbColRef.get().addOnCompleteListener { task ->

           if (task.isSuccessful) {
               // recycler view build
               //val rv : RecyclerView = recyclerView
               lv_recycler.layoutManager = LinearLayoutManager(this@MainActivity)

               for (document in task.getResult()) {
                        Log.d(AppConstants.TAG, document.getId() + " => " + document.getData());
                   mUserCards.add(document)
               }

               mCardsAdapter = CardsRecyclerViewAdapter( mUserCards)

               lv_recycler.setHasFixedSize(true)
               lv_recycler.adapter = mCardsAdapter
               //val kuser = documentSnapshot.toObject(KUser::class.java)
               //Log.d(AppConstants.TAG, "name: ${kuser.name}, age: ${kuser.age}, profile1: ${kuser.profiles?.get(0)?.title}" )
           } else {
               Log.d(AppConstants.TAG, "no success")
           }
       }

        */

            /*
        OK: Query on GeoPoints

        val dbDocRef = FirebaseFirestore.getInstance().collection("Users")

        val l = GeoPoint(-33.7465799,150.8318628)



        val q : Query = dbDocRef.whereEqualTo("location", GeoPoint(-33.7465799,150.8318628))

        val a = GeoPoint(-33.746308, 150.835094)
        val b = GeoPoint(-33.748389, 150.847984)



        if (a < b) {
            Log.d(AppConstants.TAG, "geo: true" )
        } else {
            Log.d(AppConstants.TAG, "geo: false" )
        }




        q.get().addOnSuccessListener { task ->
            if (task.isEmpty) {

                Log.d(AppConstants.TAG, "geopoints: no success")
                // recycler view build
                //val rv : RecyclerView = recyclerView


            } else {
                Log.d(AppConstants.TAG, "geopoints: ${task.documents.size}" )
            }

        }

        */


            /*
        OK: add Chats

        val dbUsersDocRef = FirebaseFirestore.getInstance().document("Users/pj1hrgpECZe97n7wb6EZ")
        val chats = dbUsersDocRef.collection("Chats")
        val chat = Chat("me", "message", "myid")
        chats.add(chat)
        */


        }

        //step 1
        fun buildGoogleApiClient() {
            Log.d(AppConstants.TAG, "Building GoogleApiClient");
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            if (mGoogleApiClient != null)
                mGoogleApiClient!!.connect()
            else
                Log.d(AppConstants.TAG, "Building GoogleApiClient")
        }

        //step 2
        fun createLocationRequest() {
            Log.d(AppConstants.TAG, "createLocationRequest")
            mLocationRequest = LocationRequest();

            // Sets the desired interval for active location updates. This interval is
            // inexact. You may not receive updates at all if no location sources are available, or
            // you may receive them slower than requested. You may also receive updates faster than
            // requested if other applications are requesting location at a faster interval.
            mLocationRequest?.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            mLocationRequest?.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

            mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        }

        //step 3
         fun buildLocationSettingsRequest() {
            val builder = LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest!!);
            mLocationSettingsRequest = builder.build();
        }

        //step 4
         fun checkLocationSettings() {
            val result = LocationServices.SettingsApi.checkLocationSettings(
                    mGoogleApiClient,
                    mLocationSettingsRequest
            );
            result.setResultCallback(this);
        }

        /**
         * Requests location updates from the FusedLocationApi.
         */


        /**
         * Removes location updates from the FusedLocationApi.
         */
         fun stopLocationUpdates() {
            // It is a good practice to remove location requests when the activity is in a paused or
            // stopped state. Doing so helps battery performance and is especially
            // recommended in applications that request frequent location updates.
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,
                    this
            ).setResultCallback {
                mRequestingLocationUpdates = false;
                //   setButtonsEnabledState();
            }


        }




        //If using emulator then send the location from Emulator's Extended Controls. :)


        /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

        // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.d(AppConstants.TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                    }
                    Activity.RESULT_CANCELED ->
                        Log.d(AppConstants.TAG, "User chose not to make required location settings changes.");
                }
            }
        }
    }*/

        /**
         * Sets the value of the UI fields for the location latitude, longitude and last update time.
         */


        /**
         *	This updateCityAndPincode method uses Geocoder api to map the latitude and longitude into city location or pincode.
         *	We can retrieve many details using this Geocoder class.
         *
        And yes the Geocoder will not work unless you have data connection or wifi connected to internet.
         */

    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(AppConstants.TAG, "no permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION);
        } else {
            Log.d(AppConstants.TAG, "permission ok")
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    this
            ).setResultCallback {
                mRequestingLocationUpdates = true;
                //     setButtonsEnabledState();
            }
        }
    }
}
