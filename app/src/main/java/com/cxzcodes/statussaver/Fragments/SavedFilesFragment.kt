package com.cxzcodes.statussaver.Fragments


import com.cxzcodes.statussaver.Utils.Common
import android.R
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.cxzcodes.statussaver.Adapter.FilesAdapter
import com.cxzcodes.statussaver.Models.Status
import java.io.File
import java.util.Arrays

class SavedFilesFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private val savedFilesList: MutableList<Status> = ArrayList<Status>()
    private val handler = Handler()
    private var filesAdapter: FilesAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var no_files_found: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.cxzcodes.statussaver.R.layout.fragment_saved_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(com.cxzcodes.statussaver.R.id.recyclerViewFiles)
        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(com.cxzcodes.statussaver.R.id.swipeRefreshLayoutFiles)
        progressBar = view.findViewById<ProgressBar>(com.cxzcodes.statussaver.R.id.progressBar)
        no_files_found = view.findViewById<TextView>(com.cxzcodes.statussaver.R.id.no_files_found)
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
                files
            }
        }
        recyclerView?.let { view ->
            view.setHasFixedSize(true)
            view.layoutManager = GridLayoutManager(activity, Common.GRID_COUNT)
        }
        files
    }

    //    private Bitmap getThumbnail(Status status) {
    private val files: Unit
        private get() {
            val app_dir: File = File(Common.APP_DIR)
            if (app_dir.exists() ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            ) {
                no_files_found!!.visibility = View.GONE
                Thread {
                    val savedFiles: Array<File>?
                    savedFiles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val f = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM
                            ).toString() + File.separator + "status_saver"
                        )
                        f.listFiles()
                    } else {
                        app_dir.listFiles()
                    }
                    savedFilesList.clear()
                    if (savedFiles != null && savedFiles.size > 0) {
                        Arrays.sort(savedFiles)
                        for (file in savedFiles) {
                            val status =
                                Status(file, file.name, file.absolutePath)
                            savedFilesList.add(status)
                        }
                        handler.post {
                            filesAdapter = FilesAdapter(savedFilesList)
                            recyclerView!!.adapter = filesAdapter
                            filesAdapter!!.notifyDataSetChanged()
                            progressBar!!.visibility = View.GONE
                        }
                    } else {
                        handler.post {
                            progressBar!!.visibility = View.GONE
                            no_files_found!!.visibility = View.VISIBLE
                        }
                    }
                    swipeRefreshLayout!!.isRefreshing = false
                }.start()
            } else {
                no_files_found!!.visibility = View.VISIBLE
                progressBar!!.visibility = View.GONE
            }
        }
    //        return a.CHAGAN.statusdownloader.Utils.ThumbnailUtils.createVideoThumbnail(status.getFile().getAbsolutePath(),
    //                3);
    //    }
}