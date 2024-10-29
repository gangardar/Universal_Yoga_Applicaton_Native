package com.example.universalyoga

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.universalyoga.Class
import com.example.universalyoga.databinding.FragmentAddClassBinding
import java.sql.Timestamp
import java.util.Date
import java.util.Locale


class AddClassFragment(
    private val course: Course,
) : Fragment(R.layout.fragment_add_class) {
    private var _binding: FragmentAddClassBinding? = null
    private val binding get() = _binding!!

    lateinit var classDBHelper: ClassDBHelper

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

        binding.llClassCourse.visibility = View.GONE
        binding.btnDeleteClass.visibility = View.GONE

        binding.etDate.setOnClickListener {
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
                    Toast.makeText(context,"Invalid Day Changing to valid day ${course.day}",Toast.LENGTH_SHORT).show()
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

        binding.btnSubmitClassInstance.setOnClickListener {
            val timestamp  = binding.etDate.tag
            val teacher = binding.etTeacher.text.toString()
            val comment = binding.etComments.text.toString()

            // Validate required fields
            if (timestamp == 0L || teacher.isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Class instance
            val classInstance = com.example.universalyoga.Class(
                id = "",  // UUID will be generated in `createClass`
                date = Timestamp(timestamp as Long),
                courseId = course.id,
                teacher = teacher,
                comment = comment,
                createdAt = Timestamp(System.currentTimeMillis()),
                updatedAt = Timestamp(System.currentTimeMillis()),
                synced = 0
            )

            // Insert class instance into database
            val result = classDBHelper.createClass(classInstance)
            if (result > 0) {
                Toast.makeText(context, "Class added successfully.", Toast.LENGTH_SHORT).show()
                activity?.onBackPressed() // Go back to the previous screen
            } else {
                Toast.makeText(context, "Error adding class. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}