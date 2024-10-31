package com.example.universalyoga

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class SearchClassAdapter(
    private val classes: List<Class>,
    private val getCourseById: (String) -> Course?,
    private val listener: courseViewClickListener // Expecting SearchFragment to implement this
) : RecyclerView.Adapter<SearchClassAdapter.ClassViewHolder>() {

    interface courseViewClickListener {
        fun onClassItemClick(clazz: Class)
    }

    inner class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClassTitle: TextView = itemView.findViewById(R.id.tvClassTitle)
        val tvClassDescription: TextView = itemView.findViewById(R.id.tvClassDescription)
        val tvClassDay: TextView = itemView.findViewById(R.id.tvClassDay)
        val tvTeacher: TextView = itemView.findViewById(R.id.tvTeacher)
        val tvPrice: TextView = itemView.findViewById(R.id.tvClassPrice)
        val tvClassCapacity: TextView = itemView.findViewById(R.id.tvClassCapacity)
        val comment: TextView = itemView.findViewById(R.id.tvComment)

        fun bind(clazz: Class) {
            itemView.setOnClickListener {
                listener.onClassItemClick(clazz)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.class_brief_view, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val classItem = classes[position]
        val courseItem: Course? = getCourseById(classItem.courseId)

        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = dateFormat.format(classItem.date)

        holder.tvClassTitle.text = "${date} By ${classItem.teacher}"
        holder.tvClassDescription.text = "Description: ${courseItem?.description}"
        holder.tvClassDay.text = "Day: ${courseItem?.day}"
        holder.tvClassCapacity.text = "Capacity: ${courseItem?.capacity}"
        holder.tvTeacher.text = "Teacher: ${classItem.teacher}"
        holder.comment.text = "Comment: ${classItem.comment}"
        holder.tvPrice.text = "£ ${courseItem?.price}"

        // Bind the click listener
        holder.bind(classItem)
    }

    override fun getItemCount(): Int = classes.size
}

