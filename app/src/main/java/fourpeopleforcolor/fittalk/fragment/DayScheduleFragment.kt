package fourpeopleforcolor.fittalk.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.navigation_activity.CommentActivity
import fourpeopleforcolor.fittalk.navigation_activity.UploadScheduleActivity
import org.w3c.dom.Text

/*
*팀원 김민지입니다.
*Schedule을 일주일단위로 등록하는 방식에서
* 캘린더를 통해 해당 요일의 일정을 추가하는 방식으로 바꾸었습니다.
* 또한 이 곳에 사용자가 해당 요일 섭취한 음식의 칼로리를 보는 기능 또한 개발할 예정입니다.
* 기존 Firebase Database의 Schedule의 필드값을 수정해야합니다.
*
* */

class DayScheduleFragment : Fragment() {

    var fragmentView: View? = null

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    // 현재 사용자의 uid
    var currentUserUid: String? = null

    var selectedDate : String? = null

    var contents : TextView? = null

    var food : TextView? = null
    var food_kcal : TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid
        fragmentView = inflater.inflate(R.layout.fragment_day_schedule, container, false)
        contents = fragmentView?.findViewById(R.id.textView)
        food = fragmentView?.findViewById(R.id.text_food)
        food_kcal = fragmentView?.findViewById(R.id.text_food_kcal)

        selectedDate=arguments?.getString("year")+"-"+arguments?.getString("month")+"-"+arguments?.getString("dayofMonth")

        getSchedule()

        var btn = fragmentView?.findViewById<Button>(R.id.upload_schedule_btn)
        btn?.setOnClickListener {
            //var uploadScheduleActivity = UploadScheduleActivity()
            var intent = Intent(fragmentView?.context, UploadScheduleActivity::class.java)
            intent.putExtra("date",selectedDate)

            startActivity(intent)
        }

        return fragmentView
    }


    fun getSchedule(){
        FirebaseFirestore.getInstance().collection("schedules").get().addOnCompleteListener { task: Task<QuerySnapshot> ->
            if (task.isSuccessful) {
                for (result in task.result) {
                    if (result.data["uid"] == currentUserUid) {
                        if(result.data["date"]==selectedDate){
                            contents?.setText(result.data["schedule"].toString())
                            food?.setText(result.data["food"].toString())
                            food_kcal?.setText(result.data["food_kcal"].toString())
                        }
                    }

                }
            }
        }
    }
}
