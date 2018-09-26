package fourpeopleforcolor.fittalk.data_trasfer_object

data class FollowDTO(

        // 사용자를 팔로잉 하는 유저의 수
        var followerCount : Int = 0,
        // 사용자를 팔로잉 하는 유저의 uid를 string으로 그리고 팔로잉 여부를 boolean으로 가집니다.
        var followers : MutableMap<String, Boolean> = HashMap(),

        // 사용자가 팔로우 하는 유저의 수
        var followingCount : Int = 0,
        // 사용자가 팔로우 하는 유저의 uid, 팔로우 여부를 가집니다.
        var followings : MutableMap<String, Boolean> = HashMap()

)