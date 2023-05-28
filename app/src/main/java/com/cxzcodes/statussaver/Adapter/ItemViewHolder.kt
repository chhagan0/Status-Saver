package com.cxzcodes.statussaver.Adapter

 import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.cxzcodes.statussaver.R


class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var save: ImageButton
    var share: ImageButton
    var imageView: ImageView

    init {
        imageView = itemView.findViewById<ImageView>(R.id.ivThumbnail)
        save = itemView.findViewById<ImageButton>(R.id.save)
        share = itemView.findViewById<ImageButton>(R.id.share)
    }
}