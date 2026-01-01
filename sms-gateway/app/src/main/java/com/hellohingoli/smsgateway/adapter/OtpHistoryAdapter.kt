package com.hellohingoli.smsgateway.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hellohingoli.smsgateway.R
import com.hellohingoli.smsgateway.data.OtpHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for OTP history list
 */
class OtpHistoryAdapter : RecyclerView.Adapter<OtpHistoryAdapter.ViewHolder>() {
    
    private var items: List<OtpHistoryEntity> = emptyList()
    private val dateFormat = SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault())
    
    fun submitList(newItems: List<OtpHistoryEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_otp_history, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val tvOtp: TextView = itemView.findViewById(R.id.tvOtp)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        
        fun bind(item: OtpHistoryEntity) {
            tvPhone.text = "+91 ${item.phone}"
            tvOtp.text = "OTP: ${item.otp}"
            tvTime.text = dateFormat.format(Date(item.sentAt))
            tvStatus.text = item.status.uppercase()
            tvStatus.setBackgroundResource(
                if (item.status == "sent") R.drawable.bg_status_sent 
                else R.drawable.bg_status_failed
            )
        }
    }
}
