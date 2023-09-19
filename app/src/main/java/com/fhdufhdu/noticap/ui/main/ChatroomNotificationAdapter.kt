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
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.util.TimeCalculator
import com.fhdufhdu.noticap.ui.main.detail.DetailActivity
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationDao
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationDatabase
import com.fhdufhdu.noticap.util.SizeManager
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationPerChatroom
import com.fhdufhdu.noticap.util.CoroutineManager
import com.fhdufhdu.noticap.util.IconConverter
import com.fhdufhdu.noticap.util.SharedPreferenceManager
import com.gun0912.tedpermission.provider.TedPermissionProvider
import kotlinx.coroutines.delay


class ChatroomNotificationAdapter(applicationContext: Context) :
    RecyclerView.Adapter<ChatroomNotificationAdapter.ViewHolder>() {

    private val dao: KakaoNotificationDao
    private var kakaoNotificationsPerChatroom: List<KakaoNotificationPerChatroom> = ArrayList()

    init {
        dao = KakaoNotificationDatabase.getInstance(applicationContext).kakaoNotificationDao()
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
        return kakaoNotificationsPerChatroom.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificationData = kakaoNotificationsPerChatroom[position]
        val context = holder.itemView.context

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("CHATROOM_NAME", notificationData.chatroomName)
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(it.context)
                .setMessage("채팅방을 삭제하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    CoroutineManager.run {
                        dao.deleteOne(
                            notificationData.chatroomName
                        )
                    }
                }
                .setNegativeButton("취소", null)
                .show()
            true
        }
        holder.tvTitle.text = notificationData.sender
        holder.tvSubText.text =
            if (notificationData.chatroomName == notificationData.sender) "" else notificationData.chatroomName
        holder.tvText.text = notificationData.content

        val unreadCount = notificationData.unreadCount
        val unreadStr = if (unreadCount > 0) "($unreadCount)" else ""
        "${
            TimeCalculator.toString(
                SharedPreferenceManager.getTimeFormatType(context),
                notificationData.time
            )
        } $unreadStr".also { holder.tvTime.text = it }
        holder.cvReadMark.visibility =
            if (notificationData.unread) CardView.VISIBLE else CardView.INVISIBLE
        holder.ivChatroom.setImageIcon(IconConverter.stringToIcon(notificationData.personIcon))

//        if (notificationData.doRunAnimation) {
//            setAnimation(holder.itemView)
//            CoroutineManager.run{
//                delay(1500)
//                dao.updateDoRunAnimation(notificationData.id)
//            }
//        }
    }

    private fun setAnimation(viewToAnimate: View) {
        val animation: Animation =
            AnimationUtils.loadAnimation(
                viewToAnimate.context,
                android.R.anim.slide_in_left
            )
        viewToAnimate.startAnimation(animation)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(kakaoNotificationsPerChatroom: List<KakaoNotificationPerChatroom>) {
        this.kakaoNotificationsPerChatroom = kakaoNotificationsPerChatroom
        notifyDataSetChanged()
    }
}