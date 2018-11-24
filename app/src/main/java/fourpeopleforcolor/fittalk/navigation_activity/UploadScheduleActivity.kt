package fourpeopleforcolor.fittalk.navigation_activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.ScheduleDTO
import kotlinx.android.synthetic.main.activity_upload_schedule.*

class UploadScheduleActivity : AppCompatActivity() {

    var storage : FirebaseStorage? = null

    var auth : FirebaseAuth? = null

    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_schedule)

        storage = FirebaseStorage.getInstance()

        auth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()

        upload_schedule_btn.setOnClickListener {
            scheduleUpload()
        }

    }

    /*
    11월 7일 팀장 박신우 개발 메모입니다.
     */
    // 운동 계획을 업로드하는 함수입니다.
    // 데이터 구조 변경 가능성에 따라 코드도 변경될 수 있습니다.

    /*
        11월 10일 팀장 박신우의 개발메모입니다. 운동 계획을 등록하지 않은 경우에 대한 예외처리를 해야합니다.
        또한 운동 계획을 업데이트할때 기존에 존재하던 것을 덮어쓰기하게끔 설정했습니다. (재등록이 곧 수정이 됩니다.)
    */

    /*
    * 11월 24일 팀원 김민지 입니다.
    * 해당 내용 점검 중에 코드가 중복되는 부분이 있는 것 같아서 함수 하나로 간략하게 바꿨습니다.
    * */
    fun scheduleUpload() {

        // 일주일이 7일이므로 그만큼 반복을 돕니다.
        var i = 0
        while (i < 7){
            when(i){
                0 -> {
                    Upload("월요일",upload_schedule_mon.text.toString())
                    i++
                }
                1 -> {
                    Upload("화요일",upload_schedule_tue.text.toString())
                    i++
                }
                2 -> {
                    Upload("수요일",upload_schedule_wed.text.toString())
                    i++
                }
                3 -> {
                    Upload("목요일",upload_schedule_thu.text.toString())
                    i++
                }
                4 -> {
                    Upload("금요일",upload_schedule_fri.text.toString())
                    i++
                }
                5 -> {
                    Upload("토요일",upload_schedule_sat.text.toString())
                    i++
                }
                6 -> {
                    Upload("일요일",upload_schedule_sun.text.toString())
                    i++
                }
            }
        }
        Toast.makeText(this, getString(R.string.upload_schedule_success), Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun Upload(dayOfWeek : String,contents : String){

        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = dayOfWeek
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = contents

        var title = scheduleDTO.userEmail + scheduleDTO.dayOfWeek


        if(scheduleDTO.schedule.isNullOrBlank()){
            //Toast.makeText(applicationContext, "월요일의 운동 계획을 입력해주세요!", Toast.LENGTH_LONG).show()
        } else{
            firestore?.collection("schedules")?.document(title)?.set(scheduleDTO)
        }
    }
}
