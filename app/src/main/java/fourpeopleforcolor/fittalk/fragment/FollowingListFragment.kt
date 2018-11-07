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
import fourpeopleforcolor.fittalk.data_trasfer_object.PhotoDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_following_list.view.*
import kotlinx.android.synthetic.main.recyclerview_item_design_following_list.view.*

/*
* 사용자가 팔로잉하는 유저들의 목록을 띄우는 fragment입니다.
 */

/*
* UserProfileFragment에서 넘어온 uid, UserEmail 값을 활용합니다.
* 넘어온 uid를 기준으로 이 uid를 가진 유저가 팔로잉 하는 유저들(팔로잉)의 정보를 가져와야 합니다.
 */


class FollowingListFragment : Fragment() {

    var fragmentView : View? = null

    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    // 현재 사용자의 uid
    var currentUserUid : String? = null

    // 현재 사용자가 선택한 사용자의 uid
    // UserProfileFragment에서 팔로잉 수 관련 숫자, 텍스트를 누르면 팔로잉 리스트 프레그먼트로 전환되는 기능을 구현했습니다.
    // 그때 선택된 사용자의 uid를 인자로 넘겼습니다. 그 값을 받는 변수가 selectedUid입니다.
    // UserProfileFragment.kt를 참고하세요
    var selectedUid : String? = null

    // 현재 사용자가 선택한 사용자의 userEmail
    var selectedUserEmail : String? = null

    var followingListenerRegistration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid
        fragmentView = inflater.inflate(R.layout.fragment_following_list, container, false)

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
        followingListenerRegistration?.remove()
    }

    override fun onResume() {
        super.onResume()
        fragmentView?.following_list_recyclerview?.adapter = FollowingListFragmentRecyclerViewAdapter()
        fragmentView?.following_list_recyclerview?.layoutManager = LinearLayoutManager(activity)
    }

    inner class FollowingListFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val photoDTOs: ArrayList<PhotoDTO>
        val photoUidSet: HashSet<String>

        init {
            photoDTOs = ArrayList()
            /* 11월 7일 팀장 박신우의 개발 메모입니다.
               Set은 중복 키를 허용하지 않는다는 점을 명심하세요
               하나의 사용자가 게시글을 여러개 올렸다 하더도 팔로워 목록에는 그 사용자가 한번만 표시되야합니다.
                */
            photoUidSet = HashSet()

            firestore?.collection("follows")?.document(selectedUid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var followingDTO = task.result.toObject(FollowDTO::class.java)

                    if(followingDTO == null){
                        // 팔로워, 팔로잉이 아예 없는 경우
                        // 해당 seletedUid로 조회했을때 이 유저를 팔로잉 하는 사람이 없다면
                        // 토스트 메세지를 출력해주고 사용자 프로필 화면으로 다시 이동시켜줍니다.
                        Toast.makeText(activity,"이 사용자는 아무도 팔로우 하고 있지 않습니다.", Toast.LENGTH_LONG).show()

                        // 넘어가야할 프레그먼트인 유저 프로필 fragment
                        val fragment = UserProfileFragment()
                        val bundle = Bundle()

                        bundle.putString("destinationUid", selectedUid)
                        bundle.putString("userEmail", selectedUserEmail)

                        fragment.arguments = bundle

                        // 프레그먼트를 전환합니다.
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
                    }else {
                        getPhotos(followingDTO.followings)
                    }
                }
            }
        }

        // 실질적으로 사진을 사용하는건 아니고 사진 데이터베이스에 있는 정보만 사용합니다.
        fun getPhotos(followings: MutableMap<String, Boolean>) {

            followingListenerRegistration = firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                photoDTOs.clear()
                photoUidSet.clear()

                for (snapshot in querySnapshot!!.documents) {

                    var item = snapshot.toObject(PhotoDTO::class.java)

                    if (followings.keys.contains(item.uid)) {
                        photoDTOs.add(item)
                        // 게시물을 올린 사람의 uid를 담습니다. Set이니까 중복은 허용 안됩니다.
                        photoUidSet.add(item.uid!!)
                    }
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context).inflate(R.layout.recyclerview_item_design_following_list, parent, false)

            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            // Set의 사이즈만큼만 화면에 뿌릴겁니다. 중복이 허용안되니 uid 당 1번씩만 뿌려집니다.
            return photoUidSet.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as FollowingListFragment.FollowingListFragmentRecyclerViewAdapter.CustomViewHolder).itemView

            // 프로필 이미지 가져오기
            /* 11월 7일 팀장 박신우 개발 메모입니다.
             * 프로필 이미지 등록 안한 경우에 대한 예외처리 필요합니다.
             * 예외처리를 안하니까 프로필 이미지 등록을 안한 계정에 대해서는 동작 안하는 기능이 몇가지가 있습니다.
            */
            firestore?.collection("profileImages")?.document(photoDTOs[position].uid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var url = task.result["image"]

                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewHolder.following_list_imageview_profile)
                }
            }

            // 이메일 아이디 가져오기
            viewHolder.following_list_email.text = photoDTOs!![position].userEmail

            // 프로필 사진 누르면 해당 프로필로 이동하는 기능
            viewHolder.following_list_imageview_profile.setOnClickListener {

                var fragment = UserProfileFragment()
                var bundle = Bundle()

                bundle.putString("destinationUid", photoDTOs[position].uid)
                bundle.putString("userId", photoDTOs[position].userEmail)

                fragment.arguments = bundle
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
            }
        }

    }


}
