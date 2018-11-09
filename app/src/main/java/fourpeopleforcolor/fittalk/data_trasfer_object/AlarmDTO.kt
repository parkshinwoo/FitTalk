package fourpeopleforcolor.fittalk.data_trasfer_object

data class AlarmDTO(
        var destinationUid : String? = null,
        var userEmail : String? = null,
        var uid : String? = null,
        var kind : Int? = 0, // 0: 좋아요, 1: 댓글, 2: 팔로우(다른 사람이 나를 팔로우 했을때 발생하는 경우, 즉 팔로워 발생에 대한 알람)
        var message : String? = null,
        var timestamp : Long? = null
)