package campus.tech.kakao.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.contacts.database.Contact
import campus.tech.kakao.contacts.viewmodel.ContactViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContactListActivity : AppCompatActivity() {

    private val contactViewModel: ContactViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        handlePermissionResult(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        initializeUI()
        requestContactsPermission()
    }

    private fun initializeUI() {
        setupRecyclerView()
        setupAddContactButton()
        showEmptyMessageIfNeeded()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAddContactButton() {
        findViewById<FloatingActionButton>(R.id.AddContactButton).setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showEmptyMessageIfNeeded() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyMessage = findViewById<TextView>(R.id.emptyMessage)

        if (contactViewModel.allContacts.value.isNullOrEmpty()) {
            recyclerView.visibility = RecyclerView.GONE
            emptyMessage.visibility = TextView.VISIBLE
        } else {
            recyclerView.visibility = RecyclerView.VISIBLE
            emptyMessage.visibility = TextView.GONE
        }
    }

    private fun requestContactsPermission() {
        if (isContactsPermissionGranted()) {
            observeContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun isContactsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            observeContacts()
        } else {
            showPermissionDeniedMessage()
        }
    }

    private fun observeContacts() {
        val inflater = LayoutInflater.from(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyMessage = findViewById<TextView>(R.id.emptyMessage)

        contactViewModel.allContacts.observe(this) { contacts ->
            contacts?.let {
                updateRecyclerViewVisibility(recyclerView, emptyMessage, it)
                updateRecyclerViewAdapter(recyclerView, inflater, it)
            }
        }
    }

    private fun updateRecyclerViewVisibility(
        recyclerView: RecyclerView,
        emptyMessage: TextView,
        contacts: List<Contact>
    ) {
        if (contacts.isEmpty()) {
            recyclerView.visibility = RecyclerView.GONE
            emptyMessage.visibility = TextView.VISIBLE
        } else {
            recyclerView.visibility = RecyclerView.VISIBLE
            emptyMessage.visibility = TextView.GONE
        }
    }

    private fun updateRecyclerViewAdapter(
        recyclerView: RecyclerView,
        inflater: LayoutInflater,
        contacts: List<Contact>
    ) {
        val adapter = ContactAdapter(contacts, inflater)
        recyclerView.adapter = adapter
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "연락처 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }
}
