package fourpeopleforcolor.fittalk.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import fourpeopleforcolor.fittalk.R

/*
* 사용자가 팔로잉하는 유저들의 목록을 띄우는 fragment입니다.
 */

/*
* UserProfileFragment에서 넘어온 uid, UserEmail 값을 활용합니다.
* 넘어온 uid를 기준으로 이 uid를 가진 유저가 팔로잉 하는 유저들(팔로잉)의 정보를 가져와야 합니다.
 */

// 리스너 두개 필요, 팔로잉 관련/ 프로필 사진 관련

class FollowingListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_following_list, container, false)
    }


}
