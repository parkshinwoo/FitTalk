package fourpeopleforcolor.fittalk.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import fourpeopleforcolor.fittalk.R
import fourpeopleforcolor.fittalk.data_trasfer_object.PhotoDTO
import kotlinx.android.synthetic.main.fragment_search.view.*
import android.content.Intent
import fourpeopleforcolor.fittalk.navigation_activity.CommentActivity
import org.w3c.dom.Comment
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.content.ContextCompat.startActivity






class SearchFragment : Fragment() {

    var fragmentView : View? = null

    // 2018년 9월 26일 팀장 박신우의 개발 메모입니다.
    // snapshot은 항상 데이터베이스를 지켜보다가 변경사항이 생기면 뷰한테 던져주는 역할을 합니다.
    // 현재 사용하는 파이어베이스 데이터베이스가 push driven 방식이므로 스냅샷을 쓰면
    // 실시간으로 변하는 데이터를 바로바로 화면에 반영할 수 있습니다.
    // 만약 뷰가 백그라운드로 가있는데 스냅샷이 데이터를 던져준다면 에러가 발생할 것입니다.
    // 그러므로 Registration에 스냅샷을 등록해놓고 뷰가 백그라운드에 가 있거나
    // 뷰가 꺼질때는 스냅샷도 함께 꺼줘야지만 안정적으로 앱이 동작합니다.
    var searchListenerRegistration : ListenerRegistration? = null

