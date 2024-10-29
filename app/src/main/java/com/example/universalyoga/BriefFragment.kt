package com.example.universalyoga

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BriefFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BriefFragment(private val getCourseList: () -> List<Course>,
                    private val getClass: () -> List<Class>,
                    private val getCourseById: (String) -> Course?
) : Fragment(), CourseAdapter.OnItemClickListener, ClassAdapter.courseViewClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var classRecyclerView: RecyclerView
    private lateinit var classAdapter: ClassAdapter

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
                showToast("Connected to Internet")
                handleDataSync()
            } else {
                showAlertDialog("Network Issue", "No internet connection! Sync will happen automatically once device is online")

            }
        }
    }

    private fun handleDataSync() {
        val courses = courseDBHelper.getUnsyncedCourse()
        val classes = classDBHelper.getUnsyncedClass()

        if(courses.isEmpty() && classes.isEmpty()){
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