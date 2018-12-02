package fourpeopleforcolor.fittalk.navigation_activity

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.ScheduleDTO
import kotlinx.android.synthetic.main.activity_upload_schedule.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.BaseAdapter


class UploadScheduleActivity : Activity() {

    var storage: FirebaseStorage? = null

    var auth: FirebaseAuth? = null

    var firestore: FirebaseFirestore? = null

    var date: String? = null

    var listView: ListView? = null

    var text_food : EditText? = null
    var text_food_kcal : EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_upload_schedule)

        storage = FirebaseStorage.getInstance()

        auth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()

        date = intent.getStringExtra("date")

        var text_date = findViewById<TextView>(R.id.text_date)
        text_date.setText(date.toString())

        text_food = findViewById(R.id.text_food)
        text_food_kcal = findViewById(R.id.text_food_kcal)

        upload_btn.setOnClickListener {
            //scheduleUpload()
            Upload(text_date.text.toString(), schedule_todo.text.toString())
        }
        //search_food_btn.setOnClickListener {
            //        var editText = findViewById<EditText>(R.id.search_food_text)
            //         search(editText.text.toString())
        //}
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

/*
* 11월 26일 팀원 김민지 입니다.
* 팀장님이 작성하신 레이아웃을 전부 바꾸었습니다.
* 스케줄러의 기능이 매주 주간 스케줄을 띄우는게 아닌 날짜별로 정리해서 띄울 수 있도록
* 세분화 작업을 했습니다.
*
* 코드가 미흡한 부분은 추후에 한번 더 점검하도록 하겠습니다.
*
* 전에 개발했던 식품 칼로리 검색 API를 추가만 해놓았습니다.
* 연동은 추후에 하도록 하겠습니다.
* */

    fun Upload(date: String, contents: String) {

        var scheduleDTO = ScheduleDTO()

        scheduleDTO.uid = auth?.currentUser?.uid
        scheduleDTO.userEmail = auth?.currentUser?.email
        scheduleDTO.date = date
        scheduleDTO.timestamp = System.currentTimeMillis()
        scheduleDTO.schedule = contents
        scheduleDTO.food = text_food?.text.toString()
        scheduleDTO.food_kcal = text_food_kcal?.text.toString()

        var title = scheduleDTO.userEmail + scheduleDTO.date


        if (scheduleDTO.schedule.isNullOrBlank()) {
            //Toast.makeText(applicationContext, "월요일의 운동 계획을 입력해주세요!", Toast.LENGTH_LONG).show()
        } else {
            firestore?.collection("schedules")?.document(title)?.set(scheduleDTO)
        }

        Toast.makeText(this, getString(R.string.upload_schedule_success), Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /*
    fun search(food: String) {
        class FoodSearchAPI() : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                start(params[0].toString())
            }

            var foodArray = ArrayList<Food>()

            inner class Food(name: String, var cal: String) {
                var name = ""

                init {
                    this.name = name
                }
            }

            fun start(food: String) {
                Log.d("food", "loading URL")
                val URL = "https://www.foodsafetykorea.go.kr/portal/healthyfoodlife/foodnutrient/simpleSearch.do?menu_no=2805&menu_grp=MENU_NEW03&code4=2&code2=&search_name=$food"
                var doc = Jsoup.connect(URL).get()
                Log.d("", food)
                Log.d("", doc.toString())
                val maxPage = getMaxPage(doc)
                Log.d("food", "find max page")
                Log.d("food", maxPage.toString())

                Log.d("food", "find list")
                for (i in 51..75) {
                    doc = Jsoup.connect("$URL&page=$i").get()
                    Parsing(doc)
                }
            }

            fun getMaxPage(doc: Document): Int {
                var data = doc.getElementsByClass("total").get(1).text()
                data = data.substring(data.indexOf("Items, "))
                data = data.replace("[^0-9]".toRegex(), "")
                data = data.substring(1, data.length)

                return Integer.parseInt(data)
            }

            fun Parsing(doc: Document) {
                Log.d("food", "Parsing")
                var cnt = 0
                val e_name = doc.select("th").select("[href]")
                val e_kcal = doc.getElementsByTag("td")
                var j = 0
                var i = 0
                while (i < e_kcal.size && cnt < 5) {
                    if (i % 10 == i / 10 + 1) {
                        foodArray.add(Food(e_name.get(j++).text(), e_kcal.get(i + 1).text()))
                        cnt++
                    }
                    i++
                }
                Log.d("food", "GOGOGO")
                print(foodArray)
            }

            fun print(food: ArrayList<Food>) {
                var foodDTO = FoodDTO()
                for (e in food) {
                    foodDTO.name = e.name
                    foodDTO.cal = e.cal
                    var title = e.name
                    //println(e.name + " " + e.cal)
                    firestore?.collection("foods")?.document(title)?.set(foodDTO)
                }
            }
        }
        Log.d("", food)
        FoodSearchAPI().execute(food)
    }

    }*/
}