    // 뷰가 처음 생성될때 실행되는 함수입니다.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.fragment_search, container, false)
        return fragmentView
    }

    // 뷰가 백그라운드로 가 있다가 다시 실행될때 실행되는 함수입니다.
    override fun onResume() {
        super.onResume()
        // SearchFragment의 main view에 아랫쪽에서 커스텀하고 디자인한 이미지 뷰를 붙여줍니다.
        fragmentView?.search_fragment_recyclerview?.adapter = SearchFragmentRecyclerviewAdapter()
        // spanCount의 값을 3으로 줘야 한줄에 사진이 3장씩 그려집니다.
        fragmentView?.search_fragment_recyclerview?.layoutManager = GridLayoutManager(activity, 3)
    }

    // 뷰가 꺼질때 실행된느 함수입니다. 이곳에서 스냅샷도 꺼야합니다.
    override fun onStop() {
        super.onStop()
        // 스냅샷을 꺼버립니다.
        searchListenerRegistration?.remove()
    }

    // search 화면은 데이터베이스에서 갱신되는 데이터를 계속 긁어와서 뿌려줘야합니다.
    // 즉 recyclerview가 들어갑니다. 그 recyclerview를 제어할 adapter를 정의합니다.
    inner class SearchFragmentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        // 데이터베이스로 부터 넘어오는 데이터를 받을 변수입니다.
        var photoDTOs : ArrayList<PhotoDTO>

        init {
            photoDTOs = ArrayList()

            // 사진을 가져오는데 최신순으로 정렬해서 가져옵니다.
            searchListenerRegistration = FirebaseFirestore.getInstance().collection("images").orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                // 스냅샷이 null로 넘어오면 종료합니다.
                if(querySnapshot == null) return@addSnapshotListener

                // 먼저 그릇을 비워줍니다.
                // 스냅샷은 항상 데이터베이스를 지켜보면서 갱신되는걸 긁어옵니다.
                // 수행될때마다 그릇을 비워주지 않으면 중복된 사진이 쌓여서 뿌려지겠죠?
                photoDTOs.clear()

                // documents는 "images" 디렉터리의 하위에 있는 각 이미지 하나 하나에 관련된 묶음입니다.
                // 즉 "images"라는 디렉터리 밑에 사진들이 있고
                // 그 하나하나의 사진이 documents인 셈이죠
                for(snapshot in querySnapshot!!.documents){
                    // 긁어온 데이터를 PhotoDTO 타입으로 캐스팅 해주고 그릇에 담습니다.
                    photoDTOs.add(snapshot.toObject(PhotoDTO::class.java))
                }
                // 데이터들이 뿌려지는 recyclerview도 새로고침해줘야 갱신 사항이 반영됩니다.
                notifyDataSetChanged()
            }
        }

        // 뷰홀더를 생성합니다.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            // 전체 화면의 폭의 3분의 1입니다.
            var width = resources.displayMetrics.widthPixels / 3
            // 전체 화면 폭의 3분의 1입니다. 정사각형 형태로 잡기 위해 widthPixels로 통일합니다.
            var height = resources.displayMetrics.widthPixels / 3

            // 사진이 뿌려질 이미지 뷰 입니다.
            var imageView = ImageView(parent?.context)
            // 레이아웃의 인자로 위에서 정의한 크기를 넘겨줍니다. 가로 세로 크기가 화면 폭의 3분의 1이 되는 정사각형이 됩니다.
            // 뿌려지는 사진의 크기를 정의한겁니다.
            imageView.layoutParams = ViewGroup.LayoutParams(width, height)

            return CustomViewHolder(imageView)
        }
        // Recyclerview의 뷰홀더를 상속해서 CustomViewHolder를 만든겁니다.
        // 사진이 뿌려질 이미지 뷰의 디자인을 커스텀했기 때문에 만든겁니다.
        // 보통의 recyclerview라면 수직으로 단순하게 뿌려지겠지만
        // 저희는 화면 폭의 3분의1만큼의 크기로 사진들을 뿌릴거기 때문에 custom 한것입니다.
        private inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        // 화면에는 데이터를 담은 그릇의 용량 만큼 뿌려주면 됩니다.
        override fun getItemCount(): Int {
            return photoDTOs.size
        }

        // 위에서 정의한 커스텀 이미지 뷰에 데이터로 받아온 사진을 실제로 그려주는 작업을 합니다.
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // 사진이 뿌려질 이미지 뷰 입니다.
            var imageView = (holder as CustomViewHolder).imageView

            // Glide는 이미지 올리는걸 쉽게 해주는 라이브러리입니다.
            // 그릇에 담긴 사진들을 glide 라이브러리를 이용해서 화면에 뿌려주는 겁니다.
            Glide.with(holder.itemView.context).load(photoDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)


            // 사진을 클릭하면 사진을 올린 유저의 프로필 화면으로 넘어갑니다.
            imageView.setOnClickListener {
                // 넘어가야할 프레그먼트인 유저 프로필 프레그먼트
                val fragment = UserProfileFragment()
                // 프레그먼트 전환에는 bundle을 사용합니다.
                val bundle = Bundle()

                // 선택된 사진을 올린 유저의 uid입니다.
                // position 값에 클릭한 사진의 위치가 담겨 있습니다.
                // 그 위치에 해당하는 사용자의 uid가 목적지가 됩니다.
                // 그 목적지로 프레그먼트를 전환합니다.
                bundle.putString("destinationUid", photoDTOs[position].uid)
                // 선택된 사진을 올린 유저의 이메일 아이디입니다.
                bundle.putString("userEmail", photoDTOs[position].userEmail)

                // 필요한 키-값 쌍을 담은 bundle을 인자로 줍니다.
                fragment.arguments = bundle


                // 프레그먼트를 전환합니다.
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }

            /**
             * by 팀원 김민지
             * 이미지를 한번 클릭하면 해당 user의 profile 로 이동.
             * 이미지를 꾹 누르면 comment화면 활성화 를 유도하려 했지만
             * fragment -> activity 로 전환 시에 intent가 오류가 나서 주석 처리로 남겨둡니다.
             *
             * uid와 timestamp 비교해서 일치하는 사진의 comment 화면을 불러오도록 함.
             * **/
         /*   imageView.setOnLongClickListener {
             val intent = Intent(activity, CommentActivity::class.java)
               intent.putExtra("fileId",photoDTOs[position].timestamp)
               intent.putExtra("Uid",photoDTOs[position].uid)
               requireActivity().startActivity(intent)*
            }*/
        }
    }
}
