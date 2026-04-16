package com.example.smartnotesapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.BitmapFactory

class RecycleAdapter(
    val context: Context,
    val list: ArrayList<NoteModel>
) : BaseAdapter() {

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_recycle_note, parent, false)

        val title = view.findViewById<TextView>(R.id.titleText)
        val desc = view.findViewById<TextView>(R.id.descText)
        val image = view.findViewById<ImageView>(R.id.noteImage)

        val note = list[position]

        title.text = note.title

        // ✅ FIXED LOGIC FOR IMAGE
        if (note.title == "Image Note" && note.description.isNotEmpty()) {

            desc.visibility = View.GONE
            image.visibility = View.VISIBLE

            try {
                val bytes = android.util.Base64.decode(note.description, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                image.setImageBitmap(bitmap)
            } catch (e: Exception) {
                image.visibility = View.GONE
            }

        } else {
            desc.visibility = View.VISIBLE
            image.visibility = View.GONE
            desc.text = note.description
        }

        return view
    }
}