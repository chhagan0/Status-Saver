package com.cxzcodes.statussaver.Fragments


import com.cxzcodes.statussaver.Utils.Common
import android.R
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.cxzcodes.statussaver.Adapter.ImageAdapter
import com.cxzcodes.statussaver.Models.Status
import java.io.File
import java.util.Arrays
import java.util.Objects
import java.util.concurrent.Executors

class ImageFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private val imagesList: MutableList<Status> = ArrayList<Status>()
    private var imageAdapter: ImageAdapter? = null
    private var container: RelativeLayout? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var messageTextView: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.cxzcodes.statussaver.R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(com.cxzcodes.statussaver.R.id.recyclerViewImage)
        progressBar = view.findViewById<ProgressBar>(com.cxzcodes.statussaver.R.id.prgressBarImage)
        container = view.findViewById<RelativeLayout>(com.cxzcodes.statussaver.R.id.image_container)
        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(com.cxzcodes.statussaver.R.id.swipeRefreshLayout)
        messageTextView = view.findViewById<TextView>(com.cxzcodes.statussaver.R.id.messageTextImage)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        swipeRefreshLayout?.let { layout ->
            val activity = requireActivity()

            layout.setColorSchemeColors(
                ContextCompat.getColor(activity, R.color.holo_orange_dark),
                ContextCompat.getColor(activity, R.color.holo_green_dark),
                ContextCompat.getColor(activity, com.cxzcodes.statussaver.R.color.colorPrimary),
                ContextCompat.getColor(activity, R.color.holo_blue_dark)
            )
        }


        swipeRefreshLayout?.let { layout ->
            layout.setOnRefreshListener {
                // Handle refresh event here
                status
            }
        }
        recyclerView?.let { view ->
            view.setHasFixedSize(true)
            view.layoutManager = GridLayoutManager(activity, Common.GRID_COUNT)
        }

        status
    }

    private val status: Unit
        private get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                executeNew()
            } else if (Common.STATUS_DIRECTORY.exists()) {
                executeOld()
            } else {
                messageTextView!!.visibility = View.VISIBLE
                messageTextView!!.setText(com.cxzcodes.statussaver.R.string.cant_find_whatsapp_dir)
                Toast.makeText(
                    activity,
                    getString(com.cxzcodes.statussaver.R.string.cant_find_whatsapp_dir),
                    Toast.LENGTH_SHORT
                ).show()
                swipeRefreshLayout!!.isRefreshing = false
            }
        }

    private fun executeOld() {
        Executors.newSingleThreadExecutor().execute {
            val mainHandler = Handler(Looper.getMainLooper())
            val statusFiles: Array<File>
            statusFiles = Common.STATUS_DIRECTORY.listFiles()
            imagesList.clear()
            if (statusFiles != null && statusFiles.size > 0) {
                Arrays.sort(statusFiles)
                for (file in statusFiles) {
                    if (file.name.contains(".nomedia")) continue
                    val status = Status(file, file.name, file.absolutePath)
                    if (!status.isVideo() && status.getTitle().endsWith(".jpg")) {
                        imagesList.add(status)
                    }
                }
                mainHandler.post {
                    if (imagesList.size <= 0) {
                        messageTextView!!.visibility = View.VISIBLE
                        messageTextView!!.setText(com.cxzcodes.statussaver.R.string.no_files_found)
                    } else {
                        messageTextView!!.visibility = View.GONE
                        messageTextView!!.text = ""
                    }
                    imageAdapter = container?.let { ImageAdapter(imagesList, it) }
                    recyclerView!!.adapter = imageAdapter
                    imageAdapter?.notifyItemRangeChanged(0, imagesList.size)
                    progressBar!!.visibility = View.GONE
                }
            } else {
                mainHandler.post {
                    progressBar!!.visibility = View.GONE
                    messageTextView!!.visibility = View.VISIBLE
                    messageTextView!!.setText(com.cxzcodes.statussaver.R.string.no_files_found)
                    Toast.makeText(
                        activity,
                        getString(com.cxzcodes.statussaver.R.string.no_files_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            swipeRefreshLayout!!.isRefreshing = false
        }
    }

    private fun executeNew() {
        Executors.newSingleThreadExecutor().execute {
            val mainHandler = Handler(Looper.getMainLooper())
            val list =
                requireActivity().contentResolver.persistedUriPermissions
            val file =
                DocumentFile.fromTreeUri(requireActivity(), list[0].uri)
            imagesList.clear()
            if (file == null) {
                mainHandler.post {
                    progressBar!!.visibility = View.GONE
                    messageTextView!!.visibility = View.VISIBLE
                    messageTextView!!.setText(com.cxzcodes.statussaver.R.string.no_files_found)
                    Toast.makeText(
                        activity,
                        getString(com.cxzcodes.statussaver.R.string.no_files_found),
                        Toast.LENGTH_SHORT
                    ).show()
                    swipeRefreshLayout!!.isRefreshing = false
                }
                return@execute
            }
            val statusFiles = file.listFiles()
            if (statusFiles.size <= 0) {
                mainHandler.post {
                    progressBar!!.visibility = View.GONE
                    messageTextView!!.visibility = View.VISIBLE
                    messageTextView!!.setText(com.cxzcodes.statussaver.R.string.no_files_found)
                    Toast.makeText(
                        activity,
                        getString(com.cxzcodes.statussaver.R.string.no_files_found),
                        Toast.LENGTH_SHORT
                    ).show()
                    swipeRefreshLayout!!.isRefreshing = false
                }
                return@execute
            }
            for (documentFile in statusFiles) {
                if (Objects.requireNonNull(documentFile.name)
                        ?.contains(".nomedia") == true
                ) continue
                val status = Status(documentFile)
                if (!status.isVideo()) {
                    imagesList.add(status)
                }
            }
            mainHandler.post {
                if (imagesList.size <= 0) {
                    messageTextView!!.visibility = View.VISIBLE
                    messageTextView!!.setText(com.cxzcodes.statussaver.R.string.no_files_found)
                } else {
                    messageTextView!!.visibility = View.GONE
                    messageTextView!!.text = ""
                }
                imageAdapter = container?.let { ImageAdapter(imagesList, it) }
                recyclerView!!.adapter = imageAdapter
                imageAdapter?.notifyItemRangeChanged(0, imagesList.size)
                progressBar!!.visibility = View.GONE
            }
        }
    }
}