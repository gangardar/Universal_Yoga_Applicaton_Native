package com.example.universalyoga

import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(
    private val getCourseList: () -> List<Course>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(course: Course)
        fun onAssignToClassClick(course: Course)
    }

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCourseTitle: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val tvCourseDay: TextView = itemView.findViewById(R.id.tvCourseDay)
        val tvCourseTime: TextView = itemView.findViewById(R.id.tvCourseTime)
        val tvCapacity: TextView = itemView.findViewById(R.id.tvCapacity)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val assignToClass: TextView = itemView.findViewById(R.id.tvAssignClass)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && getCourseList().isNotEmpty()) {
                    listener.onItemClick(getCourseList()[position])
                }
            }

            assignToClass.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && getCourseList().isNotEmpty()) {
                    listener.onAssignToClassClick(getCourseList()[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseAdapter.CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course_brief_view, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseAdapter.CourseViewHolder, position: Int) {
        val courseList = getCourseList()

        if (courseList.isEmpty()) {
            // Show the "Add New Course" message in place of regular content
            holder.tvCourseTitle.text = "Add New Course (Sidebar -> Add Course)"
            holder.tvCourseTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))

            // Hide other views
            holder.tvCourseDay.visibility = View.GONE
            holder.tvCourseTime.visibility = View.GONE
            holder.tvCapacity.visibility = View.GONE
            holder.tvDuration.visibility = View.GONE
            holder.tvPrice.visibility = View.GONE
            holder.assignToClass.visibility = View.GONE

            // Set background to transparent
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }else {
            // Regular binding when courses are available
            val course = courseList[position]
            holder.tvCourseTitle.text = course.type
            holder.tvCourseDay.text = course.day.toString()
            holder.tvCourseTime.text = course.time
            holder.tvCapacity.text = "Capacity: ${course.capacity}"
            holder.tvDuration.text = "Duration: ${course.duration} min"
            holder.tvPrice.text = "Price: £${course.price}"
            holder.assignToClass.setText(Html.fromHtml("<u>Assign to new Class -></u>"))
            holder.tvCourseDay.visibility = View.VISIBLE
            holder.tvCourseTime.visibility = View.VISIBLE
            holder.tvCapacity.visibility = View.VISIBLE
            holder.tvDuration.visibility = View.VISIBLE
            holder.tvPrice.visibility = View.VISIBLE
            holder.assignToClass.visibility = View.VISIBLE
            holder.itemView.setBackgroundResource(R.drawable.card_background)
        }
    }

    override fun getItemCount(): Int {
        // If course list is empty, return 1 to show the "Add New Course" message
        return if (getCourseList().isEmpty()) 1 else getCourseList().size
    }
}
