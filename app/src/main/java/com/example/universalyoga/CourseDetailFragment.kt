package com.example.universalyoga

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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.course_detailed_view, container, false)

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
}