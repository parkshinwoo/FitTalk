package fourpeopleforcolor.fittalk.navigation_activity

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import fourpeopleforcolor.fittalk.R
import kotlinx.android.synthetic.main.activity_upload_schedule.*

class UploadScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_schedule)
    }
}
