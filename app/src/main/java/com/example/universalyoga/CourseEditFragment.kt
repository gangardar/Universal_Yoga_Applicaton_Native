package com.example.universalyoga

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.universalyoga.databinding.CourseFormBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.DayOfWeek

class CourseEditFragment(
    private val course: Course,
    private val updateCourse: (Course) -> Int
) : Fragment(R.layout.course_form) {

    lateinit var binding: CourseFormBinding
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CourseFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the AutoCompleteTextView
        val types = resources.getStringArray(R.array.types).toMutableList()
        val spinnerAdapter = ArrayAdapter(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, types)
        binding.autoCompleteTypeCourse.setAdapter(spinnerAdapter)

        // Prepopulate the form with the existing course data
        binding.autoCompleteTypeCourse.setText(course.type, false)
        binding.etPrice.setText(course.price.toString())
        binding.autoCompleteTimeCourse.setText(course.time)
        binding.etCapacity.setText(course.capacity.toString())
        binding.etDuration.setText(course.duration.toString())
        binding.etDescription.setText(course.description)
        binding.spDate.setSelection(course.day.ordinal)

        if (course.imageUrl.isNotEmpty()) {
            selectedImageUri = Uri.parse(course.imageUrl)
            binding.ivCourse.setImageURI(selectedImageUri)
        }

        binding.fabAddImg.setOnClickListener {
            pickImageFromGallery()
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Request persistable permission for the content URI
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // Save the image to app storage and get the URI of the saved file
                val savedImagePath = saveImageToAppStorage(uri)
                if (savedImagePath != null) {
                    selectedImageUri = Uri.parse(savedImagePath)  // Convert the saved image path to a URI
                    binding.ivCourse.setImageURI(selectedImageUri)  // Display the saved image
                } else {
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up the button click listener for updating the course
        binding.btnAdd.text = "Update Course"
        binding.btnAdd.setOnClickListener {
            val updatedCourse = validateCourseForm()
            if (updatedCourse != null) {
                val result = updateCourse(updatedCourse)
                if (result > 0) {
                    Toast.makeText(requireContext(), "Course updated successfully!", Toast.LENGTH_SHORT).show()
                    AlertDialog.Builder(requireContext())
                        .setTitle("Updated")
                        .setMessage("What to go back?")
                        .setPositiveButton("Go Back") { _, _ ->
                            requireActivity().supportFragmentManager.popBackStack() // Go back to previous fragment
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update course", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please correct the errors in the form", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun validateCourseForm(): Course? {
        val courseTypeAutoComplete = binding.autoCompleteTypeCourse
        val etPrice = binding.etPrice
        val etTime = binding.autoCompleteTimeCourse
        val etCapacity = binding.etCapacity
        val etDuration = binding.etDuration
        val etDescription = binding.etDescription
        val spDate = binding.spDate

        var isValid = true

        // Validate Course Type
        if (courseTypeAutoComplete.text.isNullOrEmpty()) {
            courseTypeAutoComplete.error = "Course type is required"
            isValid = false
        }

        // Validate Price
        if (etPrice.text.isNullOrEmpty()) {
            etPrice.error = "Price is required"
            isValid = false
        } else if (etPrice.text.toString().toDoubleOrNull() == null) {
            etPrice.error = "Enter a valid price"
            isValid = false
        }

        // Validate Time
        val timePattern = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$")
        if (etTime.text.isNullOrEmpty()) {
            etTime.error = "Time is required"
            isValid = false
        } else if (!timePattern.matches(etTime.text)) {
            etTime.error = "Enter time in (HH:MM) format."
            isValid = false
        }

        // Validate Capacity
        if (etCapacity.text.isNullOrEmpty()) {
            etCapacity.error = "Capacity is required"
            isValid = false
        } else if (etCapacity.text.toString().toIntOrNull() == null) {
            etCapacity.error = "Enter a valid capacity"
            isValid = false
        }

        // Validate Duration
        if (etDuration.text.isNullOrEmpty()) {
            etDuration.error = "Duration is required"
            isValid = false
        } else if (etDuration.text.toString().toIntOrNull() == null) {
            etDuration.error = "Enter a valid duration"
            isValid = false
        }

        // If validation fails, return null
        if (!isValid) return null

        // Get the selected day from the spinner
        val selectedDay = DayOfWeek.valueOf(spDate.selectedItem.toString().uppercase())

        // Create the updated Course object
        val courseType = courseTypeAutoComplete.text.toString()
        val price = etPrice.text.toString().toDouble()
        val time = etTime.text.toString()
        val capacity = etCapacity.text.toString().toInt()
        val duration = etDuration.text.toString().toInt()
        val description = etDescription.text.toString()
        val imageUrl = selectedImageUri?.toString() ?: ""

        return course.copy(
            day = selectedDay,
            time = time,
            capacity = capacity,
            duration = duration,
            price = price,
            type = courseType,
            description = description,
            imageUrl = imageUrl,
            updatedAt = java.sql.Timestamp(System.currentTimeMillis())
        )
    }

    private fun saveImageToAppStorage(uri: Uri): String? {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val outputDir = File(requireContext().filesDir, "course_images")

        if (!outputDir.exists()) {
            outputDir.mkdir() // Create directory if it doesn't exist
        }

        val outputFile = File(outputDir, fileName)

        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(outputFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            return outputFile.absolutePath // Return the path to the saved image
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}
