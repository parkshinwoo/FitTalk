package fourpeopleforcolor.fittalk.data_trasfer_object

data class DirectMessageDTO(var directMessageRoom: MutableMap<String, String> = HashMap() // 메세지를 보내는 사람의 uid, 메세지를 받는 사람의 uid
){
    data class Message(var directMessage: MutableMap<String, String> = HashMap(), // 키는 메세지를 보낸 사람의 uid, 값은 메세지의 내용
                       var timestamp: Long? = null
                      )
}