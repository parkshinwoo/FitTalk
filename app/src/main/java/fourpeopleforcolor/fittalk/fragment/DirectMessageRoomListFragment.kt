package fourpeopleforcolor.fittalk.fragment

import android.content.Intent
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
import fourpeopleforcolor.fittalk.DirectMessageActivity
import fourpeopleforcolor.fittalk.MainActivity
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.DirectMessageDTO
import fourpeopleforcolor.fittalk.data_trasfer_object.UserDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_direct_message_room_list.view.*
import kotlinx.android.synthetic.main.recyclerview_item_design_direct_message_room_list.view.*

/*
11월 14일 팀장 박신우의 개발 메모입니다.
채팅방의 목록을 구성하는 프레그먼트 입니다.
 */

class DirectMessageRoomListFragment : Fragment() {

    var fragmentView : View? = null

    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    var currentUserUid : String? = null
    var currentUserEmail : String? = null
    var selectedUid : String? = null

    var dmRoomListenerRegistration: ListenerRegistration? = null
    var userListenerRegistration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fragmentView = inflater.inflate(R.layout.fragment_direct_message_room_list, container, false)

        currentUserUid = arguments?.getString("currentUid")
        currentUserEmail = arguments?.getString("currentUserEmail")

        var mainActivity = (activity as MainActivity)
        mainActivity.toolbar_btn_schedule.visibility = View.GONE
        mainActivity.toolbar_btn_direct_message.visibility = View.GONE

        return fragmentView
    }

    override fun onStop() {
        super.onStop()
        dmRoomListenerRegistration?.remove()
        userListenerRegistration?.remove()
    }

    override fun onResume() {
        super.onResume()
        fragmentView?.direct_message_room_list_recyclerview?.adapter = DirectMessageRoomListFragmentRecyclerViewAdapter()
        fragmentView?.direct_message_room_list_recyclerview?.layoutManager = LinearLayoutManager(activity)
    }

    inner  class DirectMessageRoomListFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val userDTOs : ArrayList<UserDTO>
        val tmpDTOs : ArrayList<UserDTO>
        val directMessageDTO : ArrayList<DirectMessageDTO>

        init {

            userDTOs = ArrayList()
            tmpDTOs = ArrayList()
            directMessageDTO = ArrayList()

            dmRoomListenerRegistration = firestore?.collection("directMessageRoom")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if(querySnapshot == null) return@addSnapshotListener

                if(querySnapshot.isEmpty){
                    Toast.makeText(activity!!,"개설된 Direct Message 방이 없습니다!", Toast.LENGTH_LONG).show()

                    var fragment = UserProfileFragment()
                    var bundle = Bundle()

                    bundle.putString("destinationUid", currentUserUid)
                    bundle.putString("userEmail", currentUserEmail)

                    fragment.arguments = bundle
                    activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
                }

                for (snapshot in querySnapshot!!.documents){
                    var directMessageRoom = snapshot.toObject(DirectMessageDTO::class.java)
                    directMessageDTO.add(directMessageRoom)
                    getUserInfo(directMessageDTO)
                }
                notifyDataSetChanged()
            }

        }

        fun getUserInfo(directMessageRoom: ArrayList<DirectMessageDTO>) {

            userListenerRegistration = firestore?.collection("users")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) return@addSnapshotListener

                userDTOs.clear()
                tmpDTOs.clear()

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(UserDTO::class.java)
                    tmpDTOs.add(item)
                }
                notifyDataSetChanged()

                var check_redundacy : String? = null

                for(dm in directMessageRoom.iterator()){
                    if(dm.directMessageRoom.containsValue(currentUserUid)){
                        for (tmp in tmpDTOs.iterator()) {
                            if (dm.directMessageRoom.containsKey(tmp.uid)) {
                                userDTOs.add(tmp)
                                check_redundacy = tmp.uid
                            }
                        }
                    }
                }

                for(dm in directMessageRoom.iterator()){
                    if(dm.directMessageRoom.containsKey(currentUserUid)) {
                        for (tmp in tmpDTOs.iterator()) {
                            if (dm.directMessageRoom.containsValue(tmp.uid)) {
                                if (check_redundacy.equals(tmp.uid)) {
                                } else {
                                    userDTOs.add(tmp)
                                }
                            }
                        }
                    }
                }

                if(userDTOs.isEmpty()){
                    Toast.makeText(activity!!,"개설된 Direct Message 방이 없습니다!", Toast.LENGTH_LONG).show()

                    var fragment = UserProfileFragment()
                    var bundle = Bundle()

                    bundle.putString("destinationUid", currentUserUid)
                    bundle.putString("userEmail", currentUserEmail)

                    fragment.arguments = bundle

                    activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
                }else{
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_design_direct_message_room_list, parent, false)

            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return userDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder).itemView

            firestore?.collection("profileImages")?.document(userDTOs[position].uid!!)?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    var url = task.result["image"]

                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewHolder.direct_message_room_list_imageview_profile)
                }
            }

            viewHolder.direct_message_room_list_email.text = userDTOs!![position].userEmail

            // 프로필 사진을 누르면 해당 채팅방 액티비티로 이동해야 합니다.
            viewHolder.direct_message_room_list_imageview_profile.setOnClickListener {

                selectedUid = userDTOs!![position].uid

                var intent = Intent(fragmentView?.context, DirectMessageActivity::class.java)

                // 채팅방의 주인인 사람(즉 현재 사용자)의 uid
                intent.putExtra("ownerUid", currentUserUid)
                // 나한테 dm을 보낸 사람의 uid
                intent.putExtra("guestUid", selectedUid)

                startActivity(intent)
            }
        }
    }
}