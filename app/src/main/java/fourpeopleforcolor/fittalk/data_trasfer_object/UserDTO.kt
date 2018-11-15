package fourpeopleforcolor.fittalk.data_trasfer_object

// 계정 생성(회원가입)을 할때 생성해서 파이어베이스 데이터베이스에 저장할
// 유저의 정보를 담고 있는 데이터

data class UserDTO(
        var uid : String? = null,
        var userEmail : String? = null,
        var timestamp : Long? = null
)