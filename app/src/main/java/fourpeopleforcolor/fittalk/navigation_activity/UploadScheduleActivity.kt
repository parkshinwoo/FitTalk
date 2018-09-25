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

    // 운동 계획을 업로드하는 함수입니다.
    fun scheduleUpload() {

        // 일주일이 7일이므로 그만큼 반복을 돕니다.
        var i = 0
        while (i < 7){
            when(i){
                0 -> {
                    // 월요일 계획을 업로드 하는 함수를 호출합니다.
                    monUpload()
                    i++
                }
                1 -> {
                    tueUpload()
                    i++
                }
                2 -> {
                    wedUpload()
                    i++
                }
                3 -> {
                    thrUpload()
                    i++
                }
                4 -> {
                    friUpload()
                    i++
                }
                5 -> {
                    satUpload()
                    i++
                }
                6 -> {
                    sunUpload()
                    i++
                }
            }
        }
        Toast.makeText(this, getString(R.string.upload_schedule_success), Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    // 월요일 계획을 업로드 하는 함수입니다.
    fun monUpload(){
        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = "월요일"
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = upload_schedule_mon.text.toString()

        firestore?.collection("schedules")?.document()?.set(scheduleDTO)
    }

    fun tueUpload(){
        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = "화요일"
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = upload_schedule_tue.text.toString()

        firestore?.collection("schedules")?.document()?.set(scheduleDTO)

    }

    fun wedUpload(){
        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = "수요일"
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = upload_schedule_wed.text.toString()

        firestore?.collection("schedules")?.document()?.set(scheduleDTO)
    }

    fun thrUpload(){
        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = "목요일"
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = upload_schedule_thr.text.toString()

        firestore?.collection("schedules")?.document()?.set(scheduleDTO)
    }

    fun friUpload(){
        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = "금요일"
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = upload_schedule_fri.text.toString()

        firestore?.collection("schedules")?.document()?.set(scheduleDTO)
    }

    fun satUpload(){
        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = "토요일"
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = upload_schedule_sat.text.toString()

        firestore?.collection("schedules")?.document()?.set(scheduleDTO)
    }

    fun sunUpload(){
        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.dayOfWeek = "일요일"
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = upload_schedule_sun.text.toString()

        firestore?.collection("schedules")?.document()?.set(scheduleDTO)
    }
}
