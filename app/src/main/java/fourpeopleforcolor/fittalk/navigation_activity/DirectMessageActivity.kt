package fourpeopleforcolor.fittalk.navigation_activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.DirectMessageDTO
import fourpeopleforcolor.fittalk.util.DirectMessageRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_direct_message.*

class DirectMessageActivity : AppCompatActivity(){

    var messageDTOs : ArrayList<DirectMessageDTO.Message>? = null
    var currentUserUid : String? = null
    var selectedUid : String? = null

    var directMessageSnapshot : ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct_message)

        currentUserUid = intent.getStringExtra("currentUid")
        selectedUid = intent.getStringExtra("destinationUid")
        var title = currentUserUid + " To " + selectedUid

        messageDTOs = ArrayList()

        directMessageSnapshot = FirebaseFirestore.getInstance().collection("chattingRoom").document(title).collection("messages").orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

            if (querySnapshot == null) return@addSnapshotListener

            messageDTOs!!.clear()

            for (snapshot in querySnapshot.documents!!) {
                messageDTOs!!.add(snapshot.toObject(DirectMessageDTO.Message::class.java))
            }
        }

        recyclerview.adapter = DirectMessageRecyclerViewAdapter(messageDTOs!! ,currentUserUid!!, selectedUid!!)
        recyclerview.layoutManager = LinearLayoutManager(this)

        recyclerview.adapter.notifyDataSetChanged()
        if(messageDTOs!!.isNotEmpty()){
            recyclerview.smoothScrollToPosition(messageDTOs!!.size-1)
        }

        direct_message_btn.setOnClickListener {
            // 빈 문자열이 아닐 경우에 메세지를 보낼 수 있게 해줍니다.
            if(!TextUtils.isEmpty(direct_message_text.text)){

                // 채팅창 생성
                var direct_messageDTO = DirectMessageDTO()
                direct_messageDTO.directMessageRoom[currentUserUid!!] = selectedUid.toString()

                FirebaseFirestore.getInstance()?.collection("chattingRoom")?.document(title)?.set(direct_messageDTO)

                var message = DirectMessageDTO.Message()
                message.timestamp = System.currentTimeMillis()
                message.directMessage[currentUserUid!!] = direct_message_text.text.toString()
                FirebaseFirestore.getInstance().collection("chattingRoom").document(title).collection("messages").document(title+message.timestamp).set(message)

                directMessageSnapshot = FirebaseFirestore.getInstance().collection("chattingRoom").document(title).collection("messages").orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    if (querySnapshot == null) return@addSnapshotListener

                    messageDTOs!!.clear()

                    for (snapshot in querySnapshot.documents!!) {
                        messageDTOs!!.add(snapshot.toObject(DirectMessageDTO.Message::class.java))
                    }
                }

                // 내가 전송한 메세지가 recyclerview 화면에 뿌려지게끔 새로고침 합니다.
                recyclerview.adapter.notifyDataSetChanged()

                // 끝 위치로 이동합니다.
                if(messageDTOs!!.isNotEmpty()){
                    recyclerview.smoothScrollToPosition(messageDTOs!!.size-1)
                }

                // 메세지 입력창을 빈문자열로 초기화 해줍니다.
                direct_message_text.setText("")
            }
        }

    }
}