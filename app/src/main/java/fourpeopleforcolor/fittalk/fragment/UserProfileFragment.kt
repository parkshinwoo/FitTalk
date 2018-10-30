package fourpeopleforcolor.fittalk.fragment

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import fourpeopleforcolor.fittalk.LoginActivity
import fourpeopleforcolor.fittalk.MainActivity
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.FollowDTO
import fourpeopleforcolor.fittalk.data_trasfer_object.PhotoDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.fragment_user_profile.view.*



class UserProfileFragment : Fragment() {

    var fragmentView : View? = null

    var PICK_PROFILE_FROM_ALBUM = 0
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    // 현재 사용자의 uid
    var currentUserUid : String? = null

    // 현재 사용자가 선택한 사용자의 uid
    // SearchFragment에서 사진을 선택하면 유저 프로필 프레그먼트로 전환되는 기능을 구현했습니다.
    // 그때 선택된 사용자의 uid를 인자로 넘겼습니다. 그 값을 받는 변수가 selectedUid입니다.
    // SearchFragment.kt의 126줄 부터 참고하세요.
    var selectedUid : String? = null




    // 2018년 9월 26일 팀장 박신우의 개발 메모입니다.
    // snapshot은 항상 데이터베이스를 지켜보다가 변경사항이 생기면 뷰한테 던져주는 역할을 합니다.
    // 현재 사용하는 파이어베이스 데이터베이스가 push driven 방식이므로 스냅샷을 쓰면
    // 실시간으로 변하는 데이터를 바로바로 화면에 반영할 수 있습니다.
    // 만약 뷰가 백그라운드로 가있는데 스냅샷이 데이터를 던져준다면 에러가 발생할 것입니다.
    // 그러므로 Registration에 스냅샷을 등록해놓고 뷰가 백그라운드에 가 있거나
    // 뷰가 꺼질때는 스냅샷도 함께 꺼줘야지만 안정적으로 앱이 동작합니다.
    var followerListenerRegistration: ListenerRegistration? = null
    var followingListenerRegistration: ListenerRegistration? = null
    var imageProfileListenerRegistration: ListenerRegistration? = null
    var photoRecyclerViewListenerRegistration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        auth = FirebaseAuth.getInstance()

        currentUserUid = auth?.currentUser?.uid
        firestore = FirebaseFirestore.getInstance()

        fragmentView = inflater.inflate(R.layout.fragment_user_profile, container, false)




