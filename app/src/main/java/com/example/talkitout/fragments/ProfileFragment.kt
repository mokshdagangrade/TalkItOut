package com.example.talkitout.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talkitout.AccountSettingsActivity
import com.example.talkitout.R
import com.example.talkitout.ShowUsersActivity
import com.example.talkitout.SignInActivity
import com.example.talkitout.adapter.PostAdapter
import com.example.talkitout.model.Post
import com.example.talkitout.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    private lateinit var profileId: String

    private lateinit var firebaseUser: FirebaseUser

    var postList: MutableList<Post>? = null
    var postAdapter: PostAdapter? = null

    var postAdapterSavedImg: PostAdapter? = null
    var postListSaved: MutableList<Post>? = null
    var mySavesImg: List<String>? = null

    lateinit var toolbar: Toolbar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(pref != null)
        {

            this.profileId = pref.getString("profileId", "none").toString()
        }

        if(profileId == firebaseUser.uid)
        {

            view.edit_account_settings_btn.text = "Edit Profile"
        }
        else if(profileId != firebaseUser.uid)
        {
            checkFollowAndFollowingButtonStatus()
        }

        //recycler View for Uploaded Images
        var recyclerViewUploadImages: RecyclerView? = null
        recyclerViewUploadImages = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 1)
        recyclerViewUploadImages.layoutManager = linearLayoutManager


        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerViewUploadImages.adapter = postAdapter


        //recycler View for Saved Images
        var recyclerViewSavedImages: RecyclerView? = null
        recyclerViewSavedImages= view.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImages.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context, 1)
        recyclerViewSavedImages.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        postAdapterSavedImg = context?.let { PostAdapter(it, postListSaved as ArrayList<Post>) }
        recyclerViewSavedImages.adapter = postAdapterSavedImg


        recyclerViewSavedImages.visibility = View.GONE
        recyclerViewUploadImages.visibility = View.VISIBLE


        var uploadedImagesBtn: ImageButton
        uploadedImagesBtn = view.findViewById(R.id.images_grid_view_btn)
        uploadedImagesBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.GONE
            recyclerViewUploadImages.visibility = View.VISIBLE
        }

        var savedImagesBtn: ImageButton
        savedImagesBtn = view.findViewById(R.id.images_save_btn)
        savedImagesBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.VISIBLE
            recyclerViewUploadImages.visibility = View.GONE
        }

        view.total_followers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        view.total_following.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }




        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()

            when
            {
                getButtonText == "Edit Profile" -> startActivity(
                    Intent(
                        context,
                        AccountSettingsActivity::class.java
                    )
                )

                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }

                    addNotification()

                }

                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }

                }
            }

        }

        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
        getTotalNumberOfPosts()
        mySaves()


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar = view.findViewById(R.id.profile_toolbar) as Toolbar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            toolbar.inflateMenu(R.menu.more)
            toolbar.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_edit_profile -> {
                            startActivity(
                                Intent(
                                    context,
                                    AccountSettingsActivity::class.java
                                )
                            )
                    }
                    R.id.action_logout -> {
                        FirebaseAuth.getInstance().signOut()

                        val intent = Intent(context, SignInActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        activity!!.finish()
                    }
                }
                true
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.more, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item!!.itemId
        if(id==R.id.action_edit_profile){
            startActivity(
                Intent(
                    context,
                    AccountSettingsActivity::class.java
                )
            )
        }
        if(id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity!!.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkFollowAndFollowingButtonStatus()
    {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if(followingRef != null)
        {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists()) {
                        view?.edit_account_settings_btn?.text = "Following"
                    } else {
                        view?.edit_account_settings_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.total_followers?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowings()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.total_following?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun myPhotos()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postList as ArrayList<Post>).clear()

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)!!
                        if (post.getPublisher().equals(profileId)) {
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        postAdapter!!.notifyDataSetChanged()

                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    if (user!!.getImage().isEmpty()) {
                        view?.pro_image_profile_frag!!.setImageResource(R.drawable.profile)
                    } else {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                            .into(view?.pro_image_profile_frag)
                    }
                    view?.profile_fragment_username?.text = user!!.getUsername()
                    view?.full_name_profile_frag?.text = user!!.getFullname()
                    view?.bio_profile_frag?.text = user!!.getBio()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }


    private fun getTotalNumberOfPosts()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var postCounter = 0

                    for (snapShot in dataSnapshot.children) {
                        val post = snapShot.getValue(Post::class.java)!!

                        if (post.getPublisher() == profileId) {
                            postCounter++
                        }
                    }
                    total_posts.text = " " + postCounter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun mySaves()
    {
        mySavesImg = ArrayList()

        val savedRef = FirebaseDatabase.getInstance()
            .reference
            .child("Saves").child(firebaseUser.uid)


        savedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        (mySavesImg as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedImagesData()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun readSavedImagesData()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (postListSaved as ArrayList<Post>).clear()

                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)

                        for (key in mySavesImg!!) {
                            if (post!!.getPostid() == key) {
                                (postListSaved as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    Collections.reverse(postListSaved)
                    postAdapterSavedImg!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun addNotification()
    {
        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(profileId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }

}
