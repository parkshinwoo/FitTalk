package fourpeopleforcolor.fittalk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import fourpeopleforcolor.fittalk.fragment.AlarmFragment
import fourpeopleforcolor.fittalk.fragment.HomeFragment
import fourpeopleforcolor.fittalk.fragment.SearchFragment
import fourpeopleforcolor.fittalk.fragment.UserProfileFragment
import fourpeopleforcolor.fittalk.navigation_activity.ChatbotActivity
import fourpeopleforcolor.fittalk.navigation_activity.UploadPhotoActivity
import fourpeopleforcolor.fittalk.navigation_activity.UploadScheduleActivity
import kotlinx.android.synthetic.main.activity_main.*

// 하단 네비게이션 바에 있는 아이콘을 클릭하면 해당 액티비티, 프레그먼트로 이동합니다.

// UploadPhotoActivity 같은 경우는 디바이스의 사진에 접근해야 하므로
// 스토리지 접근 권한 허가 코드를 AndroidManifest.xml에 반드시 추가하고 <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
// UploadPhotoActivity로 이동하기 전에 권한 허가에 대한 부분을 체크해야합니다.

// 2018년 9월 14일 개발자 메모입니다. by 팀장 박신우
// 전체적인 틀을 짰습니다. 액티비티, 프레그먼트 그리고 각각에 해당하는 디자인 레이아웃을 만들기만했어요(xml파일)
// xml파일로 디자인은 따로 해야합니다.
// 액티비티는 앱의 활동이 일어나는 화면이고 프레그먼트는 액티비티의 하위 화면입니다.
// 액티비티에 여러 프레그먼트를 붙일 수 있습니다.
// 예를 들자면 유저의 계정 화면에서 아래쪽에 그 유저의 사진들이 떠야하죠,
// 사진 데이터를 긁어와서 시간 순서대로 띄워줘야하는데 이때
// 사진이 반복적으로 그려지는 뷰를 RecyclerView라고 하구요
// RecyclerView를 잡는 RecyclerViewAdaptor가 있습니다.
// 그 RecyclerViewAdaptor가 프레그먼트에 붙고 이 프레그먼트가 액티비티에 붙는 방식입니다.
// 그리고 recyclerview_item_design_* 파일들은 각 데이터들이 RecyclerView에 뿌려지는 방식을 디자인하는 파일입니다.

// 파이어베이스 클라우드 메세징 기능을 이용해서 백그라운드 푸쉬 알람을 구현하려합니다.
// data_trasfer_obect 패키지를 보시면 FcmDTO가 있고, util 패키지를 보시면 FcmPush가 있습니다.
// 참고로 Fcm은 파이어베이스 클라우드 메세징의 줄인말이고 DTO는 Data Transfer Object입니다.

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        setToolbarDefault()

        // 하단 네비게이션 바에 여러 아이템들이 있습니다.
        // home, search, 사진 올리기, 계획 올리기, 유저, 챗봇이 있죠
        // 사용자가 네비게이션 바의 특정 아이템을 선택하면 해당하는 화면으로 이동시켜주는 기능입니다.
        when (item.itemId) {
            R.id.action_home -> {


                val homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, homeFragment).commit()

                return true
            }
            R.id.action_search -> {

                // 액티비티로 넘어가는것과 프레그먼트로 넘어가는것은 구현이 다릅니다.
                // 액티비티는 startActivity를 사용했었습니다.
                val searchFragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, searchFragment).commit()

                return true

            }
            R.id.action_upload_photo -> {

                // 스토리지 접근 권한 체크를 합니다.
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    startActivity(Intent(this, UploadPhotoActivity::class.java))
                } else {
                    Toast.makeText(this, "EXTERNAL STORAGE 읽기 권한이 없습니다.", Toast.LENGTH_LONG).show()
                }


                return true
            }
            R.id.action_upload_schedule -> {

                startActivity(Intent(this, UploadScheduleActivity::class.java))

                return true
            }
            R.id.action_alarm -> {

                val alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()

                return true
            }

            R.id.action_account -> {

                val userProfileFragment = UserProfileFragment()

                // 아무한테나 가면 안되고 내 계정 화면으로 가야하므로
                // 파이어베이스 계정 정보를 가져와야합니다.
                // uid는 유저가 가지는 고유한 정보입니다. 주민등록번호를 생각하시면 됩니다.
                val uid = FirebaseAuth.getInstance().currentUser!!.uid

                // 번들에 프레그먼트가 가져야할 목적지 역할을 하는 유저의 uid를 담는겁니다.
                val bundle = Bundle()

                bundle.putString("destinationUid", uid)
                userProfileFragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(R.id.main_content, userProfileFragment).commit()

                return true
            }

            R.id.action_chatbot -> {

                startActivity(Intent(this, ChatbotActivity::class.java))

                return true
            }


        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation_top.setOnNavigationItemSelectedListener(this)
        bottom_navigation_down.setOnNavigationItemSelectedListener(this)

        // 시작할때마다 홈에서 시작하게끔 하단 네비게이션의 현재 선택된 아이템을 "home"으로 지정합니다.
        bottom_navigation_top.selectedItemId = R.id.action_home

        // 디바이스 사진첩에 접근할 권한 주기
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1 )

    }

    // 맨 상단에 위치하는게 툴바입니다.
    // 툴바의 기본 디자인을 지정하는 코드입니다.
    fun setToolbarDefault(){
        toolbar_btn_back.visibility = View.GONE
        toolbar_username.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

}