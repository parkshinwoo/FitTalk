package fourpeopleforcolor.fittalk.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import fourpeopleforcolor.fittalk.MainActivity
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.FollowDTO
import fourpeopleforcolor.fittalk.data_trasfer_object.UserDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_follower_list.view.*
import kotlinx.android.synthetic.main.recyclerview_item_design_follower_list.view.*

/*
* 사용자를 팔로우하는 유저들의 목록을 띄우는 fragment입니다.
 */

/*
* UserProfileFragment에서 넘어온 uid, UserEmail 값을 활용합니다.
* 넘어온 uid를 기준으로 이 uid를 팔로잉 하는 유저들(팔로워)의 정보를 가져와야 합니다.
 */

class FollowerListFragment : Fragment() {

    var fragmentView : View? = null

    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    // 현재 사용자의 uid
    var currentUserUid : String? = null

    // 현재 사용자가 선택한 사용자의 uid
    // UserProfileFragment에서 팔로워 수 관련 숫자, 텍스트를 누르면 팔로워 리스트 프레그먼트로 전환되는 기능을 구현했습니다.
    // 그때 선택된 사용자의 uid를 인자로 넘겼습니다. 그 값을 받는 변수가 selectedUid입니다.
    // UserProfileFragment.kt를 참고하세요
    var selectedUid : String? = null

    // 현재 사용자가 선택한 사용자의 userEmail
    var selectedUserEmail : String? = null

    var followerListenerRegistration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid
        fragmentView = inflater.inflate(R.layout.fragment_follower_list, container, false)

        if(arguments != null){
            // arguments가 null이 아니라면 넘어온 값이 있는 것입니다.
            // 선택된 사람의 uid
            selectedUid = arguments?.getString("destinationUid")
            // 선택된 사람의 email
            selectedUserEmail = arguments?.getString("userEmail")
        }

        var mainActivity = (activity as MainActivity)
        mainActivity.toolbar_btn_schedule.visibility = View.GONE

        return fragmentView
    }

    override fun onStop() {
        super.onStop()
        followerListenerRegistration?.remove()
    }

    override fun onResume() {
        super.onResume()
        fragmentView?.follower_list_recyclerview?.adapter = FollowerListFragmentRecyclerViewAdapter()
        fragmentView?.follower_list_recyclerview?.layoutManager = LinearLayoutManager(activity)
    }

    /*
    11월 9일 팀장 박신우 개발 메모입니다.
    users 데이터베이스를 사용하는 방식으로 변경했습니다.
     */
    inner class FollowerListFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val userDTOs: ArrayList<UserDTO>

        init {
            userDTOs = ArrayList()

            firestore?.collection("follows")?.document(selectedUid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var followerDTO = task.result.toObject(FollowDTO::class.java)

                    if(followerDTO == null){
                        // 팔로워, 팔로잉이 아예 없는 경우
                        // 해당 seletedUid로 조회했을때 이 유저를 팔로잉 하는 사람이 없다면
                        // 토스트 메세지를 출력해주고 사용자 프로필 화면으로 다시 이동시켜줍니다.
                        Toast.makeText(activity,"이 사용자는 팔로워 수가 0입니다.", Toast.LENGTH_LONG).show()

                        // 넘어가야할 프레그먼트인 유저 프로필 fragment
                        val fragment = UserProfileFragment()
                        val bundle = Bundle()

                        bundle.putString("destinationUid", selectedUid)
                        bundle.putString("userEmail", selectedUserEmail)

                        fragment.arguments = bundle

                        // 프레그먼트를 전환합니다.
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
                    }else {
                        getUserInfo(followerDTO.followers)
                    }
                }
            }
        }

        fun getUserInfo(followers: MutableMap<String, Boolean>) {

            followerListenerRegistration = firestore?.collection("users")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                userDTOs.clear()

                for (snapshot in querySnapshot!!.documents) {

                    var item = snapshot.toObject(UserDTO::class.java)

                    if (followers.keys.contains(item.uid)) {
                        userDTOs.add(item)
                    }
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context).inflate(R.layout.recyclerview_item_design_follower_list, parent, false)

            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return userDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder).itemView

            // 프로필 이미지 가져오기
            firestore?.collection("profileImages")?.document(userDTOs[position].uid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var url = task.result["image"]

                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewHolder.follower_list_imageview_profile)
                }
            }
            // 이메일 아이디 가져오기
            viewHolder.follower_list_email.text = userDTOs!![position].userEmail

            // 프로필 사진 누르면 해당 프로필로 이동하는 기능
            viewHolder.follower_list_imageview_profile.setOnClickListener {

                var fragment = UserProfileFragment()
                var bundle = Bundle()

                bundle.putString("destinationUid", userDTOs[position].uid)
                bundle.putString("userId", userDTOs[position].userEmail)

                fragment.arguments = bundle
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
            }
        }
    }
}
