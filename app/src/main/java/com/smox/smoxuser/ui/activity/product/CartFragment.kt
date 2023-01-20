package com.smox.smoxuser.ui.activity.product

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentCartBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.Constants.API.ADDRESS_ITEM
import com.smox.smoxuser.manager.Constants.API.IS_EDIT
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.AddressResponse
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.CartItems
import com.smox.smoxuser.model.Products
import com.smox.smoxuser.ui.activity.ServiceCheckoutActivity
import com.smox.smoxuser.ui.adapter.CartListAdapter
import com.smox.smoxuser.ui.dialog.OrderPlacedDialog
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.ProductViewModel
import kotlinx.android.synthetic.main.cart_amount_bottom_sheet.view.*


class CartFragment : Fragment(), CartListAdapter.updateQtyClickListner {

    lateinit var binding: FragmentCartBinding
    lateinit var productViewModel: ProductViewModel
    lateinit var cartAdapter: CartListAdapter
    var cartList = arrayListOf<CartItems>()
    val handler = Handler(Looper.getMainLooper())
    lateinit var cartItem: CartItems
    lateinit var selectedAddress: AddressResponse.AddressData
    var qty = 0
    lateinit var clickRunnable: Runnable
    var addressChanged = false
    lateinit var resultPayment: ActivityResultLauncher<Intent>
    var isAddressOk = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        productViewModel = (activity as ProductsActivity).productViewModel
        (activity as ProductsActivity).imgSearch.visibility = View.GONE
        (activity as ProductsActivity).txtTitle.text = "Cart"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        cartAdapter = CartListAdapter(requireContext(), cartList, this)
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter
        productViewModel.getCartListByBarber(
            requireContext(),
            (activity as ProductsActivity).progressBar
        )
        addressChanged = false
        initClick()
        setGetAddressListner()

        resultPayment =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val intent = result.data
                intent?.getStringExtra(Constants.API.PAY_STATUS)
                intent?.getStringExtra(Constants.API.PAY_MESSAGE)

                val jsonObject = getJsonObject()
                jsonObject.addProperty(
                    "client_secret",
                    productViewModel.paymentIntentData.get()?.client_secret
                )
                val jsonaray = JsonArray()
                for (item in productViewModel.paymentIntentData.get()?.checkout_id!!) {
                    jsonaray.add(item)
                }
                jsonObject.add(
                    "checkout_id",
                    jsonaray
                )

