package fourpeopleforcolor.fittalk.navigation_activity

// 2018년 9월 25일 팀장 박신우의 개발 메모입니다.
// 파이어베이스 스토리지(저장소)와 파이어베이스 스토어(데이터베이스)를 잘 구분하셔야 합니다.
// 스토리지에는 실제 사진 파일이 저장됩니다.
// 스토어에는 사진 파일의 uri, 즉 주소와 사진에 관련된 여러 정보들(사진을 올린 유저의 정보, 유저가 남긴 느낌평)이 묶여서 올라갑니다.

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.PhotoDTO
import kotlinx.android.synthetic.main.activity_upload_photo.*
import java.text.SimpleDateFormat
import java.util.*
import android.os.SystemClock
import android.view.View


class UploadPhotoActivity : AppCompatActivity() {

    // 안드로이드 디바이스의 앨범에서 선택된 사진이 맞는지를 확인하는 코드 값입니다.
    // 코드의 하단부 onActivityResult에서 사용됩니다.
    val PICK_IMAGE_FROM_ALBUM = 0

    // 파이어베이스 스토리지(저장소) 접근을 위한 값입니다.
    var storage : FirebaseStorage? = null

    // 사진의 주소를 담습니다.
    var photoUri : Uri? = null

    // 파이어베이스 사용자 인증을 위한 값입니다. 이 값을 통해 사용자의 uid, 이메일 주소 등에 접근합니다.
    var auth : FirebaseAuth? = null

    // 파이어베이스 스토어(데이터베이스) 접근을 위한 값입니다.
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)

        storage = FirebaseStorage.getInstance()

        auth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()

        // 안드로이드 디바이스 앨범을 엽니다.
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        // 사진 영역을 클릭시 안드로이드 디바이스 앨범을 여는 코드 입니다.
        upload_photo_image.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        // 공유 버튼을 클릭시 사진을 업로드 하는 함수를 호출합니다.
        upload_photo_btn.setOnClickListener (){
            photoUpload()
            upload_photo_btn.isClickable=false //버튼 더블클릭 방지
                                               //업로드 되는 시간 동안 두번이상 클릭하면 게시글이 두개이상 올라오는 것을 방지하였습니다 . by 팀원 김민지
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM){
            // 사진이 들어가는 영역에 사용자가 선택한 이미지를 띄워줍니다.
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                upload_photo_image.setImageURI(photoUri)
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                finish()
            }
        }
    }
    // 이미지를 파이어베이스 스토리지 및 스토어에 업로드하는 함수입니다.
    fun photoUpload() {

        // 사진이 업로드된 날짜 및 시각을 담는 변수입니다.
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // 사진의 파일 형식은 png로 하며 파일명은 사진이 업로드된 시각으로 합니다.
        val imageFileName = "PNG_"+timeStamp +"_.png"
        // 파이어베이스 스토리지에 images라는 디렉터리를 생성하고 그곳에 사진을 업로드합니다.
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 파이어베이스 스토리지에 이미지 올리기
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {taskSnapshot ->
            Toast.makeText(this, getString(R.string.upload_photo_success), Toast.LENGTH_LONG).show()

            // 업로드된 이미지 주소를 가져오기 파일 경로
            var uri = taskSnapshot.downloadUrl

            // DTO에 정의된 사진 관련 코틀린 데이터 클래스입니다.
            var photoDTO = PhotoDTO()

            // 이미지 주소
            photoDTO.imageUrl = uri!!.toString()

            // 유저의 UID(고유 해쉬 코드) 주민등록번호 같이 유저가 가지는 고유한 식별자입니다.
            photoDTO.uid = auth?.currentUser?.uid

            // 사진에 남긴 사용자의 느낌입니다.
            photoDTO.feeling = upload_photo_feeling.text.toString()

            // 유저의 이메일 계정입니다.
            photoDTO.userEmail = auth?.currentUser?.email

            // 사진이 업로드된 시각입니다. 시스템 시각을 기준으로 합니다.
            photoDTO.timestamp = System.currentTimeMillis()

            // 파이어베이스 스토어(데이터베이스)에 사진과 사진에 관련된 정보를 업로드합니다.
            // 실제 사진 파일은 스토리지(저장소)에 저장합니다.
            // 사진의 uri(주소)와 사진에 관련된 정보들(사용자 느낌, 사용자 uid, 이메일 계정 등)을 함께 묶어서 데이터베이스에 저장하는 겁니다.
            // images라는 디렉터리를 생성하고 그곳에 업로드합니다.
            firestore?.collection("images")?.document()?.set(photoDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }
    }
}