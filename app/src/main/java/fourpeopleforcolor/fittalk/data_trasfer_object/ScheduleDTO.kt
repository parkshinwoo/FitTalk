package fourpeopleforcolor.fittalk.data_trasfer_object

// 데이터베이스 구조 변경해야 할 수도 있습니다. 주단위로 묶는다던지.. 하는 식으로
data class ScheduleDTO(var uid: String? = null,
                    var userEmail: String? = null,
                    var dayOfWeek: String? = null,
                    var schedule: String? = null,
                    var timestamp: Long? = null
)