        if(arguments != null){
            // arguments가 null이 아니라면 넘어온 값이 있는 것입니다.
            // 선택된 사람의 uid
            selectedUid = arguments?.getString("destinationUid")

            if(selectedUid != null && selectedUid == currentUserUid){
                // 선택된 사람의 uid가 현재 사용자의 uid와 같은 경우입니다.
                // 즉 자신이 올린 사진을 클릭해서 넘어온 상황입니다.
                // 자신의 유저 프로필 화면과 타인의 유저 프로필 화면의 구성은 달라야합니다.

                // 자신의 유저 프로필 화면에는 로그아웃 기능이 있습니다.
                fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
                // 로그아웃 버튼을 클릭했을때 발생하는 이벤트입니다.
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    // 현재 액티비티를 종료합니다.
                    activity?.finish()
                    // 로그인 화면으로 돌아가게 하기 위해 로그인 액티비티를 실행합니다.
                    startActivity(Intent(activity, LoginActivity::class.java))
                    // 파이어베이스 계정 관리 기능을 사용해서 사용자의 계정을 로그아웃합니다.
                    auth?.signOut()
                }

                // 프로필 사진을 클릭하면 발생하는 이벤트입니다.
                fragmentView?.account_profile?.setOnClickListener {
                    var photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"

                    // 현재 유저 프로필 fragment는 main activity에 붙어있는 하위 화면입니다.
                    // upload_photo_activity의 경우는 자체적으로 activity이므로 디바이스의 앨범을 열고 사진을 가져오는 행위가 가능했습니다.
                    // 유저 프로필 fragment는 자신이 붙어있는 activity인 main activity의 도움을 받아야합니다.
                    // MainActivity.kt에 추가된 코드를 참고하세요
                    // MainActivity.kt에서 아래 문장(startActivityForResult)의 결과를 받아서 수행합니다.
                    activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)


                    val ft = fragmentManager!!.beginTransaction()
                    ft.detach(this).attach(this).commit()
                    //프로필 사진이 바뀌었을 시에 fragment를 reload 하는 구문입니다.

                }
            }else{
                // 사용자가 다른 유저의 사진을 클릭해서 다른 유저의 프로필 화면으로 넘어간 경우입니다.
                // 다른 유저의 프로필 화면에서는 팔로우 하기 버튼이 보여야합니다.
                fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)

                // 또한 다른 유저의 프로필 화면에서는 상당 툴바의 디자인이 달라야합니다.
                // fragment는 activity에 붙는 하위 화면이라고 했습니다.
                // 현재 유저 프로필 fragment는 main activity에 붙어있습니다.
                var mainActivity = (activity as MainActivity)
                mainActivity.toolbar_title_image.visibility = View.GONE
                mainActivity.toolbar_btn_back.visibility = View.VISIBLE
                mainActivity.toolbar_username.visibility = View.VISIBLE
                // SearchFragment에서 받아온 인자 값입니다.
                mainActivity.toolbar_username.text = arguments?.getString("userEmail")

                // 툴바의 뒤로가기 버튼을 클릭시 발생하는 이벤트입니다.
                mainActivity.toolbar_btn_back.setOnClickListener {
                    // 하단 네비게이션바의 메뉴 아이템으로 홈 화면을 지정해서
                    // 홈화면으로 이동시킵니다.
                    mainActivity.bottom_navigation_top.selectedItemId = R.id.action_home
                }

                // 다른 유저의 프로필 화면에서는 로그아웃 버튼이 아닌 팔로우 하기 버튼입니다.
                // 팔로우 하기 버튼에 이벤트를 발생시킵니다.
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    requestFollow()
                }
            }
        }

        return fragmentView
    }

    override fun onResume() {
        super.onResume()

        getProfileImages()
        getFollowing()
        getFollower()

        fragmentView?.account_recyclerview?.adapter = UserProfileFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

    }

    override fun onStop() {
        super.onStop()

        followingListenerRegistration?.remove()
        followerListenerRegistration?.remove()
        imageProfileListenerRegistration?.remove()
        photoRecyclerViewListenerRegistration?.remove()
    }

    // 팔로우 요청을 수행하는 함수입니다.
    fun requestFollow(){

        // 파이어베이스 데이터베이스에 새 collection을 만듭니다.
        // 이름은 follows입니다. 팔로우 관련 정보를 담습니다.

        // 현재 사용자의 uid로 document를 하나 만듭니다.
        // 현재 사용자가 팔로잉 하는 경우를 위한 변수입니다.
        var DocumentFollowing = firestore!!.collection("follows").document(currentUserUid!!)

        firestore?.runTransaction {
            transaction ->
            // 수행 결과로 넘어온것을 정의한 FollowDTO 타입으로 캐스팅합니다.
            var followDTO = transaction.get(DocumentFollowing).toObject(FollowDTO::class.java)

            if(followDTO == null){
                // followDTO가 null이라면 아직 아무도 팔로잉 하고 있지 않은 상황입니다.
                followDTO = FollowDTO()
                // 팔로잉 카운트를 1도 맞춥니다.
                followDTO.followingCount = 1
                // 선택된 유저 즉 현재 유저가 팔로잉 하기로 결정한 유저의 uid와 함께 팔로잉 하기로 결정한 여부인 true를 지정합니다.
                followDTO.followings[selectedUid!!] = true

                // 파이어베이스 데이터베이스에 변경 사항을 저장합니다.
                transaction.set(DocumentFollowing, followDTO)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(selectedUid)){
                // 팔로잉 하기로 한 유저의 uid 값이
                // followings hashmap에 이미 존재한다면
                // 이미 팔로잉 하고 있는 경우입니다.
                // 이미 팔로잉하고 있는 상황에서 또 팔로우 버튼을 누르면
                // 팔로잉 취소를 시켜줍니다.
                // 팔로잉 수를 하나 줄입니다.
                followDTO?.followingCount = followDTO?.followingCount - 1
                // 해쉬맵에서 선택된 유저의 uid를 지웁니다.
                followDTO?.followings.remove(selectedUid)
            }else{
                // followDTO가 null이 아닙니다.
                // 즉 누군가를 팔로잉 하고 있기는 하지만
                // 현재 선택한 유저가 아닐 경우입니다.
                // 팔로잉 처리 해줍니다.
                followDTO.followingCount = followDTO.followingCount + 1
                followDTO.followings[selectedUid!!] = true
            }
            transaction.set(DocumentFollowing, followDTO)
            return@runTransaction
        }


        // 사용자에게 선택된 사용자의 uid(타인의 uid)로 document를 하나 만듭니다.
        // 현재 사용자를 다른 유저가 팔로워 하는 경우를 위한 변수입니다.
        var DocumentFollower = firestore!!.collection("follows").document(selectedUid!!)

        firestore?.runTransaction {
            transaction ->
            var followDTO = transaction.get(DocumentFollower).toObject(FollowDTO::class.java)

            if(followDTO == null){
                // 현재 사용자를 팔로우 하는 사람이 아무도 없는 경우입니다.
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                transaction.set(DocumentFollower, followDTO)
                return@runTransaction
            }

            if(followDTO.followers.containsKey(currentUserUid!!)){
                // 이미 나를 팔로우하고 있는 유저가 또 팔로우 버튼을 누른다면
                // 팔로우를 취소하게 처리해줍니다.
                followDTO!!.followerCount = followDTO.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)

                transaction.set(DocumentFollower, followDTO)
            }else{

                followDTO.followerCount = followDTO.followerCount + 1
                followDTO.followers[currentUserUid!!] = true


            }
            transaction.set(DocumentFollower, followDTO)
            return@runTransaction
        }
    }

    // 프로필 이미지를 가져오는 함수입니다.
    fun getProfileImages(){
        imageProfileListenerRegistration = firestore?.collection("profileImages")?.document(selectedUid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

            if (documentSnapshot == null) return@addSnapshotListener

            if (documentSnapshot.data != null) {
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView!!.account_profile)
            }
        }
    }

    // 유저 프로필 화면에 표시되는 유저를 팔로잉하는 수를 가져오는 함수입니다.
    fun getFollower(){

        followerListenerRegistration = firestore?.collection("follows")?.document(selectedUid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->


            if(documentSnapshot == null) return@addSnapshotListener

            val followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO == null) return@addSnapshotListener

            // 팔로워 수를 표시합니다.
            fragmentView?.account_follower_count?.text = followDTO?.followerCount.toString()

            // 다른 유저가 현재 사용자를 팔로잉 하고 있는 경우와 그렇지 않은 경우의 디자인은 달라야합니다.
            if(followDTO?.followers?.containsKey(currentUserUid)!!){
                // 다른 유저가 현재 사용자를 이미 팔로잉 하고 있는 경우 팔로우 취소 버튼을 보여줍니다.
                fragmentView?.account_btn_follow_signout?.text = getText(R.string.follow_cancel)
                fragmentView?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(activity!!, R.color.violet), PorterDuff.Mode.MULTIPLY)
            }else{
                if(selectedUid != currentUserUid){
                    // 다른 유저가 현재 사용자를 팔로잉 하고 있지 않으면 팔로잉 버튼을 보여줍니다.
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                    fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                }
            }
        }
    }

    // 현재 사용자가 팔로잉 하는 유저의 수를 가져오는 함수입니다.
    fun getFollowing(){
        followingListenerRegistration = firestore?.collection("follows")?.document(selectedUid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

            if(documentSnapshot == null) return@addSnapshotListener

            val followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO == null) return@addSnapshotListener

            fragmentView?.account_following_count?.text = followDTO?.followingCount.toString()
        }
    }

    inner class UserProfileFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var photoDTOs : ArrayList<PhotoDTO>

        init {
            photoDTOs = ArrayList()

            photoRecyclerViewListenerRegistration = firestore?.collection("images")?.whereEqualTo("uid", selectedUid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if(querySnapshot == null) return@addSnapshotListener

                // 데이터베이스로 부터 데이터를 긁어오고 PhotoDTO 타입으로 캐스팅해서 그릇에 담습니다.
                for (snapshot in querySnapshot.documents){
                    photoDTOs.add(snapshot.toObject(PhotoDTO::class.java))
                }

                // 게시물 개수를 표시해줍니다.
                account_post_count.text = photoDTOs.size.toString()

                notifyDataSetChanged()

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var height = resources.displayMetrics.widthPixels / 3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = ViewGroup.LayoutParams(width, height)

            return CustomViewHolder(imageView)
        }
        private inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount(): Int {
            return photoDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(photoDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

    }


}
