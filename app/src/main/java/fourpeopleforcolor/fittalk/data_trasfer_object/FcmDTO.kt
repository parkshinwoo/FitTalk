package fourpeopleforcolor.fittalk.data_trasfer_object

// 백그라운드 푸쉬 구현을 위한 data class
data class FcmDTO(
        var to : String? = null,
        var notification : Notification? = Notification()
){
    data class Notification(
            var body : String? = null,
            var title : String? = null
    )
}