package com.fhdufhdu.noticap

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.noti.manager.v2.NotificationDataList
import com.fhdufhdu.noticap.noti.manager.v2.NotificationDataManager
import com.gun0912.tedpermission.provider.TedPermissionProvider


class NotificationMainAdapter :
    RecyclerView.Adapter<NotificationMainAdapter.ViewHolder> {

    private val notificationMap: HashMap<String, NotificationDataList>
    private var notificationSortedDataList: ArrayList<NotificationDataList>

    constructor() {
        val notificationDataManager = NotificationDataManager.getInstance()
        this.notificationMap = notificationDataManager.notificationMap
        this.notificationSortedDataList = notificationDataManager.getSortedDataLists()
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
        val notificationDataManager = NotificationDataManager.getInstance()
        val notificationData = notificationSortedDataList[position][0]
        val pref = PreferenceManager.getDefaultSharedPreferences(holder.itemView.context)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            var intent = Intent(context, NotificationActivity::class.java)
            intent.putExtra("CHATROOM_NAME", notificationData.getChatroomName())
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(it.context)
                .setMessage("채팅방을 삭제하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    notificationDataManager.deleteChatroom(
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

        val unreadCount = notificationDataManager.getUnreadCount(notificationData.getChatroomName())
        val unreadStr = if (unreadCount > 0) "($unreadCount)" else ""
        "${CalculateTime.toString(pref, notificationData.time)} $unreadStr".also { holder.tvTime.text = it }
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
    fun update(context: Context) {
        val notificationDataManager = NotificationDataManager.getInstance()
        this.notificationSortedDataList = notificationDataManager.getSortedDataLists()
        notificationDataManager.saveJson(context)
        notifyDataSetChanged()
    }
}