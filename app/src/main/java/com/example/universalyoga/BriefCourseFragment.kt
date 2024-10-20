package com.example.universalyoga

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BriefCourseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BriefCourseFragment(private val courses: List<Course>) : Fragment(), CourseAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.brief_course_fragment, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)

        courseAdapter = CourseAdapter(courses, this)
        recyclerView.adapter = courseAdapter

        return view
    }

    override fun onItemClick(course: Course) {
        val fragment = CourseDetailFragment(course)
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }
}