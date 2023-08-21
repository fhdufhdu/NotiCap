package com.fhdufhdu.noticap

import android.annotation.SuppressLint
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.noti.manager.v2.NotificationDataList
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
import java.text.SimpleDateFormat


class NotificationAdapter(private val notificationDataList: NotificationDataList) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvSubText: TextView = itemView.findViewById(R.id.tv_sub_text)
        val tvText: TextView = itemView.findViewById(R.id.tv_text)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val ivChatroom: ImageView = itemView.findViewById(R.id.iv_chatroom_img)
        val cvReadMark: CardView = itemView.findViewById(R.id.cv_read_mark)
        val cvMain: CardView = itemView.findViewById(R.id.cv_main)
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
        return notificationDataList.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificationData = notificationDataList[position]
        val pref = PreferenceManager.getDefaultSharedPreferences(holder.itemView.context)

        holder.itemView.setOnClickListener {
            notificationData.pendingIntent?.send()
        }
        holder.tvTitle.text = notificationData.title
        holder.tvSubText.text = notificationData.subText ?: " "
        holder.tvText.text = notificationData.text

        holder.tvTime.text = CalculateTime.toString(pref, notificationData.time)
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
            AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }
}