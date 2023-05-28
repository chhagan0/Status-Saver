package com.cxzcodes.statussaver.Adapter


 import com.cxzcodes.statussaver.Utils.Common
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.cxzcodes.statussaver.Models.Status
import com.cxzcodes.statussaver.R


class ImageAdapter(imagesList: List<Status>, container: RelativeLayout) :
    RecyclerView.Adapter<ItemViewHolder>() {
    private val imagesList: List<Status>
    private var context: Context? = null
    private val container: RelativeLayout

    init {
        this.imagesList = imagesList
        this.container = container
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val status: Status = imagesList[position]
        if (status.isApi30()) {
//            holder.save.setVisibility(View.GONE);
            Glide.with(context!!).load(status.getDocumentFile().getUri())
                .into(holder.imageView)
        } else {
//            holder.save.setVisibility(View.VISIBLE);
            Glide.with(context!!).load(status.getFile()).into(holder.imageView)
        }
        holder.save.setOnClickListener { v -> Common.copyFile(status, context!!, container) }
        holder.share.setOnClickListener { v ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/jpg"
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
        holder.imageView.setOnClickListener { v ->
            val alertD = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.view_image_full_screen, null)
            alertD.setView(view)
            val imageView =
                view.findViewById<ImageView>(R.id.img)
            if (status.isApi30()) {
                Glide.with(context!!).load(status.getDocumentFile().getUri()).into(imageView)
            } else {
                Glide.with(context!!).load(status.getFile()).into(imageView)
            }
            val alert = alertD.create()
            alert.window!!.attributes.windowAnimations = R.style.SlidingDialogAnimation
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alert.window!!
                .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alert.show()
        }
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }
}
