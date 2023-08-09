package com.example.noticap.v2;

class KakaoTalkNoti constructor(
    val id: Int,
    val chatroomName: String,
    val sender: String,
    val text: String
) {
    val time: Long = System.currentTimeMillis()
}