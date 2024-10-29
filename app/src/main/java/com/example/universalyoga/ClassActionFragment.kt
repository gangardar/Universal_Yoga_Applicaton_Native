package com.example.universalyoga

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.universalyoga.databinding.FragmentAddClassBinding
import java.sql.Timestamp
import java.util.Locale

class ClassActionFragment(
    private val clazz : Class,
) : Fragment(R.layout.fragment_add_class) {
    private var _binding: FragmentAddClassBinding? = null
    private val binding get() = _binding!!

    lateinit var classDBHelper: ClassDBHelper
    lateinit var courseDBHelper: CourseDBHelper

    lateinit var courseRecyclerView: RecyclerView
    lateinit var courseAdapter : ClassCourseRecyclerAdapter
    lateinit var course : Course

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddClassBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        classDBHelper = ClassDBHelper(requireContext())
        courseDBHelper = CourseDBHelper(requireContext())

        populateClassForm();
        showCourseData();
        handleCourseDetailChange();
        handleDateChange();
        handleUpdate();
        handleDelete();


    }

    private fun handleDelete() {
        binding.btnDeleteClass.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this class?")
                .setPositiveButton("Delete") { _, _ ->
                    val deletedRows = classDBHelper.softDelete(clazz)
                    if (deletedRows > 0) {
                        Toast.makeText(requireContext(), "Class deleted successfully", Toast.LENGTH_SHORT).show()
                        // Go back or update the UI
                        requireActivity().supportFragmentManager.popBackStack() // Go back to previous fragment
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete class", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()

        }
    }

    private fun handleUpdate() {
        binding.btnSubmitClassInstance.setOnClickListener{
            val timestamp = binding.etDate.tag
            val teacher = binding.etTeacher.text.toString()
            val comment = binding.etComments.text.toString()

            // Validate required fields
            if (timestamp == 0L || teacher.isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Class instance
            val date = timestamp?.let { java.sql.Timestamp(it as Long) } ?: clazz.date
            val classInstance = com.example.universalyoga.Class(
                id = clazz.id,
                date = date,
                courseId = clazz.courseId,
                teacher = teacher,
                comment = comment,
                createdAt = clazz.createdAt,
                updatedAt = Timestamp(System.currentTimeMillis()),
                synced = 0,
                isDeleted = clazz.isDeleted
            )

            // Insert class instance into database
            val result = classDBHelper.updateClass(classInstance)
            if (result > 0) {
                Toast.makeText(context, "Class updated successfully.", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()  // Go back to the previous screen
            } else {
                Toast.makeText(context, "Error updating class. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleDateChange() {
        binding.etDate.setOnClickListener {
            val targetDayOfWeek = when (course.day.toString()) {
                "Sunday" -> Calendar.SUNDAY
                "Monday" -> Calendar.MONDAY
                "Tuesday" -> Calendar.TUESDAY
                "Wednesday" -> Calendar.WEDNESDAY
                "Thursday" -> Calendar.THURSDAY
                "Friday" -> Calendar.FRIDAY
                "Saturday" -> Calendar.SATURDAY
                else -> Calendar.MONDAY // Default to Monday if no match
            }
            // Initialize a Calendar to get the current date
            val calendar = Calendar.getInstance()

            // Create and show DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Update etDate with the selected date
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.etDate.setText(dateFormat.format(calendar.time))

                    // Convert the selected date to a timestamp (milliseconds since epoch)
                    val timestamp = calendar.timeInMillis
                    binding.etDate.tag = timestamp // Store timestamp in tag for later use
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                if (calendar.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                    // If the selected day is not the target day, disable it by moving to next target day
                    Toast.makeText(context,"Invalid Day Changing to valid day ${course.day}", Toast.LENGTH_SHORT).show()
                    while (calendar.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    datePickerDialog.updateDate(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }
            }
            datePickerDialog.show()
        }
    }

    private fun handleCourseDetailChange() {
        // Store the initial values
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val initialDate = dateFormat.format(clazz.date)
        val initialTeacher = clazz.teacher
        val initialComment = clazz.comment

        // Disable the update button initially
        binding.btnSubmitClassInstance.isEnabled = false

        // Function to check if any field has changed
        fun checkForChanges() {
            val dateChanged = binding.etDate.text.toString() != initialDate
            val teacherChanged = binding.etTeacher.text.toString() != initialTeacher
            val commentChanged = binding.etComments.text.toString() != initialComment

            // Enable the button if any of the fields has changed
            binding.btnSubmitClassInstance.isEnabled = dateChanged || teacherChanged || commentChanged
        }

        // Set TextWatchers on each EditText to listen for changes
        binding.etDate.addTextChangedListener {
            checkForChanges()
        }

        binding.etTeacher.addTextChangedListener {
            checkForChanges()
        }

        binding.etComments.addTextChangedListener {
            checkForChanges()
        }
    }



    private fun populateClassForm() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        binding.tvAddClassTitle.text = "Class Instance"
        binding.etDate.setText(dateFormat.format(clazz.date))
        binding.etTeacher.setText(clazz.teacher)
        binding.etComments.setText(clazz.comment)
        binding.btnSubmitClassInstance.text = "Update Class"
    }

    private fun showCourseData() {
        course = courseDBHelper.getCourseById(clazz.courseId)!!
        courseRecyclerView = binding.addClassCourseList
        courseRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        courseAdapter = ClassCourseRecyclerAdapter(course, this)
        courseRecyclerView.adapter = courseAdapter
    }

    fun onItemClick(course: Course?) {
        val fragment = course?.let { CourseDetailFragment(it) }
        if (fragment != null) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

}