package fourpeopleforcolor.fittalk.navigation_activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fourpeopleforcolor.fittalk.R

class CommentActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        auth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()


    }

    fun getImage(){
    }
}
