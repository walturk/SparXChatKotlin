package proximedia.com.au.sparxkotlin.common

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_chat.*
import proximedia.com.au.sparxkotlin.R.layout.activity_chat
import proximedia.com.au.sparxkotlin.constants.AppConstants
import proximedia.com.au.sparxkotlin.models.Chat
import proximedia.com.au.sparxkotlin.models.KUser
import proximedia.com.au.sparxkotlin.utils.ChatRecyclerAdapter
import proximedia.com.au.sparxkotlin.utils.RxBus

class ChatActivity : Activity() {

    var mChatAdapter: ChatRecyclerAdapter? = null
    lateinit var  mChats : ArrayList<Chat>
    var chattingWithUser : KUser? = null
    var commonDualChatRoom : CollectionReference? = null

    // initialize Firestore reference: Testing with collection = Users/pj1hrgpECZe97n7wb6EZ
    val dbUsersDocRef = FirebaseFirestore.getInstance().document("Users/"+SessionManager.currentUser!!.uid)
    val sChatCollection = dbUsersDocRef.collection("Chats")
    val sChatQuery = sChatCollection
            .whereEqualTo("uid", chattingWithUser?.id)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(50)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_chat)

        RxBus.listen(Chat::class.java).subscribe({
            mChats.add(it)
            if (mChats.size > 1) {
                Log.d(AppConstants.TAG, "RxBus smooth size= ${mChats.size}")
                rv_messages.smoothScrollToPosition(mChats.size - 1);
            }
            mChatAdapter?.notifyDataSetChanged()
            Log.d(AppConstants.TAG, "ChatActivity: Message event ${it.message}")

        })

        chattingWithUser = intent.getParcelableExtra<KUser?>("otherUser")
        Log.d(AppConstants.TAG, "chattingWithUser: ${chattingWithUser?.id!!}")

        Log.d(AppConstants.TAG, "id: ${chattingWithUser?.email}")

        val img = profile2_image
        Glide.with(img.getContext())
                .load("https://firebasestorage.googleapis.com/v0/b/sparxkotlin-ec645.appspot.com/o/IMG_20161124_143817.jpg?alt=media&token=65ed6459-808c-4605-8621-d7c56bfd026c")
                .into(img)

        tv_profile_title.setText(chattingWithUser?.id)
        rv_messages.setHasFixedSize(true)
        rv_messages.layoutManager = LinearLayoutManager(this@ChatActivity)

        sChatQuery.get()
                .addOnSuccessListener(OnSuccessListener<QuerySnapshot> { documentSnapshots ->

                    mChats = ArrayList<Chat>()

                    for (doc in documentSnapshots) {

                        val d = doc.toObject(Chat::class.java)
                        d.uid = chattingWithUser!!.id

                        try {
                            mChats.add(d)
                        } catch (e: Exception) {
                            Log.d(AppConstants.TAG, "X: ${e.localizedMessage}")
                        }

                        Log.d(AppConstants.TAG, "mChats: ${d.message} (${mChats.size}")
                    }

                    mChatAdapter = ChatRecyclerAdapter(mChats)
                    observeAdapter()
                    if (mChats.size > 0) {
                        Log.d(AppConstants.TAG, "smooth size= ${mChats.size}")
                        rv_messages.smoothScrollToPosition(mChats.size - 1);
                    }



                    mChatAdapter?.notifyDataSetChanged()
                    rv_messages.adapter = mChatAdapter

                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.d(AppConstants.TAG, "X: ${e.localizedMessage}")
                })

        //emo_message.setText("dfdfsdfds")

        //tv_description.setText("Description text ...")
        btnSendChatMsg.setOnClickListener(View.OnClickListener {
            Log.d(AppConstants.TAG, "btnSendChatMsg")

            // check if otherUser has a chat room with me
            val otherUserRef = FirebaseFirestore.getInstance().collection("Users").document(chattingWithUser?.id!!)
            val chatRoom = otherUserRef.collection(SessionManager.currentUser!!.uid)

            chatRoom.get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val docSize = task.getResult().documents.size
                    if (docSize > 0) { // room exists, save chat messages to this room
                        Log.d(AppConstants.TAG, "chatRoom exists")
                        commonDualChatRoom = otherUserRef.collection(SessionManager.currentUser!!.uid)

                    } else { //the other user has no chat room with me, so create one
                        Log.d(AppConstants.TAG, "chatRoom not exist")
                        val otherUserRef = FirebaseFirestore.getInstance().collection("Users").document(SessionManager.currentUser!!.uid)
                        commonDualChatRoom = otherUserRef.collection(chattingWithUser?.id!!)
                    }
                    val chat = Chat("", emo_message.text.toString(), chattingWithUser!!.id!!, SessionManager.currentKUser!!.activeprofile!!)
                    commonDualChatRoom?.add(chat)
                } else { //task failed
                    Log.d(AppConstants.TAG, "task failed")
                }
            }



        })

        /*val dbUsersDocRef = FirebaseFirestore.getInstance().document("Users/pj1hrgpECZe97n7wb6EZ")
        val chatsRef = dbUsersDocRef.collection("Chats")
        chatsRef.addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(@Nullable snapshots: QuerySnapshot,
                                @Nullable e: FirebaseFirestoreException?) {
                        if (e != null) {
                            Log.w(AppConstants.TAG, "listen:error", e)
                            return
                        }

                        for (dc in snapshots.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> Log.d(AppConstants.TAG, "New city: " + dc.document.data)
                                DocumentChange.Type.MODIFIED -> Log.d(AppConstants.TAG, "Modified city: " + dc.document.data)
                                DocumentChange.Type.REMOVED -> Log.d(AppConstants.TAG, "Removed city: " + dc.document.data)
                            }
                        }

                    }
                })*/

        // set Chats Collection listener

        /*
        OK: add Chats

        btnSendChatMsg.setOnClickListener(View.OnClickListener {
            Log.d(AppConstants.TAG, "btnSendChatMsg")
            val dbUsersDocRef = FirebaseFirestore.getInstance().document("Users/pj1hrgpECZe97n7wb6EZ")
            val chats = dbUsersDocRef.collection("Chats")
            val chat = Chat("me", "message", "myid")
            chats.add(chat)

        */
    }

    fun observeAdapter() {
        mChatAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                //mRecyclerView.smoothScrollToPosition(mChatAdapter?.getItemCount())
                Log.d(AppConstants.TAG, "onItemRangeInserted: ${itemCount}")
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)

                Log.d(AppConstants.TAG, "onItemRangeChanged: ${itemCount}")

            }

            override fun onChanged() {
                super.onChanged()
                Log.d(AppConstants.TAG, "onChanged: ")
            }


        })
    }

    public override fun onStart() {
        super.onStart()
        /*if (isSignedIn()) {

        }*/
        //FirebaseAuth.getInstance().addAuthStateListener(this)
    }


}
