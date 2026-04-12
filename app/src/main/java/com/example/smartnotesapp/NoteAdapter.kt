package com.example.smartnotesapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu

class NoteAdapter(
    val context: Context,
    val list: ArrayList<NoteModel>,
    val db: DatabaseHelper,
    val refresh: () -> Unit
) : BaseAdapter() {

    override fun getCount() = list.size
    override fun getItem(position: Int) = list[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false)

        val title = view.findViewById<TextView>(R.id.titleText)
        val desc = view.findViewById<TextView>(R.id.descText)
        val image = view.findViewById<ImageView>(R.id.noteImage)
        val favBtn = view.findViewById<ImageView>(R.id.favBtn)
        val shareBtn = view.findViewById<ImageView>(R.id.shareBtn)

        val note = list[position]

        title.text = note.title

        // ✅ IMAGE NOTE
        if (note.title == "Image Note") {
            desc.visibility = View.GONE
            image.visibility = View.VISIBLE

            try {
                val bitmap = base64ToBitmap(note.description)
                image.setImageBitmap(bitmap)
            } catch (e: Exception) {
                image.visibility = View.GONE
            }

        } else {
            desc.visibility = View.VISIBLE
            image.visibility = View.GONE
            desc.text = note.description
        }

        // ⭐ SHOW FAVORITE STATUS (VERY IMPORTANT)
        if (note.isFavorite == 1) {
            favBtn.setImageResource(android.R.drawable.btn_star_big_on)
            favBtn.setColorFilter(android.graphics.Color.parseColor("#FFD700")) // GOLD
        } else {
            favBtn.setImageResource(android.R.drawable.btn_star_big_off)
            favBtn.setColorFilter(android.graphics.Color.GRAY)
        }

        // ⭐ FAVORITE CLICK (DIRECT BUTTON)
        favBtn.setOnClickListener {

            if (note.isFavorite == 1) {
                note.isFavorite = 0
                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            } else {
                note.isFavorite = 1
                Toast.makeText(context, "Added to Favorites ⭐", Toast.LENGTH_SHORT).show()
            }

            db.toggleFavorite(note.id, note.isFavorite)
            refresh()
        }

        // 🔗 SHARE BUTTON
        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "${note.title}\n\n${note.description}"
            )
            context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
        }

        // ⭐ POPUP MENU (KEEPING YOUR ORIGINAL FEATURES)
        view.setOnClickListener {

            val popup = PopupMenu(context, view)

            popup.menu.add("Edit")
            popup.menu.add("Delete")
            popup.menu.add("Share")
            popup.menu.add("Favorite ⭐")

            popup.setOnMenuItemClickListener {

                when (it.title) {

                    "Edit" -> {
                        val intent = Intent(context, AddNoteActivity::class.java)
                        intent.putExtra("id", note.id)
                        intent.putExtra("title", note.title)
                        intent.putExtra("desc", note.description)
                        context.startActivity(intent)
                    }

                    "Delete" -> {
                        showDeleteDialog(note)
                    }

                    "Share" -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "${note.title}\n\n${note.description}"
                        )
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share Note")
                        )
                    }

                    "Favorite ⭐" -> {

                        if (note.isFavorite == 1) {
                            note.isFavorite = 0
                            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            note.isFavorite = 1
                            Toast.makeText(context, "Added to Favorites ⭐", Toast.LENGTH_SHORT).show()
                        }

                        db.toggleFavorite(note.id, note.isFavorite)
                        refresh()
                    }
                }
                true
            }

            popup.show()
        }

        return view
    }

    // 🗑 DELETE (RECYCLE BIN)
    private fun showDeleteDialog(note: NoteModel) {

        val builder = AlertDialog.Builder(context)

        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")

        builder.setPositiveButton("Yes") { dialog, _ ->

            db.moveToTrash(note.id)

            Toast.makeText(context, "Moved to Recycle Bin", Toast.LENGTH_SHORT).show()

            refresh()
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // 📷 BASE64 → BITMAP
    private fun base64ToBitmap(base64Str: String): android.graphics.Bitmap {
        val decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}