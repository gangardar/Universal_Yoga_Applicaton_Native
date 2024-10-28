package com.example.universalyoga

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.brief_course_fragment, container, false)
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


}