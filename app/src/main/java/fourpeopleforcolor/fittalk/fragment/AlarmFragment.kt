package fourpeopleforcolor.fittalk.fragment


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
import fourpeopleforcolor.fittalk.data_trasfer_object.AlarmDTO
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.recyclerview_item_design_alarm.view.*

class AlarmFragment : Fragment() {

    var fragmentView : View? = null

    var alarmSnapshot  : ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_alarm, container, false)

        return fragmentView
    }

    override fun onResume() {
        super.onResume()
        fragmentView?.alarmfragment_recyclerview?.adapter = AlarmFragmentRecyclerviewAdapter()
        fragmentView?.alarmfragment_recyclerview?.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStop() {
        super.onStop()
        alarmSnapshot?.remove()
    }

    inner class AlarmFragmentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var alarmDTOList = ArrayList<AlarmDTO>()

        init {
            var uid = FirebaseAuth.getInstance().currentUser!!.uid

            alarmSnapshot = FirebaseFirestore.getInstance().collection("alarms").orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if(querySnapshot == null) return@addSnapshotListener

                // 돌때마다 한번씩 지워줘야합니다.
                // 푸쉬 드리븐 방식은 계속 데이터가 들어올때마다 누적되어서 쌓이기 때문입니다.
                // 그래서 긁어올때 clear를 먼저 해줘야합니다.
                alarmDTOList.clear()

                for(snapshot in querySnapshot.documents){
                    if(snapshot.data["destinationUid"] == uid){
                        alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java))
                    }
                }
                // 새로고침 (푸쉬 드리븐 방식이라서)
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context).inflate(R.layout.recyclerview_item_design_alarm, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var viewHolder = (holder as CustomViewHolder).itemView

            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get()?.addOnCompleteListener {task ->
                if(task.isSuccessful){
                    var url = task.result["image"]

                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewHolder.alarmviewItem_imageview_profile)
                }
            }

            // kind를 번호별로 구분을 해놨습니다. when 문을 써서 필터링하고 해당하는 알람 메세지를 구성해줘야합니다.
            when (alarmDTOList[position].kind){

                0 -> {
                    var str_0 = alarmDTOList[position].userEmail + getString(R.string.alarm_favorite)
                    viewHolder.alarmviewItem_textview.text = str_0
                }

                1 -> {
                    var str_1 = alarmDTOList[position].userEmail +
                            getString(R.string.alarm_who) +" \"" +
                            alarmDTOList[position].message +"\" " +
                            getString(R.string.alarm_comment)
                    viewHolder.alarmviewItem_textview.text = str_1
                }

                2 -> {
                    var str_2 = alarmDTOList[position].userEmail + getString(R.string.alarm_follow)
                    viewHolder.alarmviewItem_textview.text = str_2
                }
            }
        }

    }

}
