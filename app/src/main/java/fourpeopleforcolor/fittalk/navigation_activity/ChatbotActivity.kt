package fourpeopleforcolor.fittalk.navigation_activity

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.model.AIRequest
import ai.api.model.Result
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import com.google.gson.Gson
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.WeatherDTO
import fourpeopleforcolor.fittalk.util.ChatbotRecyclerViewAdapter
import fourpeopleforcolor.fittalk.util.MessageDTO
import kotlinx.android.synthetic.main.activity_chatbot.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat

class ChatbotActivity : AppCompatActivity() {

    var messageDTOs = arrayListOf<MessageDTO>()

    // 다이얼로그 플로우와 통신하기 위한 클래스입니다.
    var aiDataService : AIDataService? = null

    // 챗봇(다이얼로그 플로우)과 날짜를 주고 받으려면 서로 이해할 수 있는 형식이어야 합니다.

    // 사람(HumanText)이 이해 할 수 있는 날짜를 컴퓨터가 이해할 수 있는 날짜 형식으로 맞추기 위한 값입니다.
    var dateFormatFromHumanText = SimpleDateFormat("yyyy-MM-dd")

    // 날씨 API(OpenWeatherMap API)에게 넘겨주기 위한 날짜 형식
    var weatherDataFormatFromHumanText = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

    // 날씨 API에게 넘겨받은 날짜를 사람이 이해하기 쉬운 형식으로 바꾸기
    var weatherDataFormatToHumanText = SimpleDateFormat("MM월 dd일 hh시")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        recyclerview.adapter = ChatbotRecyclerViewAdapter(messageDTOs)
        recyclerview.layoutManager = LinearLayoutManager(this)

        // 다이얼로그 플로우 v1 API "개발자" 키 값을 넣어줍니다.
        // v2 API는 안드로이드를 아직 지원 안합니다.
        // 이 앱을 배포할 시엔 "클라이언트" 키 값을 넣으면 됩니다.
        var config = AIConfiguration("260b917b01d24614ad23e0bdf398a7ab", AIConfiguration.SupportedLanguages.Korean)
        // 설정한 값으로 aiDataService 객체를 생성합니다.
        aiDataService = AIDataService(config)

