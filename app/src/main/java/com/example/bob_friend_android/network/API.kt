package com.example.bob_friend_android.network

import com.example.bob_friend_android.model.Board
import com.example.bob_friend_android.model.Token
import com.example.bob_friend_android.model.User
import retrofit2.Call
import retrofit2.http.*

interface API {

    //회원가입
    @POST("api/signup")
    fun getJoinResponse(@Body user: Map<String, String>): Call<User>

    //아이디 중복확인
    @GET("api/username/{username}")
    fun getIdCheck(@Path("username") username : String): Call<Boolean>

    //이메일 중복 확인
    @GET("api/email/{email}")
    fun getEmailCheck(@Path("email") email : String): Call<Boolean>

    //로그인
    @POST("api/signin")
    fun getLoginResponse(@Body user : Map<String, String>): Call<Token>

    //사용자 정보 가져오기
    @GET("api/user")
    fun getUserId(): Call<User>

    //토큰 가져오기
    @GET("api/validate")
    fun getToken(): Call<Boolean>

    //회원탈퇴
    @DELETE("api/user/{id}")
    fun deleteUser(): Call<Void>

    //약속잡기
    @POST("/recruitments")
    fun addRecruitmens(@Body board: Board): Call<Board>

    //약속조회
    @GET("/recruitments")
    fun getRecruitmens(): Call<List<Board>>

    //내 참가약속조회
    @GET("/recruitments/my/joined")
    fun getJoinRecruitmens(): Call<List<Board>>

    //내가 잡은 약속
    @GET("/recruitments/my")
    fun getMyRecruitmens(): Call<List<Board>>

    //약속삭제
    @DELETE("/recruitments/{id}")
    fun deleteRecruitmens(@Path("id") boardId: Int): Call<Void>
}