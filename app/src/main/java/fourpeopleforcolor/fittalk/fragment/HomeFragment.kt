package fourpeopleforcolor.fittalk.fragment


//import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.FollowDTO
import fourpeopleforcolor.fittalk.data_trasfer_object.PhotoDTO
import fourpeopleforcolor.fittalk.navigation_activity.CommentActivity
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.recyclerview_item_design_home.view.*


class HomeFragment : Fragment() {

    var firestore: FirebaseFirestore? = null
    var user: FirebaseAuth? = null

    // 2018년 11월 6일 팀장 박신우 향후 푸쉬알람을 위해 FcmDTO를 추가해야합니다.

    var followingSnapshot: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance()

        //

        var view = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_home, container, false)

        return view
    }

    override fun onResume() {
        super.onResume()
        view?.fragmentHome_recyclerview?.adapter = HomeFragmentRecyclerviewAdapter()
        view?.fragmentHome_recyclerview?.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStop() {
        super.onStop()
        followingSnapshot?.remove()
    }

    inner class HomeFragmentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val photoDTOs: ArrayList<PhotoDTO>
        val photoUidList: ArrayList<String>

        init {

            photoDTOs = ArrayList()
            photoUidList = ArrayList()

            // 현재 로그인된 유저의 UID, 개인 해쉬 키
            var uid = FirebaseAuth.getInstance().currentUser?.uid


            firestore?.collection("follows")?.document(uid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var userDTO = task.result.toObject(FollowDTO::class.java)

                    if (userDTO != null) {
                        // 내가 팔로잉 하는 사람에 대한 정보를 넘겨줍니다.
                        getPhotos(userDTO.followings)
                    }
                }
            }

        }

        // 내가 팔로잉하는 사람의 게시물만 가져오기
        fun getPhotos(followings: MutableMap<String, Boolean>) {

            followingSnapshot = firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                photoDTOs.clear()
                photoUidList.clear()

                for (snapshot in querySnapshot!!.documents) {

                    var item = snapshot.toObject(PhotoDTO::class.java)

                    // 이미지 마다 uid를 읽고 내가 팔로잉 하는 사람에게 해당하면 가져옵니다.
                    if (followings.keys.contains(item.uid)) {
                        photoDTOs.add(item)
                        photoUidList.add(snapshot.id)
                    }
                }
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context).inflate(R.layout.recyclerview_item_design_home, parent, false)

            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        }

        override fun getItemCount(): Int {
            return photoDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder).itemView

            // 프로필 이미지 가져오기
            /* 11월 7일 팀장 박신우 개발 메모입니다.
             * 프로필 이미지 등록 안한 경우에 대한 예외처리 필요합니다.
             * 예외처리를 안하니까 프로필 이미지 등록을 안한 계정에 대해서는 동작 안하는 기능이 몇가지가 있습니다.
            */
            firestore?.collection("profileImages")?.document(photoDTOs[position].uid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var url = task.result["image"]

                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewHolder.detailviewitem_profile_image)
                }
            }

            // 유저 아이디
            viewHolder.detailviewitem_profile_textview.text = photoDTOs!![position].userEmail

            // 이미지
            Glide.with(holder.itemView.context).load(photoDTOs!![position].imageUrl).into(viewHolder.detailviewitem_imageview_content)

            // 설명 텍스트
            viewHolder.detailviewitem_explain_textview.text = photoDTOs!![position].feeling

            // 언더아머 카운터 설정
            viewHolder.detailviewitem_underarmourcounter_textview.text = "언더아머 " + photoDTOs!![position].underArmourCount + "개"

            var uid = FirebaseAuth.getInstance().currentUser!!.uid

            // 좋아요 이벤트 발생
            viewHolder.detailviewitem_underarmour_imageview.setOnClickListener {
                underarmourEvent(position)
            }

            // 좋아요를 클릭했을시 색칠된 이미지로 변경
            if (photoDTOs!![position].underArmours.containsKey(uid)) {
                viewHolder.detailviewitem_underarmour_imageview.setImageResource(R.drawable.ic_like_pink)
                // 좋아요를 클릭하지 않았을 경우
            } else {
                viewHolder.detailviewitem_underarmour_imageview.setImageResource(R.drawable.ic_like_basic)
            }

            // 프로필 사진 누르면 해당 프로필로 이동하는 기능
            viewHolder.detailviewitem_profile_image.setOnClickListener {

                var fragment = UserProfileFragment()
                var bundle = Bundle()

                bundle.putString("destinationUid", photoDTOs[position].uid)
                bundle.putString("userId", photoDTOs[position].userEmail)

                fragment.arguments = bundle
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
            }

            // 댓글 사진 누르면 코멘트 화면으로 넘어가는 기능
            viewHolder.detailviewitem_comment_imageview.setOnClickListener { view ->


                var intent = Intent(view.context, CommentActivity::class.java)

                // 선택한 이미지에 해당하는 정보를 긁어오기 위함
                intent.putExtra("photoUid", photoUidList[position])

                // 코멘트 엑티비티에서 알람 이벤트 함수에 넘겨줄 파라미터
                intent.putExtra("destinationUid", photoDTOs[position].uid)

                startActivity(intent)

            }
        }

        // 좋아요 이벤트
        private fun underarmourEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(photoUidList[position])
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser!!.uid
                // 트랜잭션 방식으로 데이터를 가져오기 때문에 읽는 동안에는 다른 사용자가 접근 못함
                var photoDTO = transaction.get(tsDoc!!).toObject(PhotoDTO::class.java)

                // 좋아요를 누른 상태에는 고유 해쉬값이 있을것이고 그게 있는지 체크하는것입니다.
                if (photoDTO!!.underArmours.containsKey(uid)) {
                    // 좋아요를 누른 상태 -> 누르지 않은 상태로 넘어가는것
                    photoDTO?.underArmourCount = photoDTO?.underArmourCount - 1
                    photoDTO?.underArmours.remove(uid)


                } else {
                    // 좋아요를 누르지 않은 상태 -> 누르는 상태로 넘어가는것
                    photoDTO?.underArmours[uid] = true
                    photoDTO?.underArmourCount = photoDTO?.underArmourCount + 1

                    //

                }
                // photoDTO 만든 값을 지정한 경로에 set 해주는것
                transaction.set(tsDoc, photoDTO)
            }

        }
    }
}
