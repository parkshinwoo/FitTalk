package fourpeopleforcolor.fittalk.data_trasfer_object

data class PhotoDTO(var uid: String? = null,
                    var userEmail: String? = null,
                    var feeling: String? = null,
                    var imageUrl: String? = null,
                    var timestamp: Long? = null,
                    var underArmourCount: Int = 0,
                    var underArmours: MutableMap<String, Boolean> = HashMap()
){
    data class Comment(var uid: String? = null,
                       var userEmail: String? = null,
                       var comment: String? = null,
                       var timestamp: Long? = null)
}