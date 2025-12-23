package com.uyscuti.social.circuit.User_Interface.uploads

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityDocumentsBinding
import com.uyscuti.social.circuit.User_Interface.uploads.adapter.AudioDocumentAdapter

class DocumentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocumentsBinding
    private val REQUEST_PERMISSIONS_CODE = 142
    private lateinit var rs: Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24_black)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS_CODE
            )
        } else {
            loadAndDisplayDocuments()
            listDocumentFiles()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAndDisplayDocuments()
            } else {
                // Handle permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadAndDisplayDocuments() {
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED
            )
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_NONE} AND ${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?, ?, ?, ?)"

//        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT} AND ${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?, ?, ?, ?)"

            val mimeTypes = arrayOf(
                "application/pdf",
                "application/msword",
                "application/ms-doc",
                "application/doc",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"
            )

            val cursor = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                mimeTypes,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )

            Log.d("Documents", "Cursor count: ${cursor?.count}")

            cursor?.use {
                if (it.moveToFirst()) {
                    val documentList = mutableListOf<String>()
                    val dataIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                    do {
                        val documentPath = it.getString(dataIndex)
                        if (!documentPath.isNullOrBlank()) {
                            documentList.add(documentPath)
                        }
                    } while (it.moveToNext())

                    Log.d("Documents", "Found ${documentList.size} documents")

                    val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                    val layoutManager = LinearLayoutManager(this)
                    recyclerView.layoutManager = layoutManager
                    val adapter = AudioDocumentAdapter(this, documentList) { docUrl ->
                        // Pass the image URL back to the UserActivity
                        val resultIntent = Intent()
                        resultIntent.putExtra("doc_url", docUrl)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                    recyclerView.adapter = adapter
                } else {
                    // Handle the case where there are no documents
                    // You can show a message or take appropriate action
                    Toast.makeText(this, "No documents found", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Documents", "Error: $e")
            Log.e("Documents", "Error: ${e.message}")
            e.printStackTrace()
        }
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

        Log.d("Documents", "documents List found: ${rs.count}")


//        binding.documentGridView.adapter = DocumentAdapter(applicationContext)
//        (binding.documentGridView.adapter as DocumentAdapter).notifyDataSetChanged()
//
//        binding.documentGridView.setOnItemClickListener { _, _, i, _ ->
//            rs.moveToPosition(i)
//            val path = rs.getString(0)
//            // Handle document file actions as needed
//            // For example, you can open a PDF viewer or a DOCX reader activity.
//            // You can pass the document path to the viewer/reader activity.
//        }
    }

}