package proximedia.com.au.sparxkotlin.utils

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.cardview_nearby.view.*
import proximedia.com.au.sparxkotlin.R
import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.models.KUser
import android.content.Intent
import proximedia.com.au.sparxkotlin.common.ChatActivity


/**
 * Created by pc on 4/10/2017.
 */

    class CardsRecyclerViewAdapter(var userList: ArrayList<KUser>) : RecyclerView.Adapter<CardsRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->

            val context = v.context
            // retrieve the user class previously attached by setTag
            val u = v.getTag() as KUser?

            // Intent for the activity to open when user selects the notification
            val profileIntent = Intent(context, ChatActivity::class.java)
            profileIntent.putExtra("otherUser", u)
            context.startActivity(profileIntent)

            Log.d(AppConstants.TAG, "v: ${u?.name}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardsRecyclerViewAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_nearby, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CardsRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.bindItems(userList[position])



        with (holder.img) {
            setOnClickListener(mOnClickListener)
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.user_image
        val title = itemView.title
        val category = itemView.category

        fun bindItems(user: KUser) {
            val kuser = user
            //title.text = kuser.profiles!![kuser.activeprofile!!].title
            //category.text = kuser.profiles!![kuser.activeprofile!!].category
            val v = itemView.profile_description
            //v.text = kuser.profiles!![kuser.activeprofile!!].description
            //val img = itemView.findViewById<ImageView>(R.id.user_image)

            img.setTag( user)
            //Log.d(AppConstants.TAG, kuser.profiles!![kuser.activeprofile!!].photo)
            //Glide.with(img.getContext()).load(kuser.profiles!![kuser.activeprofile!!].photo).into(img)
        }
    }
    }