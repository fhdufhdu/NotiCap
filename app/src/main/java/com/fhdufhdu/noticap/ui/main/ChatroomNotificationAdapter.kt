package com.fhdufhdu.noticap.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.util.TimeCalculator
import com.fhdufhdu.noticap.ui.main.detail.DetailActivity
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.util.SizeManager
import com.fhdufhdu.noticap.noti.manager.v2.KakaoNotificationDataList
import com.fhdufhdu.noticap.noti.manager.v2.KakaoNotificationDataManager
import com.fhdufhdu.noticap.util.SharedPreferenceManager
import com.gun0912.tedpermission.provider.TedPermissionProvider


class ChatroomNotificationAdapter :
    RecyclerView.Adapter<ChatroomNotificationAdapter.ViewHolder> {

    private val notificationMap: HashMap<String, KakaoNotificationDataList>
    private var notificationSortedDataList: ArrayList<KakaoNotificationDataList>

    constructor() {
        val kakaoNotificationDataManager = KakaoNotificationDataManager.getInstance()
        this.notificationMap = kakaoNotificationDataManager.notificationMap
        this.notificationSortedDataList = kakaoNotificationDataManager.getSortedDataLists()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvSubText: TextView = itemView.findViewById(R.id.tv_sub_text)
        val tvText: TextView = itemView.findViewById(R.id.tv_text)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val ivChatroom: ImageView = itemView.findViewById(R.id.iv_chatroom_img)
        val cvReadMark: CardView = itemView.findViewById(R.id.cv_read_mark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        val viewHolder = ViewHolder(view)

        // max width 설정
        val displayMetrics = viewHolder.itemView.resources.displayMetrics
        val widthPx = displayMetrics.widthPixels
        val widthDp = SizeManager.pxToDp(widthPx, displayMetrics)
        val tvTextMaxWidth = widthDp - 94

        viewHolder.tvText.maxWidth = SizeManager.dpToPx(tvTextMaxWidth, displayMetrics)

        return viewHolder
    }

    override fun getItemCount(): Int {
        return notificationSortedDataList.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kakaoNotificationDataManager = KakaoNotificationDataManager.getInstance()
        val notificationData = notificationSortedDataList[position][0]
        val context = holder.itemView.context

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("CHATROOM_NAME", notificationData.getChatroomName())
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(it.context)
                .setMessage("채팅방을 삭제하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    kakaoNotificationDataManager.deleteChatroom(
                        it.context,
                        notificationData.getChatroomName()
                    )
                }
                .setNegativeButton("취소", null)
                .show()
            true
        }
        holder.tvTitle.text = notificationData.title
        holder.tvSubText.text = notificationData.subText ?: " "
        holder.tvText.text = notificationData.text

        val unreadCount =
            kakaoNotificationDataManager.getUnreadCount(notificationData.getChatroomName())
        val unreadStr = if (unreadCount > 0) "($unreadCount)" else ""
        "${
            TimeCalculator.toString(
                SharedPreferenceManager.getTimeFormatType(context),
                notificationData.time
            )
        } $unreadStr".also { holder.tvTime.text = it }
        holder.cvReadMark.visibility =
            if (notificationData.unread) CardView.VISIBLE else CardView.INVISIBLE
        holder.ivChatroom.setImageIcon(notificationData.personIcon)

        if (notificationData.doRunAnimation) {
            setAnimation(holder.itemView)
            notificationData.doRunAnimation = false
        }
    }

    private fun setAnimation(viewToAnimate: View) {
        val animation: Animation =
            AnimationUtils.loadAnimation(
                TedPermissionProvider.context,
                android.R.anim.slide_in_left
            )
        viewToAnimate.startAnimation(animation)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        val kakaoNotificationDataManager = KakaoNotificationDataManager.getInstance()
        this.notificationSortedDataList = kakaoNotificationDataManager.getSortedDataLists()
        notifyDataSetChanged()
    }
}