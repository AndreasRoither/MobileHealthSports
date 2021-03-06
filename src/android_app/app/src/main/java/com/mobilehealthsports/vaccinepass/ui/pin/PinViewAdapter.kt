package com.mobilehealthsports.vaccinepass.ui.pin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilehealthsports.vaccinepass.R
import com.mobilehealthsports.vaccinepass.databinding.ListItemCircleBinding

/**
 * Custom adapter for pin view in PinActivity
 */
class PinViewAdapter(var pins: List<Boolean>) : RecyclerView.Adapter<PinViewAdapter.ViewHolder>() {

    // This method creates views for the RecyclerView by inflating the layout
    // Into the viewHolders which helps to display the items in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        // Inflate the layout view you have created for the list rows here
        val binding: ListItemCircleBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_circle, parent, false)
        return ViewHolder(binding)
    }


    // This is your ViewHolder class that helps to populate data to the view
    inner class ViewHolder(private val binding: ListItemCircleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(active: Boolean) {
            binding.pinActive = active
        }
    }

    override fun getItemCount(): Int {
        return pins.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pin = pins[position]
        holder.bind(pin)
    }
}