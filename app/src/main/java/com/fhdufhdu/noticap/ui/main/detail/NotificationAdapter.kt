package com.fhdufhdu.noticap.ui.main.detail

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.notification.manager.MemDB
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity
import com.fhdufhdu.noticap.util.SharedPreferenceManager
import com.fhdufhdu.noticap.util.SizeManager
import com.fhdufhdu.noticap.util.TimeCalculator

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private var kakaoNotifications: List<KakaoNotificationEntity> = ArrayList()
    private val memDB: MemDB = MemDB.getInstance()

    class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvSubText: TextView = itemView.findViewById(R.id.tv_sub_text)
        val tvText: TextView = itemView.findViewById(R.id.tv_text)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val ivChatroom: ImageView = itemView.findViewById(R.id.iv_chatroom_img)
        val cvReadMark: CardView = itemView.findViewById(R.id.cv_read_mark)
        val cvMain: CardView = itemView.findViewById(R.id.cv_main)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
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

    override fun getItemCount(): Int = kakaoNotifications.size

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val notificationData = kakaoNotifications[position]
        val context = holder.itemView.context

        holder.itemView.setOnClickListener {
            if (SharedPreferenceManager.whetherToMoveToKakao(context)) {
                val pendingIntent =
                    MemDB.getInstance().pendingIntentMap[notificationData.chatroomName]
                if (pendingIntent != null) {
                    pendingIntent.send()
                } else {
                    val compName =
                        ComponentName("com.kakao.talk", "com.kakao.talk.activity.SplashActivity")
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_ALTERNATIVE)
                    intent.component = compName
                    context.startActivity(intent)
                }
            }
        }
        holder.tvTitle.text = notificationData.sender
        holder.tvSubText.text =
            if (notificationData.chatroomName == notificationData.sender) "" else notificationData.chatroomName
        holder.tvText.text = notificationData.content

        holder.tvTime.text =
            TimeCalculator.toString(
                SharedPreferenceManager.getTimeFormatType(context),
                notificationData.time
            )
        holder.cvReadMark.visibility =
            if (notificationData.unread) CardView.VISIBLE else CardView.INVISIBLE
        val icon = memDB.getOrSaveIconCompat(holder.itemView.context, notificationData.personKey, notificationData.personIcon)
        if (icon != null) {
            holder.ivChatroom.setImageIcon(icon.toIcon(holder.itemView.context))
        }
    }

    private fun setAnimation(viewToAnimate: View) {
        val animation: Animation =
            AnimationUtils.loadAnimation(viewToAnimate.context, android.R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(kakaoNotifications: List<KakaoNotificationEntity>) {
        this.kakaoNotifications = kakaoNotifications
        notifyDataSetChanged()
    }
}
