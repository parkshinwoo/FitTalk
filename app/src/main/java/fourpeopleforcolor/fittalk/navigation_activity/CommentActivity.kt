package fourpeopleforcolor.fittalk.navigation_activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import fourpeopleforcolor.fittalk.data_trasfer_object.AlarmDTO
import fourpeopleforcolor.fittalk.data_trasfer_object.PhotoDTO
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.recyclerview_item_design_comment.view.*

class CommentActivity : AppCompatActivity() {

    // 이미지 콜렉션에 댓글 콜렉션을 추가로 붙여야 하는데 그 이미지 콜렉션 정보를 긁어오기 위한 변수입니다.
    // 파이어베이스 데이터베이스에 image 콜렉션이 기존에 존재하는데 그 하위 콜렉션으로 comment를 추가합니다.
    var photoUid: String? = null

    var user : FirebaseAuth? = null
    var destinationUid : String? = null

    var firestore : FirebaseFirestore? = null

    // 2018년 11월 9일 팀장 박신우의 개발 메모입니다. Fcm, 즉 파이어베이스 클라우드 메세징을 위한 변수를 향후 추가하고 백그라운드 푸쉬알람을 구현해야합니다.

    var commentSnapshot : ListenerRegistration? = null

    var imageListenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        user = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()

        // 이미지 컬렉션 정보를 가져옵니다.
        // activity일때는 intent를 쓰고 fragment일때는 arguments를 쓴다는 점을 구분해주세요
        // 번들 사용해서 사용자 정보 가져올때 한 예제와 비슷합니다.
        // 아래 두 값은 HomeFragment.kt 파일에서 넘어옵니다.
        photoUid = intent.getStringExtra("photoUid")
        destinationUid = intent.getStringExtra("destinationUid")

        //

        // 댓글 달기 버튼에 붙이는 이벤트
        comment_btn_send.setOnClickListener {
            var comment = PhotoDTO.Comment()
            comment.userEmail = FirebaseAuth.getInstance().currentUser!!.email
            comment.comment = comment_edit_message.text.toString()
            comment.uid = FirebaseAuth.getInstance().currentUser!!.uid
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(photoUid!!).collection("comments").document().set(comment)

            /*
              11월 10일 팀장 박신우의 개발 메모입니다.
              알람 화면을 위한 함수 호출입니다.
            */
            commentAlarm(destinationUid!!, comment_edit_message.text.toString())

            // 댓글 달기 버튼을 누르고 나면 기존에 있던 내용은 초기화
            comment_edit_message.setText("")
        }

        getImage()
    }

    fun getImage(){
        imageListenerRegistration = firestore?.collection("images")?.document(photoUid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var url = documentSnapshot?.data!!["imageUrl"]
            Glide.with(this).load(url).into(main_image)
        }
    }

    fun commentAlarm(destinationUid : String, message : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userEmail = user?.currentUser?.email
        alarmDTO.uid = user?.currentUser?.uid
        alarmDTO.kind = 1
        alarmDTO.message = message
        alarmDTO.timestamp = System.currentTimeMillis()

        var title = alarmDTO.userEmail + alarmDTO.kind + alarmDTO.timestamp

        FirebaseFirestore.getInstance().collection("alarms").document(title).set(alarmDTO)

        //
    }

    override fun onResume() {
        super.onResume()
        comment_recyclerview.adapter = CommentRecyclerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)
    }

    override fun onStop() {
        super.onStop()
        commentSnapshot?.remove()
    }

    inner class CommentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        // 댓글을 가져옵니다.
        val comments : ArrayList<PhotoDTO.Comment>
        init {
            comments = ArrayList()

            commentSnapshot = FirebaseFirestore.getInstance().collection("images").document(photoUid!!).collection("comments").orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if(querySnapshot == null) return@addSnapshotListener

                comments.clear()

                for(snapshot in querySnapshot.documents!!){
                    comments.add(snapshot.toObject(PhotoDTO.Comment::class.java))
                }
                // 어뎁터 새로고침해서 리사이클러뷰 다시 그리기
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context).inflate(R.layout.recyclerview_item_design_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = (holder as CustomViewHolder).itemView
            view.commentviewItem_textview_comment.text = comments[position].comment
            view.commentviewItem_textview_profile.text = comments[position].userEmail

            // 프로필 이미지 가져오기
            /* 11월 7일 팀장 박신우 개발 메모입니다.
             * 프로필 이미지 등록 안한 경우에 대한 예외처리 필요합니다.
             * 예외처리를 안하니까 프로필 이미지 등록을 안한 계정에 대해서는 동작 안하는 기능이 몇가지가 있습니다.
            */
            FirebaseFirestore.getInstance().collection("profileImages")?.document(comments[position].uid!!)?.get()?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    var url = task.result["image"]

                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewItem_imageview_profile)
                }
            }
        }
    }
}
