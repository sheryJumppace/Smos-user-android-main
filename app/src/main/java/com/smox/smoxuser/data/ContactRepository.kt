package com.smox.smoxuser.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.SmoxUser
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Repository module for handling data operations.
 */
class ContactRepository : BaseObservable() {

    var isUpdated: MutableLiveData<Boolean> = MutableLiveData()
    var contacts: ArrayList<SmoxUser> = ArrayList<SmoxUser>()

    fun getContact(id: Int): SmoxUser? {
        return contacts.find { it.id == id }
    }

    fun getContactFound(id: Int): Boolean {
        var found = false
        for (contact in contacts) {
            if (contact.id == id) {
                found = true
            }
        }
        return found
    }

    fun getContacts(isFavorite: Boolean): MutableLiveData<List<SmoxUser>> {
        val contacts =
            contacts.filter { contact -> if (isFavorite) contact.isFavorite.get() else true }
        val newList: ArrayList<SmoxUser> = ArrayList<SmoxUser>()
        for (element in contacts) {
            // Loop arrayList1 items
            var found = false
            for (element1 in newList) {
                if (element1.id == element.id) {
                    found = true
                }
            }
            if (!found) {
                newList.add(element)
                Log.e("Contact Repository User", "${element.toString()}")
            }
        }
        Log.e(
            "Contact Repository",
            "getContacts Contact size :- ${contacts.size} , New list size:- ${newList.size}"
        )
        return MutableLiveData(newList)
    }

    fun didDeselectAll() {
        for (contact in contacts) {
            contact.isSelected.set(false)
        }
        isUpdated.postValue(true)
    }

    fun getSelectedContacts(): List<SmoxUser> {
        val contacts = contacts.filter { contact -> contact.isSelected.get() }
        val newList: ArrayList<SmoxUser> = ArrayList<SmoxUser>()
        for (element in contacts) {
            // Loop arrayList1 items
            var found = false
            for (element1 in newList) {
                if (element1.id == element.id) {
                    found = true
                }
            }
            if (!found) {
                newList.add(element)
                Log.e("Contact Repository User", "${element.toString()}")
            }
        }
        Log.e(
            "Contact Repository",
            "getSelectedContacts Contact size :- ${contacts.size} , New list size:- ${newList.size}"
        )
        return newList
    }

    fun updateContacts(users: List<SmoxUser>) : Boolean {
        var isAlreadyAdded = true
        for (contact in users) {
            if (!getContactFound(contact.id)) {
                contacts.add(contact)
                isAlreadyAdded= false
            }
        }
        isUpdated.postValue(true)
        return isAlreadyAdded
    }

    fun updateFavoriteState(context: Context, id: Int) {
        val all = contacts
        val contact = all.find { it.id == id } ?: return
        val isFavorite = contact.isFavorite.get()
//        contact.isFavorite.set(isFavorite)
//        this.contacts =all
        isUpdated.postValue(true)

        val method = if (isFavorite) Request.Method.POST else Request.Method.DELETE
        val params = HashMap<String, String>()
        params["contact"] = id.toString()

        APIHandler(
            context,
            method,
            Constants.API.favorite,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {

                }

                override fun onFail(error: String?) {
                    shortToast(error)
                }
            })
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: ContactRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ContactRepository().also { instance = it }
            }
    }

    fun fetchList(context: Context) {
        val params = HashMap<String, String>()

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.smox_talk,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONObject("result").getJSONArray("contacts")
                    val items: ArrayList<SmoxUser> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val user = SmoxUser(json)
                        if (!items.contains(user)) {
                            items.add(user)
                            Log.e("Contact Repository User", "${user.toString()}")
                        }
                    }
                    Log.e(
                        "Contact Repository",
                        "fetchList Items size :- ${items.size} , jsonArray list size:- ${jsonArray.length()}"
                    )
                    contacts = items
                    isUpdated.postValue(true)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}
