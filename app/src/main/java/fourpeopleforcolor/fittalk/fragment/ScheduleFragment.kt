package fourpeopleforcolor.fittalk.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fourpeopleforcolor.fittalk.R

/*
* selectedUid 값 이용해서 계획만 띄우면 됩니다.
*/

class ScheduleFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }
}
