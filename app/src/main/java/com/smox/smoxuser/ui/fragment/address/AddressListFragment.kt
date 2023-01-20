package com.smox.smoxuser.ui.fragment.address

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentAddressListBinding
import com.smox.smoxuser.manager.Constants.API.ADDRESS_ITEM
import com.smox.smoxuser.manager.Constants.API.IS_EDIT
import com.smox.smoxuser.model.AddressResponse
import com.smox.smoxuser.ui.activity.product.ProductsActivity
import com.smox.smoxuser.ui.adapter.AddressListAdapter
import com.smox.smoxuser.viewmodel.AddressViewModel

class AddressListFragment : Fragment() {

    lateinit var binding: FragmentAddressListBinding
    lateinit var addressViewModel: AddressViewModel
    var addressList = arrayListOf<AddressResponse.AddressData>()
    lateinit var adapter: AddressListAdapter
    var mainPos=0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddressListBinding.inflate(inflater, container, false)
        addressViewModel = (activity as ProductsActivity).addressViewModel
        addressViewModel.isAddressAdded.value = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservars()
        (activity as ProductsActivity).txtTitle.text = "Addresses"
        binding.rvAddress.layoutManager = LinearLayoutManager(requireContext())
        adapter = AddressListAdapter(
            requireContext(),
            addressList,
            object : AddressListAdapter.OnAddressSelect {
                override fun onAddressSelected(pos: Int) {
                    Log.e("TAG", "onAddressSelected: $pos")
                    mainPos=pos
                    val addressItem = addressList[pos]
                    addressViewModel.makeDefaultAddress(addressItem, binding)

                }

                override fun onDeleteSelected(pos: Int) {
                    Log.e("TAG", "onDeleteSelected: $pos")

                    AlertDialog.Builder(activity)
                        .setCancelable(true)
                        .setMessage("Are you sure to delete this address ?")
                        .setPositiveButton(getString(R.string.ok)) { dialog: DialogInterface, _ -> //Prompt the user once explanation has been shown
                            addressViewModel.deleteAddress(
                                requireContext(),
                                addressList[pos].id.toString()
                            )
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _ ->
                            dialog.dismiss()
                        }.create().show()


                }

                override fun onEditSelected(pos: Int) {
                    Log.e("TAG", "onEditSelected: $pos")
                    val addressItem = addressList[pos]
                    val bundle = Bundle()
                    bundle.putParcelable(ADDRESS_ITEM, addressItem)
                    bundle.putBoolean(IS_EDIT, true)
                    Navigation.findNavController(requireView()).navigate(R.id.action_addressListFragment_to_addEditAddressFragment, bundle)

                }
            })

        binding.rvAddress.adapter = adapter
        addressViewModel.getAddressList(requireContext())
        binding.imgAdd.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(IS_EDIT, false)
            Navigation.findNavController(requireView()).navigate(R.id.action_addressListFragment_to_addEditAddressFragment,bundle)
        }
    }

    private fun initObservars() {
        addressViewModel.addressList.observe(viewLifecycleOwner, Observer {
            addressList.clear()
            if (it.isNotEmpty()) {
                addressList.addAll(it)
                addressViewModel.addressCount.set(it.size)
            }
            adapter.notifyDataSetChanged()
        })

        addressViewModel.isAddressAdded.observe(viewLifecycleOwner, Observer {
            if (it) {
                val bundle = Bundle()
                bundle.putParcelable(ADDRESS_ITEM, addressList[mainPos])
                addressViewModel.addressList.value= arrayListOf()
                Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set("KEY", bundle)
                Navigation.findNavController(requireView()).popBackStack()
            }
        })
    }

}