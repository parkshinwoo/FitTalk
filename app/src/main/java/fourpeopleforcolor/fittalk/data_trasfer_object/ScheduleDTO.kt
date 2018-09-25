package fourpeopleforcolor.fittalk.data_trasfer_object

data class ScheduleDTO(var uid: String? = null,
                    var userEmail: String? = null,
                    var dayOfWeek: String? = null,
                    var schedule: String? = null,
                    var timestamp: Long? = null
)