        chat.setOnClickListener {
            if(!TextUtils.isEmpty(chatText.text)){
                // 챗봇에게 보낼 말을 입력하는 창이 빈 문자열이 아닐 경우에 메세지를 보낼 수 있게 해줍니다.
                messageDTOs.add(MessageDTO(true, chatText.text.toString()))

                // 내가 전송한 메세지가 recyclerview 화면에 뿌려지게끔 새로고침 합니다.
                recyclerview.adapter.notifyDataSetChanged()

                // 끝 위치로 이동합니다.
                recyclerview.smoothScrollToPosition(messageDTOs.size-1)

                // 챗봇(다이얼로그 플로우)와 통신하는 쓰레드를 실행합니다.
                TalkAsyncTask().execute(chatText.text.toString())
                // 메세지 입력창을 빈문자열로 초기화 해줍니다.
                chatText.setText("")
            }
        }

    }
    // 다이얼로그 플로우와 통신하는 쓰레드를 만들어줍니다.
    inner class TalkAsyncTask : AsyncTask<String, Void, Result>(){

        // 백그라운드에서 수행될 일을 작성합니다.
        override fun doInBackground(vararg params: String?): Result {

            // 사용자가 전송한 메세지를 다이얼로그 플로우로 넘겨주는 쿼리입니다.
            var aiRequest = AIRequest()
            aiRequest.setQuery(params[0])

            // 결과를 리턴합니다.
            return aiDataService?.request(aiRequest)!!.result
        }

        // doInBackground 이후에 수행되는 곳입니다.
        override fun onPostExecute(result: Result?) {
            // 리턴받은 결과가 null이 아니라면 메세지로 만들어줍니다.
            if(result != null){
                makeMessage(result)
            }
        }

    }

    // 챗봇의 답장을 받아와서 메세지로 만들고 채팅창에 띄웁니다.
    fun makeMessage(result: Result?){

        // 다이얼로그 플로우 콘솔에 직접 생성한 Intent가 있을때
        // 종류에 따라 필터링을 하는 겁니다.
        when(result?.metadata?.intentName){

            // 메타데이터의 인텐트 이름이 "Weather"일때
            "Weather" -> {
                //Weather라는 인텐트를 다이얼로그 플로우에 생성했습니다.
                //Training phrases(챗봇 훈련어구)에 서울, 용인 등 지역을 등록을 해놨습니다.
                var city = result.parameters["geo-city"]
                if (city == null){
                    // 사용자가 도시를 언급안했을시 기본값으로 서울의 날씨를 알려줍니다.
                    weatherMessage("서울특별시")
                } else{
                    weatherMessage(city.asString)
                }
            }
            else -> {
                // 어느 유형에도 속하지 않을 시 다이얼로그 플로우에 기본 어구로 등록된 말이 출력됩니다.
                var speech = result?.fulfillment?.speech
                messageDTOs.add(MessageDTO(false, speech))

                // recyclerview를 새로고침해줍니다.
                recyclerview.adapter.notifyDataSetChanged()
                recyclerview.smoothScrollToPosition(messageDTOs.size-1)
            }

        }
    }

    // 날씨 관련 메세지를 만듭니다.
    fun weatherMessage(city:String){

        // city 파라메터로 넘어온걸 url에 붙입니다.
        // 맨끝의 Units-metric은 날씨를 섭씨로 받아오는 옵션입니다.
        // 발급 받은 api 키는 86299d89d3158e76da1eeb77522844b0
        var weatherUrl = "https://api.openweathermap.org/data/2.5/forecast?id=524901&APPID=86299d89d3158e76da1eeb77522844b0&q="+city+"&units=metric"
        // openWeatherMap API에 해당 url을 넘겨주고 날씨 정보를 요청합니다.
        var request = Request.Builder().url(weatherUrl).build()

        // request를 OkHttp로 호출합니다
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                // 요청이 실패했을 시 실행됩니다.

            }

            override fun onResponse(call: Call?, response: Response?) {
                // 결과로 넘어온 json 값을 담습니다.
                var result = response?.body()?.string()

                // json 값을 weatherDTO 오브젝트로 만듭니다.
                var weatherDTO = Gson().fromJson(result, WeatherDTO::class.java)
                for(item in weatherDTO.list!!){
                    // 데이터 수신 시간을 컴퓨터가 이해할 수 있는 형태로 바꿉니다.
                    var weatherItemUnixTime = weatherDataFormatFromHumanText.parse(item.dt_txt).time
                    if(weatherItemUnixTime > System.currentTimeMillis()){
                        // json을 파싱해서 가져온 시간이 현재 시간보다 미래일 경우에 가져다 씁니다.

                        // 온도
                        var temp = item.main?.temp
                        // 날씨 상태
                        var description = item.weather!![0].description
                        // 시간
                        var time = weatherDataFormatToHumanText.format(weatherItemUnixTime)
                        // 습도
                        var humidity = item.main?.humidity
                        // 풍속
                        var speed = item.wind?.speed
                        // 3시간 동안 침적되는 비의 양
                        var three_hour = item.rain?.three_hour

                        var message = time + " 기준 " + city + "의 기온은 " + temp + "도 입니다! " + "\n" + "습도는 " + humidity + "% 구요! " + "풍속은 " + speed + "meter/sec " + "입니다! " + "\n" + " 날씨 핑계대지 말고 나가서 운동하세요!!"

                        runOnUiThread {
                            messageDTOs.add(MessageDTO(false, message))
                            // recyclerview를 새로고침해줍니다.
                            recyclerview.adapter.notifyDataSetChanged()
                            recyclerview.smoothScrollToPosition(messageDTOs.size - 1)
                        }
                        break // 현재 시간에서 가장 가까운 미래의 날씨만 가져오면 됩니다. 계속 앞의 미래를 가져올 필요는 없으니 break 해줍니다.

                    }
                }
            }

        })
    }
}
