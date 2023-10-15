package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.ServiceItem

class rvAdapter(val NotesList: List<ServiceItem>, val context: Context):
RecyclerView.Adapter<rvAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener{
        private val MerName: TextView = itemView.findViewById(R.id.Title)
        private val MerAddress: TextView = itemView.findViewById(R.id.description)
        private val imageView2: ImageView = itemView.findViewById(R.id.image_of_item)

        fun bind(note: ServiceItem){
            MerName.text = note.title
            MerAddress.text = note.description
            imageView2.setImageResource(note.Image)
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            view: View?,
            menuinfo: ContextMenu.ContextMenuInfo?
        ) {
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)

        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(NotesList[position])
        holder.itemView.requestLayout()

    }

    override fun getItemCount(): Int {
        return NotesList.size
    }
}
