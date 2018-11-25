package fourpeopleforcolor.fittalk.fragment

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fourpeopleforcolor.fittalk.R
import android.widget.CalendarView
import android.widget.FrameLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Month
import java.time.Year

/*
* 11월 26일 팀원 김민지 입니다.
* 화면은 2분할 시켜 위에는 캘린더가, 아래는 캘린더에 적힌 일정에 대한 내용이 뜨도록 하였습니다.
* 파이어베이스 데이터베이스에 데이터가 오갈 수 있도록 연동을 시켜놓은 상태입니다.
* 아직 디자인적인 부분이 완성되지않아 미흡해 보이지만 출력만 시키지 않은 상태입니다.
*
* 현재 디자인을 수정하면 fragment가 사라지는(?) 버그가 있습니다.
* */
class CalendarFragment:Fragment(){

    var fragmentView : View? = null

    var CalendarView : CalendarView? = null

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var currentUserUid: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(inflater?.context).inflate(R.layout.fragment_calendar, container, false)
        CalendarView = fragmentView?.findViewById(R.id.calendarView)


        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        currentUserUid = auth?.currentUser?.uid

        CalendarView?.setOnDateChangeListener { fragmentView, year, month, dayOfMonth ->
            //Toast.makeText(fragmentView?.context, "" + dayOfMonth, Toast.LENGTH_LONG).show()
            viewDaySchedule(year,month,dayOfMonth)
        }

        return fragmentView
    }

    /*
    팀원 김민지 입니다.
    하위 fragment를 불러오는 내용입니다.*/
    fun viewDaySchedule(year: Int ,month: Int ,dayofMonth :Int){
        var fragmentScheduleView = fragmentView?.findViewById<FrameLayout>(R.id.fragment_schedule_view)

        var dayScheduleFragment = DayScheduleFragment()
        var FragmentTransaction = childFragmentManager.beginTransaction()
        var bundle = Bundle()

        bundle.putString("year",year.toString())
        bundle.putString("month",month.toString())
        bundle.putString("dayofMonth",dayofMonth.toString())

        dayScheduleFragment.arguments = bundle
        FragmentTransaction.replace(fragmentScheduleView!!.id,dayScheduleFragment).commit()
    }
}