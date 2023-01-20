package com.smox.smoxuser.ui.activity.barber

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smox.smoxuser.R
import com.smox.smoxuser.data.UpNextOptionRepository
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.UpNextOptionAdapter
import com.smox.smoxuser.ui.dialog.AddUpNextOptionDialog
import com.smox.smoxuser.viewmodel.UpNextOptionListViewModel
import com.smox.smoxuser.viewmodel.UpNextOptionListViewModelFactory
import kotlinx.android.synthetic.main.activity_up_next_options.*

class UpNextOptionsActivity : BaseActivity() {
    private lateinit var viewModel: UpNextOptionListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_up_next_options)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@UpNextOptionsActivity, Constants.backButton))
        }

        val factory = UpNextOptionListViewModelFactory(UpNextOptionRepository.getInstance())
        viewModel = ViewModelProvider(this, factory).get(UpNextOptionListViewModel::class.java)
        val adapter = UpNextOptionAdapter()
        adapter.setItemClickListener(object : UpNextOptionAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (view.id == R.id.btnDelete) {
                    showConfirmDeleteService(position)
                } else {
                    val option = viewModel.getOption(position)
                    option?.apply {
                        val returnIntent = Intent()
                        returnIntent.putExtra("status", option.title)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                }
            }
        })
        recyclerView.adapter = adapter
        viewModel.options.observe(this, Observer { options ->
            if (options != null) {
                adapter.submitList(options)
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.fetchList(this@UpNextOptionsActivity)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add -> {
                openRoomDialog()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openRoomDialog() {
        val dialog = AddUpNextOptionDialog(this)

        dialog.show()
        dialog.confirmButton.setOnClickListener {
            val title = dialog.valueEditText.text.toString()
            dialog.dismiss()
            viewModel.addOption(this@UpNextOptionsActivity, title)
        }
    }

    private fun showConfirmDeleteService(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete the selected service?")
        builder.setPositiveButton("DELETE") { _, _ ->
            viewModel.deleteOption(this@UpNextOptionsActivity, position)
        }
        builder.setNegativeButton("CANCEL", null)
        builder.show()
    }
}
