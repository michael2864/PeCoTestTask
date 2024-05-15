package com.example.pecotesttask

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: MyPagerAdapter
    private lateinit var btnAddFragment: Button
    private lateinit var btnDeleteFragment: Button
    private lateinit var textViewFragmentNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val fragmentCount = sharedPreferences.getInt("fragmentCount", 0)

        createNotificationChannel()

        setContentView(R.layout.activity_main)


        // Initialize views
        viewPager = findViewById(R.id.upper_fragment_container)
        btnAddFragment = findViewById(R.id.btn_add_fragment)
        btnDeleteFragment = findViewById(R.id.btn_delete_fragment)
        textViewFragmentNumber = findViewById(R.id.textview_fragment_number)

        // Set up ViewPager adapter
        adapter = MyPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        // Set up buttons
        btnAddFragment.setOnClickListener {
            addFragment()
        }

        btnDeleteFragment.setOnClickListener {
            deleteFragment()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Update the visibility of the "Delete fragment" button
                updateDeleteFragmentButtonVisibility()
            }
        })

        updateDeleteFragmentButtonVisibility()

        for (i in 0 until fragmentCount-1) {
            //  val fragment = PageFragment.newInstance(i + 1)
            //  supportFragmentManager.beginTransaction()
            //      .add(R.id.upper_fragment_container, fragment, "fragment_$i")
            //      .commit()

            addFragment()

        }

    }



    override fun onStop() {
        super.onStop()

        // Get the SharedPreferences instance
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Get the editor for SharedPreferences
        val editor = sharedPreferences.edit()

        // Save the count of fragments
        editor.putInt("fragmentCount", supportFragmentManager.fragments.size)
        // Commit the changes
        editor.apply()
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "200"
            val channelName = "Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }



    private fun updateDeleteFragmentButtonVisibility() {
        if (viewPager.adapter?.itemCount ?: 0 <= 1) {
            // If there's only one or zero fragments, hide the "Delete fragment" button
            btnDeleteFragment.visibility = View.GONE
        } else {
            // If there's more than one fragment, show the "Delete fragment" button
            btnDeleteFragment.visibility = View.VISIBLE
        }
    }


    private fun addFragment() {
        adapter.addFragment()
        // Update fragment number in TextView
        textViewFragmentNumber.text = "${adapter.itemCount}"
    }

    private fun deleteFragment() {
        if (adapter.itemCount > 0) {
            adapter.removeLastFragment()
            // Update fragment number in TextView
            textViewFragmentNumber.text = "${adapter.itemCount}"
        }
    }
}

class MyPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragmentList = mutableListOf<Fragment>()

    init {
        // Add a default fragment during initialization
        fragmentList.add(PageFragment.newInstance(1))
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun addFragment() {
        val fragment = PageFragment.newInstance(fragmentList.size + 1)
        fragmentList.add(fragment)
        notifyDataSetChanged()
    }

    fun removeLastFragment() {
        if (fragmentList.size > 1) {
            fragmentList.removeAt(fragmentList.size - 1)
            notifyDataSetChanged()
        }
    }
}


class PageFragment : Fragment() {

    companion object {
        private const val ARG_FRAGMENT_NUMBER = "fragment_number"

        fun newInstance(fragmentNumber: Int): PageFragment {
            val fragment = PageFragment()
            val args = Bundle()
            args.putInt(ARG_FRAGMENT_NUMBER, fragmentNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my, container, false)

        // Initialize button and set click listener
        val btnCreateNotification = view.findViewById<Button>(R.id.btn_create_notification)
        btnCreateNotification.setOnClickListener {
            Log.d("PageFragment", "Create notification button clicked")
            createNotification()
        }

        return view
    }

    private fun createNotification() {


        Log.d("PageFragment", "Creating notification")
        val fragmentNumber = requireArguments().getInt(ARG_FRAGMENT_NUMBER, 0)
        val notificationText = "You created fragment $fragmentNumber"
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Notification.Builder(requireContext(), "200")
            .setContentTitle("New Notification")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        notificationManager.notify(fragmentNumber, notification)
    }
}

