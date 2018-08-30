package proximedia.com.au.sparkchat.fragments

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

import proximedia.com.au.sparxkotlin.R
import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.constants.AppConstants.ACTIVATE_PROFILE
import proximedia.com.au.sparxkotlin.constants.AppConstants.EDIT_PROFILE
import proximedia.com.au.sparxkotlin.fragments.MyProfilesFragment
import proximedia.com.au.sparxkotlin.models.Profile

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(private val mValues: ArrayList<Profile>?,
                                private val mListener: MyProfilesFragment.OnListFragmentInteractionListener?) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.myprofiles_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var bitmap : Bitmap? = null

        holder.mItem = mValues!![position]
        holder.mPosition = position
        holder.mTitleView.text = mValues[position].title
        holder.mDescView.text = mValues[position].category

        /*Glide.with(holder.mImgView!!.getContext())
                .load(holder.mItem!!.photo)
                .into(holder.mImgView)*/

        /*Glide.with(holder.mImgView!!.getContext())
                .load(holder.mItem!!.photo)
                .into(holder.mImgView)*/

        val myWidth = 512
        val myHeight = 384

        var target = object : SimpleTarget<Bitmap>(myWidth, myHeight) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                holder.mImgView!!.setImageBitmap(resource)
            }
        }
        Glide.with(holder.mImgView!!.getContext()).asBitmap().load(holder.mItem!!.photo).into(target)

        holder.mActivateView!!.setOnClickListener() {

            Log.d(AppConstants.TAG, "ACTIVATE PROFILE: ${holder.mItem!!.title} | ${holder.mPosition}")
            mListener?.onListFragmentInteraction(holder.mPosition, ACTIVATE_PROFILE)

        }

        holder.mEditView!!.setOnClickListener() {

            Log.d(AppConstants.TAG, "EDIT PROFILE: ${holder.mItem!!.title} | ${holder.mPosition}")
            mListener?.onListFragmentInteraction(holder.mPosition, EDIT_PROFILE)

        }

        holder.mView.setOnClickListener {
            //mListener?.onListFragmentInteraction(holder.mItem!!)
        }
    }

    override fun getItemCount(): Int {
        return mValues!!.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mPosition : Int = 0
        var mImgView : ImageView? = null
        val mTitleView: TextView
        val mDescView: TextView
        var mActivateView : LinearLayout? = null
        var mEditView : LinearLayout? = null
        var mItem: Profile? = null

        init {
            mImgView = mView.findViewById(R.id.img_profile)


            mActivateView = mView.findViewById(R.id.activate_view)
            mEditView = mView.findViewById(R.id.edit_view)
            mTitleView = mView.findViewById(R.id.category)
            mDescView = mView.findViewById(R.id.title)
        }

        override fun toString(): String {
            return super.toString() + " '" + mDescView.text + "'"
        }
    }
}
