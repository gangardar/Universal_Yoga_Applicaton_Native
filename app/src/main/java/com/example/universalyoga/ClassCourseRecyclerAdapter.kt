package com.example.universalyoga

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassCourseRecyclerAdapter(
    private val course: Course?,
    private val listener: ClassActionFragment
) : RecyclerView.Adapter<ClassCourseRecyclerAdapter.CourseViewHolder>() {

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
        val assignToClass : TextView = itemView.findViewById(R.id.tvAssignClass)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(course)
                }
            }
        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClassCourseRecyclerAdapter.CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_brief_view,parent,false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassCourseRecyclerAdapter.CourseViewHolder, position: Int) {
        holder.tvCourseTitle.text = course?.type ?: ""
        holder.tvCourseDay.text = course?.day.toString()
        holder.tvCourseTime.text = course?.time ?: "10:00"
        holder.tvCapacity.text = "Capacity: ${course?.capacity}"
        holder.tvDuration.text = "Duration: ${course?.duration} min"
        holder.tvPrice.text = "Price: £${course?.price}"
        holder.assignToClass.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return 1;
    }
}