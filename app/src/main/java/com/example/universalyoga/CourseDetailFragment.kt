package com.example.universalyoga

import android.app.AlertDialog
import android.content.Intent
import android.inputmethodservice.Keyboard.Row
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment


class CourseDetailFragment(private val course: Course) : Fragment() {
    lateinit var courseDBHelper: CourseDBHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.course_detailed_view, container, false)
        courseDBHelper = CourseDBHelper(requireContext())
        // Bind views
        val ivCourseImage = view.findViewById<ImageView>(R.id.ivCourseImage)
        val tvCourseTitle = view.findViewById<TextView>(R.id.tvCourseTitle)
        val tvCourseDay = view.findViewById<TextView>(R.id.tvCourseDay)
        val tvCourseTime = view.findViewById<TextView>(R.id.tvCourseTime)
        val tvCapacity = view.findViewById<TextView>(R.id.tvCapacity)
        val tvDuration = view.findViewById<TextView>(R.id.tvDuration)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)

        // Set course data to views
        tvCourseTitle.text = course.type
        tvCourseDay.text = course.day.toString()
        tvCourseTime.text = course.time
        tvCapacity.text = "Capacity: ${course.capacity}"
        tvDuration.text = "Duration: ${course.duration} min"
        tvPrice.text = "Price: £${course.price}"
        tvDescription.text = course.description

        // If the course has an image URL or resource, you can load it here
        // Example: Glide or Picasso can be used to load images from URL
//        ivCourseImage.setImageResource(course.imageUrl) // Placeholder
        if(course.imageUrl.isNotEmpty()){
            val imageUri = Uri.parse(course.imageUrl)
            try {
                // Requesting permission again (not persistable) is not ideal.
                requireContext().contentResolver.takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // Load the image into the ImageView
                ivCourseImage.setImageURI(imageUri)
            } catch (e: SecurityException) {
                // Handle the case where permission is not granted
                Toast.makeText(requireContext(), "Permission denied for accessing the image", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
        btnEdit.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(
                    R.id.fragment_container,  // Container where the fragment will be replaced
                    CourseEditFragment(course) { courseDBHelper.updateCourse(course) }  // Pass the course object and updateCourse function
                )
                ?.addToBackStack(null)  // Optional: Add this transaction to the back stack
                ?.commit()
        }

    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course?")
            .setPositiveButton("Delete") { _, _ ->
                val deletedRows = courseDBHelper.deleteCourse(course.id,course.imageUrl)
                if (deletedRows > 0) {
                    Toast.makeText(requireContext(), "Course deleted successfully", Toast.LENGTH_SHORT).show()
                    // Go back or update the UI
                    requireActivity().supportFragmentManager.popBackStack() // Go back to previous fragment
                } else {
                    Toast.makeText(requireContext(), "Failed to delete course", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}