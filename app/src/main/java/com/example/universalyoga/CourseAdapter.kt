package com.example.universalyoga

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(
    private val courseList: List<Course>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(course: Course)
    }

    inner class CourseViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tvCourseTitle: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val tvCourseDay: TextView = itemView.findViewById(R.id.tvCourseDay)
        val tvCourseTime: TextView = itemView.findViewById(R.id.tvCourseTime)
        val tvCapacity: TextView = itemView.findViewById(R.id.tvCapacity)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(courseList[position])
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseAdapter.CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_brief_view,parent,false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseAdapter.CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.tvCourseTitle.text = course.type
        holder.tvCourseDay.text = course.day.toString()
        holder.tvCourseTime.text = course.time
        holder.tvCapacity.text = "Capacity: ${course.capacity}"
        holder.tvDuration.text = "Duration: ${course.duration} min"
        holder.tvPrice.text = "Price: £${course.price}"
    }

    override fun getItemCount(): Int {
        return courseList.size;
    }
}