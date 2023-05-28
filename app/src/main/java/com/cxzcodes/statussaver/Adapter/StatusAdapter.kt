import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.cxzcodes.statussaver.R

class StatusAdapter(private val images: List<Bitmap>) : RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

      class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusImageView: ImageView = itemView.findViewById(R.id.statusimage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.status_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//         Glide.with(holder.itemView.context)
//            .load(Uri.parse(modelClass[position].fileuri))
//            .into(holder.statusImageView)
        val image = images[position]
        holder.statusImageView.setImageBitmap(image)
    }

    override fun getItemCount(): Int {
        return images.size
    }
}
