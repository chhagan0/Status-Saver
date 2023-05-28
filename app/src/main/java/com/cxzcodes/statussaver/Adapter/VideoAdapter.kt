package com.cxzcodes.statussaver.Adapter


import com.cxzcodes.statussaver.Utils.Common
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.cxzcodes.statussaver.Models.Status
import com.cxzcodes.statussaver.R

class VideoAdapter(videoList: List<Status>, container: RelativeLayout) :
    RecyclerView.Adapter<ItemViewHolder>() {
    private val videoList: List<Status>
    private var context: Context? = null
    private val container: RelativeLayout

    init {
        this.videoList = videoList
        this.container = container
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val status: Status = videoList[position]
        if (status.isApi30()) {
//            holder.save.setVisibility(View.GONE);
            Glide.with(context!!).load(status.getDocumentFile().getUri())
                .into (holder.imageView)
        } else {
//            holder.save.setVisibility(View.VISIBLE);
            Glide.with(context!!).load(status.getFile()).into (holder.imageView)
        }
        holder.share.setOnClickListener { v ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/mp4"
            if (status.isApi30()) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, status.getDocumentFile().getUri())
            } else {
                shareIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    Uri.parse("file://" + status.getFile().getAbsolutePath())
                )
            }
            context!!.startActivity(Intent.createChooser(shareIntent, "Share image"))
        }
        val inflater = LayoutInflater.from(context)
        val view1: View = inflater.inflate(R.layout.view_video_full_screen, null)
        holder.imageView.setOnClickListener { v ->
            val alertDg = AlertDialog.Builder(context)
            val mediaControls = view1.findViewById<FrameLayout>(R.id.videoViewWrapper)
            if (view1.parent != null) {
                (view1.parent as ViewGroup).removeView(view1)
            }
            alertDg.setView(view1)
            val videoView = view1.findViewById<VideoView>(R.id.video_full)
            val mediaController =
                MediaController(context, false)
            videoView.setOnPreparedListener { mp: MediaPlayer ->
                mp.start()
                mediaController.show(0)
                mp.isLooping = true
            }
            videoView.setMediaController(mediaController)
            mediaController.setMediaPlayer(videoView)
            if (status.isApi30()) {
                videoView.setVideoURI(status.getDocumentFile().getUri())
            } else {
                videoView.setVideoURI(Uri.fromFile(status.getFile()))
            }
            videoView.requestFocus()
            (mediaController.parent as ViewGroup).removeView(mediaController)
            if (mediaControls.parent != null) {
                mediaControls.removeView(mediaController)
            }
            mediaControls.addView(mediaController)
            val alert2 = alertDg.create()
            alert2.window!!.attributes.windowAnimations = R.style.SlidingDialogAnimation
            alert2.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alert2.window!!
                .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alert2.show()
        }
        holder.save.setOnClickListener { v -> Common.copyFile(status, context!!, container) }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }
}