package com.uyscuti.social.circuit.User_Interface.uploads.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityDocsBinding
import java.io.File

class Docs : AppCompatActivity() {
    private lateinit var binding: ActivityDocsBinding
    lateinit var rs: Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listDocumentFiles()
    }


    //@RequiresApi(Build.VERSION_CODES.R)
    private fun listDocumentFiles() {
        val docCols = listOf(MediaStore.Files.FileColumns.DATA).toTypedArray()
        val mimeTypes = arrayOf(
            "application/pdf",
            "application/msword",
            "application/ms-doc",
            "application/doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        )
        //val mimeTypes = arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?, ?, ?, ?)"
        rs = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            docCols, selection, mimeTypes, null
        )!!
//        rs.moveToFirst()

        binding.documentGridView.adapter = DocumentAdapter(applicationContext)
        (binding.documentGridView.adapter as DocumentAdapter).notifyDataSetChanged()

        binding.documentGridView.setOnItemClickListener { _, _, i, _ ->
            rs.moveToPosition(i)
            val path = rs.getString(0)
            // Handle document file actions as needed
            // For example, you can open a PDF viewer or a DOCX reader activity.
            // You can pass the document path to the viewer/reader activity.
        }
    }

    inner class DocumentAdapter(context: Context) : BaseAdapter() {
        private val context: Context = context

        override fun getCount(): Int {
            return rs.count
        }

        override fun getItem(p0: Int): Any {
            return p0
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        @SuppressLint("MissingInflatedId")
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            // Inflate the custom document item layout
            val itemView = LayoutInflater.from(context).inflate(R.layout.audio_item_layout, null)

            rs.moveToPosition(p0)
            val path = rs.getString(0)

            Log.d("DocumentAdapter", "Document path: $path")

            // Set the document file name to the TextView
            val documentNameTextView = itemView.findViewById<TextView>(R.id.documentNameTextView)
            val documentImageView = itemView.findViewById<ImageView>(R.id.documentIconImageView)

            documentNameTextView.text = getDocumentFileName(path) // Set the actual document file name here
            val fileExtension = getFileExtension(path)
//            if(fileExtension == "pdf") {
//                documentImageView.setImageResource(R.drawable.pdf)
//            }else {
//                documentImageView.setImageResource(R.drawable.doc)
//            }
            // Handle document file actions as needed
            // For example, you can set an OnClickListener to open the document.
            itemView.setOnClickListener {
                // Handle document action here.
                val resultIntent = Intent()
                resultIntent.putExtra("docPath", path)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

            return itemView
        }

        private fun getDocumentFileName(path: String): String {
            // Extract and return the file name from the path
            val file = File(path)
            return file.name
        }
        private fun getFileExtension(url: String): String {
            val parts = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val fileName = parts[parts.size - 1]
            val fileNameParts = fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return if (fileNameParts.isNotEmpty()) fileNameParts[fileNameParts.size - 1] else ""
        }

    }
}