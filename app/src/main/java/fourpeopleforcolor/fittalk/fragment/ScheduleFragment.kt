package fourpeopleforcolor.fittalk.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import fourpeopleforcolor.fittalk.MainActivity
import fourpeopleforcolor.fittalk.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_schedule.view.*

/*
* selectedUid 값 이용해서 계획만 띄우면 됩니다.
*/

class ScheduleFragment : Fragment() {

    var fragmentView: View? = null

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    // 현재 사용자의 uid
    var currentUserUid: String? = null

    // 현재 사용자가 선택한 사용자의 uid
    // UserProfileFragment에서 스케쥴 버튼을 누르면 스케쥴 프레그먼트로 전환되는 기능을 구현했습니다.
    // 그때 선택된 사용자의 uid를 인자로 넘겼습니다. 그 값을 받는 변수가 selectedUid입니다.
    // UserProfileFragment.kt를 참고하세요
    var selectedUid: String? = null

    // 현재 사용자가 선택한 사용자의 userEmail
    var selectedUserEmail: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid
        fragmentView = inflater.inflate(R.layout.fragment_schedule, container, false)

        if (arguments != null) {
            // arguments가 null이 아니라면 넘어온 값이 있는 것입니다.
            // 선택된 사람의 uid
            selectedUid = arguments?.getString("destinationUid")
            // 선택된 사람의 email
            selectedUserEmail = arguments?.getString("userEmail")
        }

        var mainActivity = (activity as MainActivity)
        mainActivity.toolbar_btn_schedule.visibility = View.GONE

        // timestamp를 통해 최신순으로 정렬하고 사용자의 uid, 요일에 맞는 데이터를 가져옵니다.
        // schedules 디렉터리에 접근해서 timestamp(계획이 등록된 시스템 시간)으로 정렬해서 최신순으로 합니다.
        // 데이터 구조 변경 가능성에 따라 코드도 변경될 수 있습니다.
        FirebaseFirestore.getInstance().collection("schedules").orderBy("timestamp").get().addOnCompleteListener { task: Task<QuerySnapshot> ->
            if (task.isSuccessful) {
                // timestamp로 orderby 쿼리를 날리면
                // 계획이 업로드된 시스템 시간을 보고 최신순으로 정렬합니다.
                // 그 결과가 task.result에 담겨서 반환됩니다.
                for (result in task.result) {
                    if (result.data["uid"] == selectedUid) {
                        if (result.data["dayOfWeek"] == "월요일") {
                            fragmentView?.schedule_mon?.text = result.data["schedule"].toString()
                        }
                        if (result.data["dayOfWeek"] == "화요일") {
                            fragmentView?.schedule_tue?.text = result.data["schedule"].toString()
                        }
                        if (result.data["dayOfWeek"] == "수요일") {
                            fragmentView?.schedule_wed?.text = result.data["schedule"].toString()
                        }
                        if (result.data["dayOfWeek"] == "목요일") {
                            fragmentView?.schedule_thu?.text = result.data["schedule"].toString()
                        }
                        if (result.data["dayOfWeek"] == "금요일") {
                            fragmentView?.schedule_fri?.text = result.data["schedule"].toString()
                        }
                        if (result.data["dayOfWeek"] == "토요일") {
                            fragmentView?.schedule_sat?.text = result.data["schedule"].toString()
                        }
                        if (result.data["dayOfWeek"] == "일요일") {
                            fragmentView?.schedule_sun?.text = result.data["schedule"].toString()
                        }
                    }
                }
            }
        }
        return fragmentView
    }
}
