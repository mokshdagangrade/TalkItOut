package com.example.talkitout

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class StoryTimeActivity : AppCompatActivity() {
    var hours:Int?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_time)

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        val edittextCode = EditText(this@StoryTimeActivity)
        alert.setMessage("Enter the number of hours below.")
        alert.setTitle("Time duration")
        alert.setView(edittextCode)
        alert.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, whichButton ->
            hours = edittextCode.text.toString().toInt()
            val intent = Intent(this@StoryTimeActivity, AddStoryActivity::class.java)
            intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
            intent.putExtra("hours",hours)
            startActivity(intent)
        })
        alert.show()
    }
}