                Log.e("TAG", "onViewCreated: abcd $jsonObject")
                productViewModel.confirmPayment(
                    requireContext(),
                    (activity as ProductsActivity).progressBar,
                    jsonObject
                )
            }

    }

    private fun initClick() {
        binding.priceDetailBottom.btnAddAddress.setOnClickListener {

            val args = Bundle()
            args.putBoolean(IS_EDIT, false)
            Navigation.findNavController(requireView())
                .navigate(R.id.action_cartFragment_to_addEditAddressFragment)
        }

        binding.priceDetailBottom.txtEdit.setOnClickListener {
            Navigation.findNavController(requireView())
                .navigate(R.id.action_cartFragment_to_addressListFragment)
        }

        binding.priceDetailBottom.txtCheckout.setOnClickListener {
            val jsonObject = getJsonObject()
            productViewModel.callCheckoutApi(
                requireContext(),
                (activity as ProductsActivity).progressBar,
                jsonObject
            )
        }
    }

    private fun getJsonObject(): JsonObject {
        val jsonArray = JsonArray()
        val cartData = productViewModel.cartData.value
        for (item in cartData?.cart_items!!) {
            jsonArray.add(item.id)
        }

        val cartAmountObject = JsonObject()
        for (item in cartData.cart_items) {
            val jsonObject1 = JsonObject()
            jsonObject1.addProperty("qty_discounted_price", item.product?.qty_discounted_price)
            jsonObject1.addProperty("qty_price", item.product?.qty_price)
            jsonObject1.addProperty("qty_shipping", item.product?.qty_shipping)
            jsonObject1.addProperty("quantity", item.quantity)
            jsonObject1.addProperty("cart_id", item.id)
            cartAmountObject.add(item.product_id.toString(), jsonObject1)
        }

        Log.e("TAG", "getJsonObject: $cartAmountObject")

        val user = Barber(SessionManager.getInstance(requireContext()).userData)
        val oneLineAddress =
            selectedAddress.address_one + ", " + selectedAddress.address_two + " " + selectedAddress.city + " " + selectedAddress.state + " " + selectedAddress.country + " " + selectedAddress.zipcode
        val jsonObject = JsonObject()
        jsonObject.addProperty("barber_id", cartData.cart_items[0].barber_id)
        jsonObject.addProperty("name", selectedAddress.first_name + " " + selectedAddress.last_name)
        jsonObject.addProperty("phone", selectedAddress.phone)
        jsonObject.addProperty("email", user.email)
        jsonObject.addProperty("address_id", selectedAddress.id)
        jsonObject.addProperty("address", oneLineAddress)
        jsonObject.addProperty("subtotal", cartData.discounted_price.toString())
        jsonObject.addProperty("discounted_price", cartData.discounted_price.toString())
        jsonObject.addProperty("discount", cartData.discount.toString())
        jsonObject.addProperty("shipping", cartData.shipping.toString())
        jsonObject.addProperty("total", cartData.total.toString())
        jsonObject.add("cart_id", jsonArray)
        jsonObject.add("single_product", cartAmountObject)
        jsonObject.addProperty("latitude", selectedAddress.latitude)
        jsonObject.addProperty("longitude", selectedAddress.longitude)

        Log.e("TAG", "Main Json: $jsonObject")

        return jsonObject
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservers() {
        productViewModel.cartData.observe(viewLifecycleOwner, Observer { cartData ->
            if (cartData.cart_items != null) {
                cartList.clear()
                cartList.addAll(cartData.cart_items)
                cartAdapter.notifyDataSetChanged()
                binding.priceDetailBottom.cartData = cartData
                if (cartData.default_address.first_name?.isNotEmpty()!!) {
                    if (cartData.zip_error.isNotEmpty()) {
                        isAddressOk = false
                        binding.priceDetailBottom.txtCheckout.isEnabled = false
                        shortToast(cartData.zip_error)
                        return@Observer
                    }
                    binding.priceDetailBottom.txtCheckout.isEnabled = true
                    binding.priceDetailBottom.btnAddAddress.visibility = View.GONE
                    binding.priceDetailBottom.llAddress.visibility = View.VISIBLE

                    if (!addressChanged) {
                        selectedAddress = cartData.default_address
                        updateDeliveryAddress()
                    }
                } else {
                    binding.priceDetailBottom.btnAddAddress.visibility = View.VISIBLE
                    binding.priceDetailBottom.llAddress.visibility = View.GONE
                }
            } else {
                Navigation.findNavController(requireView()).popBackStack()
            }
        })
        productViewModel.isCheckoutSuccess.value=false
        productViewModel.isCheckoutSuccess.observe(viewLifecycleOwner, Observer {
            if (it) {
                val intent = Intent(
                    requireActivity(),

                    ServiceCheckoutActivity::class.java
                )
                intent.putExtra(
                    Constants.API.PAYMENT_INTENT,
                    productViewModel.paymentIntentData.get()?.client_secret
                )
                intent.putExtra(Constants.API.CALLED_FROM, "Cart")
                resultPayment.launch(intent)
            }
        })

        productViewModel.isOrderPlaced.observe(viewLifecycleOwner, Observer {
            if (it) {
                OrderPlacedDialog(requireActivity())
            }
        })

        clickRunnable = Runnable {
            updateQuantity(cartItem, qty)
            Log.e("TAG", "initObservers: cart product name=${cartItem.product!!.product_name} qty= $qty")
            qty = 0
        }
        val sheetBehavior = BottomSheetBehavior.from(binding.priceDetailBottom.root);
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    override fun onAddQuantity(pos: Int) {
        cartItem = cartList[pos]
        if (qty == 0) qty = cartItem.quantity

        if (cartItem.product!!.stock > qty) {
            qty++
            cartItem.quantity = qty
            binding.priceDetailBottom.txtCheckout.isEnabled = false
            cartAdapter.notifyItemChanged(pos)
            handler.removeCallbacks(clickRunnable)
            handler.postDelayed(clickRunnable, 1000)

        } else {
            shortToast("Stock full")
        }
    }

    private fun updateQuantity(cartItem: CartItems, qty: Int) {

        val jsonObject = JsonObject()
        jsonObject.addProperty("product_id", cartItem.product_id)
        jsonObject.addProperty("barber_id", cartItem.barber_id)
        jsonObject.addProperty("user_id", SessionManager.getInstance(requireContext().applicationContext).userId)
        jsonObject.addProperty("quantity", qty)
        productViewModel.updateCartQuantity(
            requireContext(),
            (activity as ProductsActivity).progressBar,
            jsonObject,
            false
        )
    }


    private fun setGetAddressListner() {
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>(
            "KEY"
        )
            ?.observe(viewLifecycleOwner) {
                if (it != null) {
                    addressChanged = true
                    selectedAddress = it.getParcelable(ADDRESS_ITEM)!!
                    updateDeliveryAddress()
                }
            }
    }

    private fun updateDeliveryAddress() {
        binding.priceDetailBottom.txtAddressLine.text = selectedAddress.defaultAddress()
    }


    override fun onRemoveQuantity(pos: Int) {
        cartItem = cartList[pos]
        if (qty == 0) qty = cartItem.quantity
        if (qty > 1) {
            qty--
            cartItem.quantity = qty
            binding.priceDetailBottom.txtCheckout.isEnabled = false
            cartAdapter.notifyItemChanged(pos)
            handler.removeCallbacks(clickRunnable)
            handler.postDelayed(clickRunnable, 1000)
        } else {
            shortToast("quantity can't be less than 1")
        }
    }

    override fun onDeletCartItem(pos: Int) {

        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.deleteCart))
            .setCancelable(false).setMessage(getString(R.string.deleteCartMsg))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                updateQuantity(cartList[pos], 0)
            }.setNegativeButton(getString(R.string.cancel)) { _, _ ->

            }.create().show()
    }
}