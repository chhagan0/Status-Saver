package com.cxzcodes.statussaver


import com.cxzcodes.statussaver.Utils.Common
import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.util.Log
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cxzcodes.statussaver.Adapter.PageAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import java.util.Objects
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private var back_pressed: Long = 0
    private var context: Context? = null
    var activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data!!
            context!!.contentResolver.takePersistableUriPermission(
                data.data!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cxzcodes.statussaver.R.layout.activity_main)
        context = applicationContext
        checkForUpdate()
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarMainActivity)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager>(R.id.viewPager)
        setSupportActionBar(toolbar)
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.images)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.videos)))


///        }
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.saved_files)))
        val adapter: PagerAdapter = PageAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager?.adapter = adapter

        viewPager?.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                viewPager?.setCurrentItem(tab.position)



            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


    }
     @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_rateUs -> {
                Toast.makeText(this, "Rate Us", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val app_url = "https://github.com/chhagan0"
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey check out my app at \n\n$app_url"
                )
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WhatsApp Status Saver")
                startActivity(Intent.createChooser(shareIntent, "Share via"))
                true
            }

            R.id.menu_privacyPolicy -> {
                startActivity(Intent(applicationContext, PrivacyPolicy::class.java))
                true
            }

            R.id.menu_aboutUs -> {
//                startActivity(Intent(applicationContext, AboutUs::class.java))
                true
            }

            R.id.menu_checkUpdate -> {
                checkForUpdate()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
        
      
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.size > 0) {
            if (arePermissionDenied()) {
                (Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE)) as ActivityManager).clearApplicationUserData()
                recreate()
            }
        }
    }

    private fun arePermissionDenied(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return contentResolver.persistedUriPermissions.size <= 0
        }
        for (permissions in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    permissions
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionDenied()) {

            // If Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissionQ()
                return
            }
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                NOTIFICATION_PERMISSION,
                NOTIFICATION_REQUEST_PERMISSIONS
            )
        }
        if (Common.APP_DIR == null || Common.APP_DIR!!.isEmpty()) {
            Common.APP_DIR = getExternalFilesDir("StatusDownloader")!!.path
            Log.d("App Path", Common.APP_DIR.toString())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun requestPermissionQ() {
        val sm = context!!.getSystemService(STORAGE_SERVICE) as StorageManager
        val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        val startDir = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        var scheme = uri.toString()
        scheme = scheme.replace("/root/", "/document/")
        scheme += "%3A$startDir"
        uri = Uri.parse(scheme)
        Log.d("URI", uri.toString())
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        intent.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        activityResultLauncher.launch(intent)
    }

    override fun onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            finish()
            moveTaskToBack(true)
        } else {
            Snackbar.make(viewPager!!, "Press Again to Exit", Snackbar.LENGTH_LONG).show()
            back_pressed = System.currentTimeMillis()
        }
    }

    private fun checkForUpdate() {
//        Executors.newSingleThreadExecutor().execute {
//            val mainHandler = Handler(Looper.getMainLooper())
//            mainHandler.post {
//                val appUpdater = AppUpdater(this@MainActivity)
//                appUpdater.setDisplay(Display.DIALOG)
//                appUpdater.setDialogAlertStyle(R.style.dialogAlertStyle)
//                appUpdater.setUpGithub("CHAGANAsir", "WhatsApp_Status_Saver")
//                appUpdater.start()
//            }
//        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 1234
        private val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        @SuppressLint("InlinedApi")
        private val NOTIFICATION_PERMISSION = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
        private const val NOTIFICATION_REQUEST_PERMISSIONS = 4
    }
}