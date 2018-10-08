package fourpeopleforcolor.fittalk

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

// 2018년 9월 14일 팀장 박신우의 개발 메모입니다.
// 코드 공유 및 프로젝트 버전 관리는 github와 sourcetree를 사용하기로 결정했습니다.
// 구글 계정 로그인 기능을 위해서
// AndroidManifest.xml 파일에 인터넷 사용 권한 허가 코드를 넣어야합니다. <uses-permission android:name="android.permission.INTERNET" />

// 페이스북 로그인 기능은 현재 프로젝트가 "개발 모드" 이기때문에 개발자인 저의 계정으로만 로그인이 가능합니다.
// 다른 유저의 로그인도 가능하게 하려면 개인정보처리방침을 작성하고 그 url을 페이스북 개발자 홈페이지에 등록을 해야합니다.
// 그리고 앱의 사용목적을 명시한 후 페이스북 측에 앱 "공개" 허가를 신청을 해서 검토를 받은 후 승인이 나야만 합니다.
// 이 부분은 프로젝트의 완성도를 끌어올리는 단계에서 수행하기로 결정했습니다. (현재 단계에서는 제외)
// 또한 카카오톡, 네이버 API를 활용해서 로그인 기능을 추가하려 했는데 구글 검색 결과 파이어베이스와는 충돌을 일으킨다고 합니다.
// 파이어베이스 기능중에 전화번호로 인증해서 계정을 생성하는 기능도 있는데 이 부분도 나중에 추가 구현하기로 하겠습니다.


class LoginActivity : AppCompatActivity() {

    // 파이어베이스 계정 인증을 위한 변수
    var auth : FirebaseAuth? = null

    // 구글 로그인 계정을 위한 변수
    var googleSignInClient : GoogleSignInClient? = null

    var GOOGLE_LOGIN_CODE = 9001

    // onCreate는 앱의 화면이 처음으로 생성됬을때 호출 되는 부분입니다. 즉 앱의 특정 화면이 처음 켜질때 호출이 됩니다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // 이메일 계정 생성 및 로그인 버튼에 이벤트를 다는겁니다. (activity_login.xml 디자인을 참고하세요)
        email_login_button.setOnClickListener {
            createAndLoginEmail()
        }

        // 구글 계정으로 로그인 버튼에 이벤트를 다는겁니다.
        google_sign_in_button.setOnClickListener {
            googleLogin()
        }

        // 구글 로그인 옵션 설정
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

    }

    // 이메일 계정 생성
    fun createAndLoginEmail(){
        if(email_edittext.text.isNullOrBlank()||password_edittext.text.isNullOrBlank()){
            Toast.makeText(applicationContext,"이메일과 비밀번호를 입력 해 주세요.", Toast.LENGTH_LONG).show() //이메일 입력란, 비밀번호 입력란 둘 중 하나라도 비어있으면 진행이 되지 않고 Toast메세지를 띄우는 역할을 합니다.
        }else{
            auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    // 계정 생성이 성공하면 메인 액티비티로 이동합니다.
                    // currentUser라는건 현재 회원가입 및 로그인을 시도하는 유저입니다. 즉 사용자이죠
                    moveMainPage(auth?.currentUser)
                }else if(task.exception?.message.isNullOrEmpty()){
                    // 예외가 발생하면 메세지를 찍어주는 기능입니다.
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
                    //회원가입 성공도 아니고 실패도 아니면 기존에 이미 있는 계정으로 로그인을 시키면 되겠죠
                    signinEmail()
                }
            }
        }

    }

    // 이메일 계정으로 로그인
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }else{
                Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // 메인 액티비티로 이동합니다.
    fun moveMainPage(user :FirebaseUser?){
        if(user != null){
            // 액티비티를 옮겨다닐때는 startActivity를 사용합니다.
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    // 구글 로그인
    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    // 파이어베이스 구글 계정 인증을 처리하는 부분입니다.
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount){
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {

            task ->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }
        }
    }

    // onResume는 앱의 화면이 잠시 백그라운드로 가있다가 다시 켜지면 실행되는 곳입니다.
    // 앱을 사용하다가 홈버튼을 눌러서 밖으로 나갔다가 다시 들어올때 실행되는 곳이에요
    override fun onResume() {
        super.onResume()
        moveMainPage(auth?.currentUser) // 자동 로그인
    }

    // Activity에서 일어난 일의 결과를 처리하는 곳이라고 생각하면 됩니다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
        }
    }
}
