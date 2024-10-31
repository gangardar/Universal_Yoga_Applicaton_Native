package com.example.universalyoga

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.universalyoga.databinding.FragmentSearchBinding
import java.sql.Timestamp
import java.util.Locale

class SearchFragment : Fragment(),SearchClassAdapter.courseViewClickListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var courseDBHelper: CourseDBHelper
    private lateinit var classDBHelper: ClassDBHelper

    private lateinit var searchClassAdapter: SearchClassAdapter
    private lateinit var classRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideAdvancedSearch()
        setupSpinner()

        courseDBHelper = CourseDBHelper(requireContext())
        classDBHelper = ClassDBHelper(requireContext())

        binding.imBtnAdvancedSearch.setOnClickListener {
            advancedSearchToggle()
        }
        binding.imBtnSearchTeacher.setOnClickListener {
            handleSearch()
        }

        binding.btnSearchAdvanced.setOnClickListener {
            handleSearch()
        }

        binding.editTextDate.setOnClickListener {
            // Initialize a Calendar to get the current date
            initializeCalendar()
        }
    }

    private fun initializeCalendar() {
        val calendar = Calendar.getInstance()

        // Create and show DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Update etDate with the selected date
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.editTextDate.setText(dateFormat.format(calendar.time))

                // Convert the selected date to a timestamp (milliseconds since epoch)
                val timestamp = calendar.timeInMillis
                binding.editTextDate.tag = timestamp // Store timestamp in tag for later use
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun handleSearch() {
        // Retrieve teacher and date info
        val teacher = binding.editTextTeacher.text.toString()
        val date = binding.editTextDate.tag

        // Check selectedDay and perform search logic
        val selectedDay = binding.spDaysOfWeek.selectedItem?.toString() ?: ""
        var day: Int? = null

        if (selectedDay.isNotEmpty()) {
            day = getDayOfWeekFromString(selectedDay)
        }

        Log.d("TAG", "Selected Day: $day")
        Log.d("TAG", "Selected Day String: $selectedDay")

        // Perform the search
        val classes: List<Class> = classDBHelper.searchClasses(
            date?.let { Timestamp(it as Long).toString() },
            teacher,
            day
        )

        // Update RecyclerView
        classRecyclerView = binding.recyclerViewResults
        classRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        searchClassAdapter = SearchClassAdapter(classes, { id -> courseDBHelper.getCourseById(id) }, this)
        classRecyclerView.adapter = searchClassAdapter

    }


    private fun hideAdvancedSearch() {
        binding.llAdvancedSearch.visibility = View.GONE

    }

    private fun advancedSearchToggle() {
        if (binding.llAdvancedSearch.isVisible) {
            binding.llAdvancedSearch.visibility = View.GONE
            binding.imBtnSearchTeacher.visibility = View.VISIBLE
            clearSearchForm()
        } else {
            binding.llAdvancedSearch.visibility = View.VISIBLE
            binding.imBtnSearchTeacher.visibility = View.GONE
        }
    }

    private fun clearSearchForm() {
        binding.editTextDate.text.clear()
        binding.spDaysOfWeek.setSelection(0)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClassItemClick(clazz: Class) {
        val fragment = ClassActionFragment(clazz)
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun setupSpinner() {
        // Create an array of days of the week
        val daysOfWeek = arrayOf("Select a day", "Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        // Get the Spinner reference
        val spinner: Spinner = binding.spDaysOfWeek

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun getDayOfWeekFromString(day: String): Int {
        return when (day) {
            "Sunday" -> 0
            "Monday" -> 1
            "Tuesday" -> 2
            "Wednesday" -> 3
            "Thursday" -> 4
            "Friday" -> 5
            "Saturday" -> 6
            else -> -1 // Invalid day
        }
    }

}