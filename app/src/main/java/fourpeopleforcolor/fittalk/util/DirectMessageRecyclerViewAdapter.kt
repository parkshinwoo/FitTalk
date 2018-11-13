package fourpeopleforcolor.fittalk.util

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.DirectMessageDTO
import kotlinx.android.synthetic.main.recyclerview_item_design_direct_message.view.*


class DirectMessageRecyclerViewAdapter(messageDTOs : ArrayList<DirectMessageDTO.Message>, currentUserUid:String, selectedUid:String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var currentUserUid = currentUserUid
    var selectedUid = selectedUid

    var messageDTOs  = messageDTOs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_design_direct_message, parent, false)

        return CustomViewHolder(view)
    }

    private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int {
        return messageDTOs!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(messageDTOs!![position].directMessage.containsKey(currentUserUid)){
            // 내가 보낸 메세지
            holder.itemView.right_DMbubble.visibility = View.VISIBLE
            holder.itemView.right_DMbubble.text = messageDTOs!![position].directMessage[currentUserUid].toString()
            holder.itemView.left_DMbubble.visibility = View.INVISIBLE
        } else{
            // 상대방이 나한테 보낸 메세지
            holder.itemView.left_DMbubble.visibility = View.VISIBLE
            holder.itemView.left_DMbubble.text = messageDTOs!![position].directMessage[selectedUid].toString()
            holder.itemView.right_DMbubble.visibility = View.INVISIBLE
        }
    }
}

