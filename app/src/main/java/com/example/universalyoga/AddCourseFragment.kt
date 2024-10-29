package com.example.universalyoga

import CourseConfirmDialog
import android.app.Activity
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


class AddCourseFragment : Fragment(R.layout.course_form){

    lateinit var courseDBHelper: CourseDBHelper
    lateinit var binding: CourseFormBinding
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CourseFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        courseDBHelper = CourseDBHelper(requireContext())
        // Set up the AutoCompleteTextView
        val types = mutableListOf(R.array.types)
        val spinnerAdapter = ArrayAdapter(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, types)
        binding.autoCompleteTypeCourse.setAdapter(spinnerAdapter)

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



//        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            if (uri != null) {
//                selectedImageUri = uri
//                binding.ivCourse.setImageURI(uri)  // Show the selected image
//
//                // Save the image to internal storage and update the Course imageUrl
//                val savedImagePath = saveImageToAppStorage(uri)
//                if (savedImagePath != null) {
//                    // Update the imageUrl field in your Course object
//                    course.imageUrl = savedImagePath
//                }
//            }
//        }

        // Set up the button click listener
        binding.btnAdd.setOnClickListener {
            // Show the confirmation dialog before adding the course
            val course = validateCourseForm()

            if (course != null) {
                val dialog = CourseConfirmDialog.newInstance(
                    onConfirm = {
                        val result = courseDBHelper.createCourse(course)
                        if (result != -1L) {
                            Toast.makeText(requireContext(), "Course added successfully!", Toast.LENGTH_SHORT).show()
                            clearForm() // Clear the form after successful addition
                        } else {
                            Toast.makeText(requireContext(), "Failed to add course", Toast.LENGTH_SHORT).show()
                        }
                    },
                    course = course // Pass the course object to display in the dialog
                )
                dialog.show(parentFragmentManager, CourseConfirmDialog.TAG)
            } else {
                Toast.makeText(requireContext(), "Please correct the errors in the form", Toast.LENGTH_SHORT).show()
            }
        }

    }



    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }



    private fun validateCourseForm(): Course? {
        // Extract views from the form
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

        val timePattern = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$")
        if (etTime.text.isNullOrEmpty()){
            etTime.error = "Time is required"
            isValid = false
        } else  if (!timePattern.matches(etTime.text)){
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

        // If any validation fails, return null
        if (!isValid) return null

        // Get the day from the spinner (assuming it has day values set in it)
        val selectedDay = DayOfWeek.valueOf(spDate.selectedItem.toString().uppercase())

        // Get the values from the fields
        val courseType = courseTypeAutoComplete.text.toString()
        val price = etPrice.text.toString().toDouble()
        val time = etTime.text.toString()
        val capacity = etCapacity.text.toString().toInt()
        val duration = etDuration.text.toString().toInt()
        val description = etDescription.text.toString()
        val imageUrl = selectedImageUri?.toString() ?: ""

        // Return a new Course object with validated inputs
        return Course(
            id = "", // UUID will be generated in `createCourse`
            day = selectedDay,
            time = time, // Assume a fixed time for now (adjust as necessary)
            capacity = capacity,
            duration = duration,
            price = price,
            type = courseType,
            description = description,
            imageUrl = imageUrl, // Handle image URL logic
            isActive = true, // Assume default value, update based on form if necessary
            createdAt = java.sql.Timestamp(System.currentTimeMillis()),
            updatedAt = java.sql.Timestamp(System.currentTimeMillis()),
            synced = 0
        )
    }


    private fun clearForm() {
        // Directly clear text in each EditText and AutoCompleteTextView
        binding.autoCompleteTypeCourse.text.clear()    // AutoCompleteTextView
        binding.etPrice.text.clear()                   // EditText for Price
        binding.autoCompleteTimeCourse.text.clear()    // EditText for Time
        binding.etCapacity.text.clear()                // EditText for Capacity
        binding.etDuration.text?.clear()                // EditText for Duration
        binding.etDescription.text.clear()

        binding.ivCourse.setImageResource(R.drawable.kids_yoga)
        selectedImageUri = null

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