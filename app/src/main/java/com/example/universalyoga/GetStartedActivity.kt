package com.example.universalyoga

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.universalyoga.databinding.ActivityGetStartedBinding

class GetStartedActivity : AppCompatActivity() {
    lateinit var binding: ActivityGetStartedBinding
    lateinit var courseDBHelper: CourseDBHelper
    lateinit var classDBHelper: ClassDBHelper
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGetStartedBinding.inflate(layoutInflater);
        setContentView(binding.root)
        courseDBHelper = CourseDBHelper(this)
        classDBHelper = ClassDBHelper(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BriefFragment(
                { courseDBHelper.getAllCourses() },
                { classDBHelper.getAllClasses() },
                { id -> courseDBHelper.getCourseById(id.toString()) }
            ))
            .addToBackStack(null)
            .commit()

        toggle = ActionBarDrawerToggle(this, binding.main, R.string.open, R.string.close)
        binding.main.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setNavigationItemSelectedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (it.itemId) {
                R.id.nav_add_course -> {
                    if(currentFragment !is AddCourseFragment){
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, AddCourseFragment())
                            .addToBackStack(null)
                            .commit()
                    }


                }

                R.id.nav_search -> {
                    if(currentFragment !is AddCourseFragment){
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, SearchFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }

            }
            binding.main.closeDrawer(binding.navView)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}