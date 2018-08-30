package proximedia.com.au.sparxkotlin.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_near_cards.view.*
import proximedia.com.au.sparxkotlin.R.layout.fragment_near_cards
import proximedia.com.au.sparxkotlin.common.SessionManager
import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.models.Chat
import proximedia.com.au.sparxkotlin.models.KUser
import proximedia.com.au.sparxkotlin.utils.CardsRecyclerViewAdapter
import proximedia.com.au.sparxkotlin.utils.RxBus
import java.text.DateFormat
import java.util.*

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * [NearbyCardsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [NearbyCardsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class NearbyCardsFragment : Fragment() , GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        GeoQueryEventListener {

    override fun onGeoQueryReady() {
    }

    override fun onKeyEntered(key: String, location: GeoLocation?) {

        getUserByKey(key)
        Log.d(AppConstants.TAG, "Key entered: ${key}")
    }

    override fun onKeyMoved(key: String?, location: GeoLocation?) {
    }

    override fun onKeyExited(key: String?) {
    }

    override fun onGeoQueryError(error: DatabaseError?) {
    }

    val self = context

    // The URL to +1.  Must be a valid URL.
    private val PLUS_ONE_URL = "http://developer.android.com"
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    var mGoogleApiClient: GoogleApiClient? = null
    var RQS_GooglePlayServices = 0
    protected var mLocationRequest: LocationRequest? = null
    protected var mRequestingLocationUpdates: Boolean = false
    protected var mLastUpdateTime: String = ""

    //Any random number you can take
    val REQUEST_PERMISSION_LOCATION: Int = 10

    /**
     * Constant used in the location settings dialog.
     */
    protected val REQUEST_CHECK_SETTINGS: Int = 0x1

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected var mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Represents a geographical location.
     */
    protected var mCurrentLocation: Location? = null

    // Labels.
    protected var mLatitudeLabel: String = "";
    protected var mLongitudeLabel: String = "";
    protected var mLastUpdateTimeLabel: String = ""

    var fireStore : FirebaseFirestore? = null
    var fireStoreUsers : CollectionReference? = null
    var mUserCards = ArrayList<KUser>()
    var mCardsAdapter : CardsRecyclerViewAdapter? = null

    // GeoFire:
    private val GEO_FIRE_REF = "geofire"
    private val GEO_FIRE_INITIAL_CENTER = GeoLocation(-33.751130, 150.833901)
    private val GEO_RADIUS = 10.0
    private var geoQuery : GeoQuery? = null
    private var c : Int = 0
    private var r : Double = 1.0

    val mQuery : Query? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = savedInstanceState!!.getString(ARG_PARAM1)
            mParam2 = savedInstanceState.getString(ARG_PARAM2)


        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(fragment_near_cards, container, false)

        // Access a Cloud Firestore instance from your Activity
        fireStore = FirebaseFirestore.getInstance()

        fireStoreUsers = fireStore?.collection("Users")

        // Listen for MessageEvents only
        RxBus.listen(Chat::class.java).subscribe({


        })

// Listen for String events only
        RxBus.listen(String::class.java).subscribe({
            println("Im a String event $it")
        })

// Listen for Int events only
        RxBus.listen(Int::class.java).subscribe({
            println("Im an Int event $it")
        })


        /*@Subscribe(threadMode = ThreadMode.MAIN)
        public void onMessageEvent(EventMessage event) {
            Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        }*/


        /* TRY: GeoFire
        */


        /* TRY: Query on GeoFire

        */

        val dbGeoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(GEO_FIRE_REF)
        val geoFire = GeoFire(dbGeoRef)

        this.geoQuery = geoFire.queryAtLocation(GEO_FIRE_INITIAL_CENTER, GEO_RADIUS)

        Log.d(AppConstants.TAG, "Building GoogleApiClient")
        mGoogleApiClient = GoogleApiClient.Builder(container!!.context)
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


        view.lv_cards.layoutManager = LinearLayoutManager(context)
        mCardsAdapter = CardsRecyclerViewAdapter(mUserCards)

        view.lv_cards.setHasFixedSize(true)
        view.lv_cards.adapter = mCardsAdapter
        //Find the +1 button

        return view
    }

    protected fun stopLocationUpdates() {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_LOCATION ->
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(AppConstants.TAG, "Connection suspended");
    }

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
            if (ActivityCompat.checkSelfPermission(activity!!.baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)  , REQUEST_PERMISSION_LOCATION)
            } else {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                Log.d(AppConstants.TAG, "mCurrentLocation: ${mCurrentLocation.toString()}")
                updateLocationUI()
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

    //If using emulator then send the location from Emulator's Extended Controls. :)
    override fun onResult(locationSettingsResult: LocationSettingsResult) {

        val status: Status = locationSettingsResult.getStatus();
        Log.d(AppConstants.TAG, "Status: ${status}");

        when (status.getStatusCode()) {
            LocationSettingsStatusCodes.SUCCESS -> {
                Log.d(AppConstants.TAG, "All location settings are satisfied.");
                //If using emulator then send the location from Emulator's Extended Controls. :)
                Toast.makeText(activity, "Location is already on.", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
            }

            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                Log.d(AppConstants.TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    Toast.makeText(self, "Location dialog will be open", Toast.LENGTH_SHORT).show();

                    //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                    status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private fun updateLocationUI() {

    }

    protected fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(activity!!.baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(AppConstants.TAG, "no permission")
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION);
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

    fun getUserByKey(key: String) {

        val doc = fireStoreUsers?.document(key) as DocumentReference

        doc.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val kuser = documentSnapshot.toObject(KUser::class.java)
                if (mUserCards != null && SessionManager.currentUser != null) {
                    kuser?.id = documentSnapshot.id
                    if (kuser?.id != SessionManager.currentUser!!.uid) {
                    //if (kuser.id != TEMP_UID) {
                        mUserCards.add(kuser!!)
                        Log.d(AppConstants.TAG, "mUserCards size= ${mUserCards.size}")
                        mCardsAdapter?.notifyDataSetChanged()
                    }

                } else {
                    Log.d(AppConstants.TAG, "mUserCards: null")
                }
                Log.d(AppConstants.TAG, "name: ${kuser?.name}, age: ${kuser?.age}, profile1: ${kuser?.profiles?.get(0)?.title}, mUserCards: ${mUserCards.size}" )
            } else {
                Log.d(AppConstants.TAG, "not exist")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (mUserCards.size > 0) {
            mUserCards.clear()
        }

        Log.d(AppConstants.TAG, "onStart")
        var googleAPI = GoogleApiAvailability.getInstance();
        var resultCode: Int = googleAPI.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient?.connect();
        } else {
            googleAPI.getErrorDialog(activity, resultCode, RQS_GooglePlayServices);
        }

        this.geoQuery?.addGeoQueryEventListener(this)
        /* val token = FirebaseInstanceId.getInstance().token
         Log.d(AppConstants.TAG, "Refreshed token: $token - send this to server?")*/
    }

    override fun onResume() {
        super.onResume()

        // Refresh the state of the +1 button each time the activity receives focus.
        Log.d(AppConstants.TAG, "onResume")
        if (mGoogleApiClient?.isConnected() == true && mRequestingLocationUpdates) {
            //  Toast.makeText(FusedLocationWithSettingsDialog.this, "location was already on so detecting location now", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        } else {
            Log.d(AppConstants.TAG, "mGoogleApiClient not connected")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(AppConstants.TAG, "onPause")
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient?.isConnected() == true) {
            stopLocationUpdates();
        }

    }

    override fun onStop() {
        super.onStop()
        Log.d(AppConstants.TAG, "onStop")
        mGoogleApiClient?.disconnect()
        this.geoQuery?.removeAllListeners()

    }

    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        // The request code must be 0 or greater.
        private val PLUS_ONE_REQUEST_CODE = 0

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NearbyCardsFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): NearbyCardsFragment {
            val fragment = NearbyCardsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
