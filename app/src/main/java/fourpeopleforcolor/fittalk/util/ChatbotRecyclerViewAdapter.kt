package fourpeopleforcolor.fittalk.util


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fourpeopleforcolor.fittalk.R
import kotlinx.android.synthetic.main.recyclerview_item_design_chatbot.view.*

// 다른 fragment들 처럼 activity의 하위화면으로 붙는것이 아니라
// recyclerview만 잡으면 되기에 fragment로 만들지 않았습니다.
// ChatbotActivity에서 ChatbotRecyclerViewAdapter를 잡습니다.


// 메세지를 담는 데이터 클래스입니다.
// 내가 보낸 메세지인지 챗봇이 내게 보낸 메세지인지를 체크하는 변수 isMyMesage
// 메세지 내용을 담는 message
data class MessageDTO(
        var isMyMessage:Boolean? = null,
        var message:String? = null
        )

class ChatbotRecyclerViewAdapter(messageDTOs:ArrayList<MessageDTO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var messageDTOs = messageDTOs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_design_chatbot, parent, false)

        return CustomViewHolder(view)

    }

    private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int {
        return messageDTOs.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (messageDTOs[position].isMyMessage!!) {
            // 내가 챗봇에겐 보낸 메세지일 경우에는 나의 말풍선만 보이게 하고
            // 챗봇의 말풍선은 가려야합니다.
            holder.itemView.right_chatbubble.visibility = View.VISIBLE
            holder.itemView.right_chatbubble.text = messageDTOs[position].message
            holder.itemView.left_chatbubble.visibility = View.INVISIBLE
        } else {
            // 챗봇이 내게 보낸 메세지일 경우
            holder.itemView.left_chatbubble.visibility = View.VISIBLE
            holder.itemView.left_chatbubble.text = messageDTOs[position].message
            holder.itemView.right_chatbubble.visibility = View.INVISIBLE
        }

    }

}