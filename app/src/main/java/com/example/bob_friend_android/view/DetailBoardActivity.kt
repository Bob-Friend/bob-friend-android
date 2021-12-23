package com.example.bob_friend_android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bob_friend_android.App
import com.example.bob_friend_android.KeyboardVisibilityUtils
import com.example.bob_friend_android.R
import com.example.bob_friend_android.adapter.CommentAdapter
import com.example.bob_friend_android.adapter.UserAdapter
import com.example.bob_friend_android.databinding.ActivityDetailBoardBinding
import com.example.bob_friend_android.model.Comment
import com.example.bob_friend_android.model.UserItem
import com.example.bob_friend_android.viewmodel.BoardViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import java.util.*
import kotlin.collections.ArrayList


class DetailBoardActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "DetailBoardActivity"
    private lateinit var binding: ActivityDetailBoardBinding
    private lateinit var viewModel: BoardViewModel

    private var detailBoardId : Int = 0
    private var detailCommentId : Int = 0
    private var userId: Int = 0

    private val commentList : ArrayList<Comment> = ArrayList()
    private val userList : ArrayList<UserItem> = ArrayList()
    private var commentAdapter = CommentAdapter(commentList, detailBoardId)
    private val userAdapter = UserAdapter(userList)

    private lateinit var mapView: MapView
    private val LOCATION_PERMISSTION_REQUEST_CODE: Int = 1000
    private lateinit var locationSource: FusedLocationSource // 위치를 반환하는 구현체
    private lateinit var naverMap: NaverMap

    var appointmentTime = ""

    var inputMethodManager: InputMethodManager? = null

    var selectedId: Int? = null
    var flag: Boolean = false

    var toast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_board)
        viewModel = ViewModelProvider(this).get(BoardViewModel::class.java)
        binding.lifecycleOwner = this
        binding.detail = viewModel

        mapView = binding.detailMapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSTION_REQUEST_CODE)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        commentAdapter.commentClick = object : CommentAdapter.CommentClick {
            override fun onCommentClick(view: View, position: Int, comment: Comment) {
                withCommentItems(comment.id, comment.author!!.id, true, null, position)
            }
        }

        commentAdapter.reCommentClick = object : CommentAdapter.ReCommentClick {
            override fun onReCommentClick(
                view: View,
                position: Int,
                commentId: Int,
                reComment: Comment
            ) {
                withCommentItems(commentId, reComment.author!!.id, false, reComment.id, null)
            }
        }

        observeData()

        if(intent.hasExtra("boardId")) {
            detailBoardId = intent.getIntExtra("boardId", 0)
            userId = intent.getIntExtra("userId", 0)

            val swipe = binding.swipeLayout
            swipe.setOnRefreshListener {
                viewModel.readBoard(detailBoardId)
                swipe.isRefreshing = false
            }

            binding.detailButton.setOnClickListener {
                if(binding.detailButton.text=="마감하기"){
                    viewModel.closeBoard(detailBoardId)
                    val intent = Intent().apply {
                        putExtra("CallType", "close")
                    }
                    setResult(RESULT_OK, intent)
                    if(!isFinishing) finish()
                }
                else {
                    viewModel.participateBoard(detailBoardId)
                }
            }

            viewModel.readBoard(detailBoardId)
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.editTextComment.setOnEditorActionListener{ textView, action, event ->
            var handled = false

            if (action == EditorInfo.IME_ACTION_DONE) {
                // 키보드 내리기
                inputMethodManager!!.hideSoftInputFromWindow(binding.editTextComment.windowToken, 0)
                handled = true
            }

            handled
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        if (userId == App.prefs.getInt("id", 0)){
            menu?.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "삭제하기")
        }
        else {
            Log.d(TAG, "userId: $userId")
            menu?.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, "신고하기")
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            Menu.FIRST + 1 -> {
                viewModel.deleteBoard(detailBoardId)
                val intent = Intent().apply {
                    putExtra("CallType", "delete")
                }
                setResult(RESULT_OK, intent)
                if(!isFinishing) finish()
            }
            Menu.FIRST + 2 -> {
                Log.d(TAG, "boardId: $detailBoardId")
                viewModel.reportBoard(detailBoardId)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun withCommentItems(
        commentId: Int,
        commentWriterId: Int,
        Comment: Boolean,
        recommentId: Int?,
        position: Int?
    ) {
        var writer = false

        if (commentWriterId == App.prefs.getInt("id", -1)){
            writer = true
        }

        val dialog = DialogCommentFragment(writer, Comment)
        // 버튼 클릭 이벤트 설정
        dialog.setButtonClickListener(object : DialogCommentFragment.OnButtonClickListener {
            override fun onAddReCommentClicked() {
                if (position != null) {
                    if (selectedId!=null) {
                        binding.commentRecyclerview[selectedId!!].setBackgroundColor(Color.parseColor("#FFFFFF"))
                    }
                    selectedId = position
                    binding.commentRecyclerview[position].setBackgroundColor(Color.parseColor("#DADAEC"))
//                    binding.commentRecyclerview.setBackgroundColor(Color.WHITE)
                    showSoftInput()
                    detailCommentId = commentId
                    flag = true
                }
            }

            override fun onReportCommentClicked() {
                if (Comment) {
                    viewModel.reportComment(detailBoardId, commentId)
                } else {
                    if (recommentId != null) {
                        viewModel.reportReComment(detailBoardId, commentId, recommentId)
                    }
                }
            }

            override fun onDeleteCommentClicked() {
                if (Comment) {
                    viewModel.deleteComment(detailBoardId, commentId)
                } else {
                    if (recommentId != null) {
                        viewModel.deleteReComment(detailBoardId, commentId, recommentId)
                    }
                }
            }
        })
        dialog.show(supportFragmentManager, "CustomDialog")
    }


    fun showSoftInput() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.editTextComment.requestFocus()
        binding.editTextComment.postDelayed({
            inputMethodManager.showSoftInput(binding.editTextComment, 0)
        }, 100)
    }

    override fun onBackPressed() {
        if (flag) {
            Toast.makeText(this, "한번 더 클릭시 화면이 종료됩니다.", Toast.LENGTH_SHORT).show()
            viewModel.readBoard(detailBoardId)
            flag = false
            return
        }
        else {
            super.onBackPressed()
        }
    }


    private fun observeData() {
        with(viewModel) {
            result.observe(this@DetailBoardActivity, Observer { board ->
                detailBoardId = board.id
                binding.detailCurrentMember.text = board.currentNumberOfPeople.toString()
                binding.detailCurrentComment.text = board.amountOfComments.toString()
                binding.readWriter.text = board.author?.nickname
                binding.detailWriteTime.text = board.createdAt

                binding.detailTitle.text = board.title
                binding.detailContent.text = board.content
                binding.readWriter.text = board.author!!.nickname

                val marker = Marker()
                marker.position = LatLng(board.latitude!!, board.longitude!!)
                marker.map = naverMap
                val cameraPosition = CameraPosition( // 카메라 위치 변경
                    LatLng(board.latitude!!, board.longitude!!),  // 위치 지정
                    15.0 // 줌 레벨
                )
                naverMap.cameraPosition = cameraPosition // 변경된 위치 반영

                if (board.ageRestrictionStart != null && board.ageRestrictionEnd != null) {
                    val ageFilter = board.ageRestrictionStart.toString() + "부터 " + board.ageRestrictionEnd.toString() + "까지"
                    binding.detailAge2.text = ageFilter
                }
                else {
                    binding.detailAgeLayout.visibility = View.GONE
                }

                when (board.sexRestriction) {
                    "NONE" -> {
                        binding.detailGenderLayout.visibility = View.GONE
                    }
                    "FEMALE" -> {
                        binding.detailGender2.text = "여성"
                    }
                    "MALE" -> {
                        binding.detailGender2.text = "남성"
                    }
                }

                if (board.appointmentTime != null) {
                    val createDay: String = board.appointmentTime!!
                    val created = createDay.split("T")
                    appointmentTime = created[0] + ", " + created[1].substring(0, 5)
                }
                binding.readMeetingTime2.text = appointmentTime
                binding.detailCurrentMember.text = board.currentNumberOfPeople.toString()
                binding.detailTotalMember.text = board.totalNumberOfPeople.toString()
                binding.detailAppointmentPlaceName.text = board.restaurantName
                binding.detailCurrentComment.text = board.amountOfComments.toString()

                binding.commentRecyclerview.adapter = commentAdapter
                val commentLayoutManager = LinearLayoutManager(this@DetailBoardActivity, RecyclerView.VERTICAL, false)
                binding.commentRecyclerview.layoutManager = commentLayoutManager

                binding.postComment.setOnClickListener {
                    if (binding.editTextComment.text.toString() != "" && !flag) {
                        viewModel.addComment(
                            detailBoardId,
                            binding.editTextComment.text.toString()
                        )
                        binding.editTextComment.text = null
                    } else if (binding.editTextComment.text.toString() != "" && flag) {
                        viewModel.addReComment(
                            detailBoardId,
                            detailCommentId,
                            binding.editTextComment.text.toString()
                        )
                        binding.editTextComment.text = null
                        flag = false
                    }
                }
                binding.detailMember.layoutManager = LinearLayoutManager(
                    this@DetailBoardActivity,
                    RecyclerView.VERTICAL,
                    false
                )
                binding.detailMember.adapter = userAdapter

                if (board.author!!.id == App.prefs.getInt("id", -1)) {
                    binding.detailButton.text = "마감하기"
                } else {
                    binding.detailButton.text = "참가하기"
                    for (member in board.members!!) {
                        if (member.id == App.prefs.getInt("id", -1)) {
                            binding.detailButton.text = "취소하기"
                        }
                    }
                }

                commentList.clear()
                if (board.comments != null) {
                    for (comment in board.comments!!) {
                        commentList.add(comment)
                        if (comment.replies !== null) {
                            for (recomment in comment.replies!!) {
                                val reComment = Comment(
                                    recomment.id,
                                    recomment.author,
                                    recomment.content,
                                    recomment.replies,
                                    typeFlag = 1,
                                    createdAt = recomment.createdAt
                                )
                                commentList.add(reComment)
                            }
                        }
                    }
                }

                userList.clear()
                for (member in board.members!!) {
                    userList.add(member)
                }
            })

            val dialog = LoadingDialog(this@DetailBoardActivity)
            progressVisible.observe(this@DetailBoardActivity) {
                if (progressVisible.value!!) {
                    dialog.show()
                }
                else if (!progressVisible.value!!) {
                    dialog.dismiss()
                }
            }

            errorMsg.observe(this@DetailBoardActivity) {
                when (errorMsg.value) {
                    "삭제되거나 마감된 글입니다." -> {
                        setBoardDialog(this@DetailBoardActivity)
                    }
                    "댓글이 삭제되었습니다." -> {
                        readBoard(detailBoardId)
                    }
                    else -> {
                        showToast(it)
                    }
                }
            }
        }
    }


    private fun setBoardDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("접근할 수 없는 약속")
            setMessage("마감되거나 삭제되어 접근 할 수 없는 약속입니다..")
            setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                finish()
            })
            show()
        }
    }


    @SuppressLint("ShowToast")
    private fun showToast(msg: String) {
        if (toast == null) {
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        } else toast?.setText(msg)
        toast?.show()
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}