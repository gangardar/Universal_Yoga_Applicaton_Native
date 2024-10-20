import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.universalyoga.Course
import com.example.universalyoga.R

class CourseConfirmDialog(
    private val onConfirm: () -> Unit,
    private val course: Course // Pass the Course object for display
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.course_detailed_view, null)

        // Find and assign the views
        val courseImage: ImageView = dialogView.findViewById(R.id.ivCourseImage)
        val courseTitle: TextView = dialogView.findViewById(R.id.tvCourseTitle)
        val courseDay: TextView = dialogView.findViewById(R.id.tvCourseDay)
        val courseTime: TextView = dialogView.findViewById(R.id.tvCourseTime)
        val courseCapacity: TextView = dialogView.findViewById(R.id.tvCapacity)
        val courseDuration: TextView = dialogView.findViewById(R.id.tvDuration)
        val coursePrice: TextView = dialogView.findViewById(R.id.tvPrice)
        val courseDescription: TextView = dialogView.findViewById(R.id.tvDescription)

        // Assign the course data to the views
        courseTitle.text = course.type
        courseDay.text = course.day.toString()
        courseTime.text = course.time
        courseCapacity.text = "Capacity: ${course.capacity}"
        courseDuration.text = "Duration: ${course.duration} min"
        coursePrice.text = "Price: £${course.price}"
        courseDescription.text = course.description

        // If you are dealing with image URI from the course object
        val imageUri = Uri.parse(course.imageUrl)
        courseImage.setImageURI(imageUri)

        val buttonLayout = dialogView.findViewById<LinearLayout>(R.id.llBtn)
        buttonLayout.visibility = View.GONE

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                // Invoke the onConfirm callback
                onConfirm()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "CourseConfirmationDialog"

        fun newInstance(onConfirm: () -> Unit, course: Course): CourseConfirmDialog {
            return CourseConfirmDialog(onConfirm, course)
        }
    }
}

