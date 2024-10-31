package com.example.universalyoga

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.log


class BriefFragment(private val getCourseList: () -> List<Course>,
                    private val getClass: () -> List<Class>,
                    private val getCourseById: (String) -> Course?
) : Fragment(), CourseAdapter.OnItemClickListener, ClassAdapter.courseViewClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var classRecyclerView: RecyclerView
    private lateinit var classAdapter: ClassAdapter

    private lateinit var tvSyncStatus : TextView

    private lateinit var networkConnectionHelper: NetworkConnectionHelper
    private lateinit var courseDBHelper: CourseDBHelper
    private lateinit var classDBHelper: ClassDBHelper
    private lateinit var firestoreHelper: FirestoreHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.brief_fragment, container, false)
//        Course Recycler Adapter
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        courseAdapter = CourseAdapter(getCourseList, this)
        recyclerView.adapter = courseAdapter
//        Class Recycler Adapter
        classRecyclerView = view.findViewById(R.id.recyclerClassesView)
        classRecyclerView.layoutManager= LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        classAdapter= ClassAdapter(getClass,getCourseById,this)
        classRecyclerView.adapter = classAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Classes for data sync initialized.
        networkConnectionHelper = NetworkConnectionHelper(requireContext())
        courseDBHelper = CourseDBHelper(requireContext())
        classDBHelper = ClassDBHelper(requireContext())
        firestoreHelper = FirestoreHelper()

        //Handle data Sync
        networkConnectionHelper.observe(requireActivity()) { isConnected ->
            if (isConnected) {
                showToast("Syncing....")
                handleDataSyncToFirestore()
                handleDataSyncFromFirestore()
            } else {
                showAlertDialog("Network Issue", "No internet connection! Sync will happen automatically once device is online")

            }
        }

        tvSyncStatus = view.findViewById(R.id.tvSyncStatus)
    }

    private fun handleSetSyncStatus(isTrue : Boolean) {

        if(isTrue){
            tvSyncStatus.setText("Synced")
        }else{
            tvSyncStatus.setText("Not Synced")
        }
    }

    private fun handleDataSyncFromFirestore() {
        firestoreHelper.fetchCoursesFromFirebase { courses ->
            if (courses.isNotEmpty()) {
                for (course in courses) {
                    courseDBHelper.insertOrUpdateCourse(course)
                }
                courseAdapter.notifyDataSetChanged()
            }
        }
        firestoreHelper.fetchClassesFromFirebase { classes ->
            if (classes.isNotEmpty()) {
                for (clazz in classes) {
                    classDBHelper.insertOrUpdateClasses(clazz)
                }
                classAdapter.notifyDataSetChanged()
            }
        }
        handleSetSyncStatus(true)

    }

    private fun handleDataSyncToFirestore() {
        val courses = courseDBHelper.getUnsyncedCourse()
        val classes = classDBHelper.getUnsyncedClass()

        if(courses.isEmpty() && classes.isEmpty()){
            handleSetSyncStatus(true)
            showToast("Synced Already!")
            return
        }
        if(courses.isNotEmpty()){
            for (course in courses) {
                firestoreHelper.syncCourseToFirebase(course, courseDBHelper)
            }
        }
        if(classes.isNotEmpty()){
            for (clazz in classes) {
                firestoreHelper.syncClassToFirebase(clazz, classDBHelper)
            }
        }
        showToast("Synced Successfully!")
        handleSetSyncStatus(true)

    }

    override fun onItemClick(course: Course) {
        val fragment = CourseDetailFragment(course)
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onAssignToClassClick(course: Course) {
        val fragment = AddClassFragment(course)
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onClassItemClick(clazz: Class) {
        val fragment = ClassActionFragment(clazz)
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Got it"){ dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }


}