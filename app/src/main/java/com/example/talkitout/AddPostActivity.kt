package com.example.talkitout


import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_post.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AddPostActivity : AppCompatActivity()
{
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null
    var arrayList: ArrayList<String>? = null
    var spinner: Spinner ?= null
    var categoryName: String? =null
    var image_add:ImageView?= null
    var image_add_text:TextView?=null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        spinner= findViewById(R.id.spinner)
        arrayList = ArrayList()
        arrayList!!.add("Music")
        arrayList!!.add("Personal")
        arrayList!!.add("Community")
        arrayList!!.add("Others")
        arrayList!!.sort()
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayList!!)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.adapter = arrayAdapter
        spinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                categoryName = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        save_new_post_btn.setOnClickListener {
            if(imageUri!=null)
            uploadImage()
            else{
                uploadText()
            }
        }
        image_add = findViewById(R.id.image_add)
        image_add_text = findViewById(R.id.image_add_text)

        image_add!!.setOnClickListener{
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(2, 1)
                    .start(this@AddPostActivity)
            image_add!!.visibility = View.GONE
            image_add_text!!.visibility = View.GONE
        }
        image_add_text!!.setOnClickListener{
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(2, 1)
                    .start(this@AddPostActivity)
            image_add!!.visibility = View.GONE
            image_add_text!!.visibility = View.GONE
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post.setImageURI(imageUri)
            image_post!!.visibility = View.VISIBLE
        }
    }

    private fun uploadText() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Adding New Post")
        progressDialog.setMessage("Please wait, we are adding your post...")
        progressDialog.show()
        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
        val postId = ref.push().key

        myUrl = ""

        val postMap = HashMap<String, Any>()
        postMap["postid"] = postId!!
        postMap["description"] = description_post.text.toString()
        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
        postMap["category"] = categoryName!!.toString()
        postMap["postimage"] = myUrl

        ref.child(postId).updateChildren(postMap)

        Toast.makeText(this, "Post uploaded successfully.", Toast.LENGTH_LONG).show()

        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

        progressDialog.dismiss()
    }

    private fun uploadImage()
    {
        when{
            //imageUri == null -> Toast.makeText(this, "Please select image first.", Toast.LENGTH_LONG).show()
            //TextUtils.isEmpty(description_post.text.toString()) -> Toast.makeText(this, "Please write description.", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we are adding your post...")
                progressDialog.show()

                    val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                    var uploadTask: StorageTask<*>
                    uploadTask = fileRef.putFile(imageUri!!)

                    uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                                progressDialog.dismiss()
                            }
                        }
                        return@Continuation fileRef.downloadUrl
                    })
                            .addOnCompleteListener(OnCompleteListener<Uri> { task ->
                                if (task.isSuccessful) {
                                    val downloadUrl = task.result
                                    myUrl = downloadUrl.toString()

                                    val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                                    val postId = ref.push().key

                                    val postMap = HashMap<String, Any>()
                                    postMap["postid"] = postId!!
                                    postMap["description"] = description_post.text.toString()
                                    postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                                    postMap["postimage"] = myUrl
                                    postMap["category"] = categoryName!!.toString()

                                    ref.child(postId).updateChildren(postMap)

                                    Toast.makeText(this, "Post uploaded successfully.", Toast.LENGTH_LONG).show()

                                    val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()

                                    progressDialog.dismiss()

                                } else {
                                    progressDialog.dismiss()
                                }
                            })
                }
            }
        }
    }