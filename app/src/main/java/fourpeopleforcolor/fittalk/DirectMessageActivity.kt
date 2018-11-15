package fourpeopleforcolor.fittalk

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import fourpeopleforcolor.fittalk.data_trasfer_object.DirectMessageDTO
import kotlinx.android.synthetic.main.activity_direct_message.*
import kotlinx.android.synthetic.main.recyclerview_item_design_direct_message.view.*

/*
11월 14일 팀장 박신우의 개발 메모입니다.
Direct Message 즉 실시간 메신저 기능입니다.
 */

class DirectMessageActivity : AppCompatActivity(){

    // 다른 사람의 프로필로 들어가서 처음 채팅을 거는 사람의 uid입니다
    var currentUserUid : String? = null
    // 채팅을 받는 사람의 uid입니다.
    var selectedUid : String? = null


    /*
    currentUserUid, selectedUid는 다른 사람의 프로필에서 채팅방에 입장하는 경우에 넘어오는 값이고
    아래 두 값은 내가 내 프로필을 통해 나한테 온 메세지들의 채팅방에 입장하는 경우에 넘어오는 값입니다.
     */
    // 다른 사람의 프로필로 들어가서 처음 채팅을 거는 사람의 uid입니다
    var guestUid : String? = null
    // 채팅을 받는 사람의 uid입니다.
    var ownerUid : String? = null

    var directMessageSnapshot : ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct_message)

        currentUserUid = intent.getStringExtra("currentUid")
        selectedUid = intent.getStringExtra("destinationUid")
        //var title_alpha = currentUserUid + " " + selectedUid
        var title_alpha : String? = ""
        if(currentUserUid != null && selectedUid != null){
            title_alpha = (currentUserUid!!.hashCode() + selectedUid!!.hashCode()).toString()
        }

        guestUid = intent.getStringExtra("guestUid")
        ownerUid = intent.getStringExtra("ownerUid")
        //var title_omega = guestUid + " " + ownerUid
        var title_omega : String? = ""
        if(guestUid != null && ownerUid != null){
            title_omega = (guestUid!!.hashCode() + ownerUid!!.hashCode()).toString()
        }

        direct_message_btn.setOnClickListener {
            // 빈 문자열이 아닐 경우에 메세지를 보낼 수 있게 해줍니다.
            if(!TextUtils.isEmpty(direct_message_text.text)){

                if(currentUserUid != null && selectedUid != null){

                    // 채팅창 생성
                    var direct_messageDTO = DirectMessageDTO()
                    direct_messageDTO.directMessageRoom[currentUserUid!!] = selectedUid.toString()
                    FirebaseFirestore.getInstance()?.collection("directMessageRoom")?.document(title_alpha!!)?.set(direct_messageDTO)

                    // 채팅창 하위에 댓글 생성
                    var message = DirectMessageDTO.Message()
                    message.timestamp = System.currentTimeMillis()
                    message.directMessage[currentUserUid!!] = direct_message_text.text.toString()
                    FirebaseFirestore.getInstance().collection("directMessageRoom").document(title_alpha!!).collection("messages").document(title_alpha+message.timestamp).set(message)

                    // 메세지 입력창을 빈문자열로 초기화 해줍니다.
                    direct_message_text.setText("")
                }else if(ownerUid != null && guestUid != null){

                    // 채팅창 하위에 댓글 생성
                    var message = DirectMessageDTO.Message()
                    message.timestamp = System.currentTimeMillis()
                    message.directMessage[ownerUid!!] = direct_message_text.text.toString()
                    FirebaseFirestore.getInstance().collection("directMessageRoom").document(title_omega!!).collection("messages").document(title_omega+message.timestamp).set(message)

                    // 메세지 입력창을 빈문자열로 초기화 해줍니다.
                    direct_message_text.setText("")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dm_recyclerview.adapter = DirectMessageRecyclerViewAdapter()
        dm_recyclerview.layoutManager = LinearLayoutManager(this)
    }

    override fun onStop() {
        super.onStop()
        directMessageSnapshot?.remove()
    }

    inner  class DirectMessageRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val message : ArrayList<DirectMessageDTO.Message>
        init {
            message = ArrayList()

            if(currentUserUid != null && selectedUid != null){

                //var title_alpha = currentUserUid + " " + selectedUid
                var title_alpha = (currentUserUid!!.hashCode() + selectedUid!!.hashCode()).toString()

                directMessageSnapshot = FirebaseFirestore.getInstance().collection("directMessageRoom").document(title_alpha).collection("messages").orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    if(querySnapshot == null) return@addSnapshotListener

                    message.clear()

                    for(snapshot in querySnapshot.documents!!){
                        message.add(snapshot.toObject(DirectMessageDTO.Message::class.java))
                    }

                    notifyDataSetChanged()
                    /*
                     if(message.isNotEmpty()){
                        dm_recyclerview.adapter.notifyDataSetChanged()
                        dm_recyclerview.layoutManager.scrollToPosition(message.size-1)
                     }
                     */

                }

            }else if(ownerUid != null && guestUid != null){

                //var title_omega = guestUid + " " + ownerUid
                var title_omega = (guestUid!!.hashCode() + ownerUid!!.hashCode()).toString()

                directMessageSnapshot = FirebaseFirestore.getInstance().collection("directMessageRoom").document(title_omega).collection("messages").orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    if(querySnapshot == null) return@addSnapshotListener

                    message.clear()

                    for(snapshot in querySnapshot.documents!!){
                        message.add(snapshot.toObject(DirectMessageDTO.Message::class.java))
                    }

                    notifyDataSetChanged()
                    /*
                     if(message.isNotEmpty()){
                        dm_recyclerview.adapter.notifyDataSetChanged()
                        dm_recyclerview.layoutManager.scrollToPosition(message.size-1)
                     }
                    */
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_design_direct_message, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return message.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if(currentUserUid != null && selectedUid != null){
                if(message[position].directMessage.containsKey(currentUserUid!!)){
                    holder.itemView.right_DMbubble.visibility = View.VISIBLE
                    holder.itemView.right_DMbubble.text = message[position].directMessage[currentUserUid!!]
                    holder.itemView.left_DMbubble.visibility = View.INVISIBLE
                }else{
                    holder.itemView.left_DMbubble.visibility = View.VISIBLE
                    holder.itemView.left_DMbubble.text = message[position].directMessage[selectedUid!!]
                    holder.itemView.right_DMbubble.visibility = View.INVISIBLE
                }
            }else if(ownerUid != null && guestUid != null){
                if(message[position].directMessage.containsKey(ownerUid!!)){
                    holder.itemView.right_DMbubble.visibility = View.VISIBLE
                    holder.itemView.right_DMbubble.text = message[position].directMessage[ownerUid!!]
                    holder.itemView.left_DMbubble.visibility = View.INVISIBLE
                }else{
                    holder.itemView.left_DMbubble.visibility = View.VISIBLE
                    holder.itemView.left_DMbubble.text = message[position].directMessage[guestUid!!]
                    holder.itemView.right_DMbubble.visibility = View.INVISIBLE
                }
            }
        }

    }
}