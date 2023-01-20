package com.smox.smoxuser.ui.activity.product

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AddressRepository
import com.smox.smoxuser.databinding.ActivityAddressMainBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Address
import com.smox.smoxuser.model.Product
import com.smox.smoxuser.stripe.controller.ErrorDialogHandler
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.CardListActivity
import com.smox.smoxuser.ui.activity.ProductCheckoutActivity
import com.smox.smoxuser.ui.adapter.AddressAdapter
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.AddressListViewModel
import com.smox.smoxuser.viewmodel.AddressListViewModelFactory
import com.stripe.android.PaymentSession
import com.stripe.android.PaymentSessionData
import com.stripe.android.model.Customer
import kotlinx.android.synthetic.main.activity_address_main.*
import org.json.JSONObject

class AddressMainActivity : BaseActivity() {

    private lateinit var viewModel: AddressListViewModel
    private lateinit var activity: AddressMainActivity
    private var addressID: Int = 0
    private lateinit var listAddress: List<Address>
    private lateinit var selectedAddress: List<Address>
    private var product: Product? = null
    private lateinit var binding: ActivityAddressMainBinding
    private var productTotalPrice: Float = 0f
    private var productPrice: Float = 0f
    private var mCustomer: Customer? = null
    private lateinit var mErrorDialogHandler: ErrorDialogHandler
    private var mPaymentSession: PaymentSession? = null
    private var mPaymentSessionData: PaymentSessionData? = null
    private lateinit var mContext: Context
    var card_id: String? = null;

    companion object {
        private const val ADDRESS_EDIT = 1011
    }

    private var isDelivery: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_address_main)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@ProductDetailActivity, Constants.backButton))
        }
        mContext = applicationContext
        activity = AddressMainActivity()
        product = intent.getSerializableExtra("ProductDetail") as Product
        isDelivery = intent.getBooleanExtra("IsDelivery", true)

        binding.txtAddrProductName.text = product!!.title
        productPrice = product!!.price
        //binding.txtAddrProductPrice.text = productPrice.toString()
        binding.txtAddrProductPrice.text = String.format(
            resources.getString(R.string.product_price_with_quantity),
            product!!.quantity.get().toString(),
            productPrice.toString()
        )
        binding.txtAddrProductQuantity.text = product!!.quantity.get().toString()
        binding.txtProductShipCharges.text = product!!.shippingCharges.toString()
        //binding.txtProductShipCharges.text = 20.0.toString()

        //binding.txtProductTax.text = 10.0.toString()
        //productTotalPrice = product!!.price * product!!.quantity.get()

        val taxCalculate = product!!.tax * product!!.quantity.get()
        binding.txtProductTax.text = String.format(
            resources.getString(R.string.product_tax_with_quantity),
            product!!.quantity.get().toString(),
            product!!.tax.toString()
        )

        if (isDelivery) {
            productTotalPrice =
                (product!!.price * product!!.quantity.get()) + (product!!.shippingCharges + taxCalculate)
        } else {
            productTotalPrice = (product!!.price * product!!.quantity.get()) + (taxCalculate)
        }

        binding.txtAddrProductTotal.text = productTotalPrice.toString()

        mErrorDialogHandler = ErrorDialogHandler(supportFragmentManager)

        val factory =
            AddressListViewModelFactory(AddressRepository.getInstance(addressId = app.currentUser.id))
        viewModel = ViewModelProviders.of(this, factory).get(AddressListViewModel::class.java)

        val addressAdapter = AddressAdapter()

        binding.recAddress.adapter = addressAdapter

        viewModel.addresses.observe(this, Observer { addresses ->
            listAddress = ArrayList()
            selectedAddress = ArrayList()
            if (isDelivery && addresses != null) {
                if (addresses.isNotEmpty()) {
                    listAddress = addresses
                    selectedAddress = listOf(addresses.get(0))
                    addressAdapter.submitList(addresses)
                    addressAdapter.notifyDataSetChanged()
                    binding.txtNoAddress.visibility = View.GONE
                    binding.recAddress.visibility = View.VISIBLE

                } else {
                    binding.txtNoAddress.visibility = View.VISIBLE
                    binding.recAddress.visibility = View.GONE
                }
                binding.lnrShippingCharges.visibility = View.VISIBLE
            } else {
                binding.lnrShippingCharges.visibility = View.GONE
            }

        })

        if (isDelivery) {
            viewModel.fetchList(this@AddressMainActivity)
            recAddress.visibility = View.VISIBLE
            binding.lnrShippingCharges.visibility = View.VISIBLE
        } else {
            recAddress.visibility = View.GONE
            binding.lnrShippingCharges.visibility = View.GONE
        }


//        setupStripePayment()

        viewModel.deleteAddress.observe(this, Observer { deleteAddress ->
            viewModel.fetchList(this@AddressMainActivity)
        })

        /*addressAdapter.setItemClickListener(object: AddressAdapter.ItemClickListener{
            override fun onItemClick(view: View, barberId: Int) {
                if(view.id == R.id.btnDelete && appointment != null){
                    showDeleteConfirmDialog(appointment)
                }else{
                    if(barber.accountType == UserType.Barber || appointment?.customerId == barber.id){
                        val intent = Intent(activity, AppointmentDetailActivity::class.java)
                        intent.putExtra("appoint_id", appointmentId)
                        startActivity(intent)
                        activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                    }
                }
            }
        })*/

        selectedAddress = ArrayList()
        addressAdapter.setItemClickListener(object : AddressAdapter.ItemClickListener {
            override fun OnItemClick(view: View, position: Int) {
                if (view.id == R.id.imgAddressDelete) {
                    showDeleteConfirmDialog(listAddress[position].id.toString())
                } else if (view.id == R.id.imgAddressEdit) {
                    val intent = Intent(this@AddressMainActivity, AddAddressActivity::class.java)
                    intent.putExtra("AddressDetail", listAddress!!.get(position))
                    intent.putExtra("IsNewAddress", false)
                    startActivityForResult(intent, ADDRESS_EDIT)
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                } else {
                    selectedAddress = ArrayList()
                    selectedAddress = listOf(listAddress.get(position))
                }
            }

        })



        binding.btnAddressAddCard.setOnClickListener {
            val intent = Intent(this@AddressMainActivity, CardListActivity::class.java)
            startActivityForResult(intent, 101)
//            mPaymentSession!!.presentPaymentMethodSelection()
        }

        binding.btnAddressCheckOut.setOnClickListener {
            checkoutProduct()

//            mPaymentSession!!.completePayment { data, listener ->
//                if (data.selectedPaymentMethodId != null && mCustomer != null) {
//                    val source = mCustomer!!
//                        .getSourceById(data.selectedPaymentMethodId!!)
//                    val customerID = mCustomer!!.id
//                    source?.id?.apply {
//                        buyProduct(customerID, this)
//                    }
//                }
//            }
        }

    }

    private fun checkoutProduct() {

        if(product!!.stripe_public_key.isBlank()){
            showAlertDialog(
                "",
                resources.getString(R.string.text_key_not_get_error),
                DialogInterface.OnClickListener { _, _ -> },
                getString(R.string.ok),
                null,
                null
            )
            return
        }

        var addressId = "0"

        if (isDelivery) {
            if (selectedAddress.isEmpty()) {
                shortToast(resources.getString(R.string.error_select_address))
                return
            } else {
                addressId = selectedAddress[0].id.toString()
            }
        }

        val intent = Intent(this@AddressMainActivity, ProductCheckoutActivity::class.java)
        intent.putExtra("ProductDetail", product)
        intent.putExtra("addressId", addressId)
        intent.putExtra("productTotalPrice", productTotalPrice)
        startActivity(intent)
    }

    private fun setupStripePayment() {
//        binding.productProgressBar.visibility = View.VISIBLE
//        PaymentConfiguration.init(sessionManager.Sp_publishableKey!!)

        setupCustomerSession()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_address -> {
                val addAddressIntent = Intent(this, AddAddressActivity::class.java)
                addAddressIntent.putExtra("IsNewAddess", true)
                startActivityForResult(addAddressIntent, ADDRESS_EDIT)
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        //viewModel.fetchList(this@AddressMainActivity)
    }

    private fun showDeleteConfirmDialog(addressId: String) {
        val builder = AlertDialog.Builder(this@AddressMainActivity)
        builder.setTitle("Delete Address")
        builder.setMessage("Are you sure you want to delete the selected address?")
        builder.setPositiveButton("Delete") { _, _ ->
            viewModel.deleteAddress(this@AddressMainActivity, addressId)
            //deleteAddress(this@AddressMainActivity, addressId)
        }
        builder.setNegativeButton("Cancel") { _, _ ->

        }
        builder.show()
    }

    fun deleteAddress(context: Context, addressId: String) {
        val params = HashMap<String, String>()
        params["id"] = addressId

        progressHUD.show()

        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.delete_address,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    if (result.has("message")) {
                        shortToast(result.getString("message"))
                    }
                    viewModel.fetchList(this@AddressMainActivity)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }

            }
        )
    }

    private fun setupCustomerSession() {
//        CustomerSession.initCustomerSession(
//            ExampleEphemeralKeyProvider({ string ->
//                if (string.startsWith("Error: ")) {
//                    mErrorDialogHandler.show(string)
//                }
//            }, applicationContext)
//        )
//
//        CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
//            override fun onCustomerRetrieved(customer: Customer) {
//                mCustomer = customer
//                binding.productProgressBar.visibility = View.INVISIBLE
//                setupPaymentSession()
//            }
//
//            override fun onError(errorCode: Int, errorMessage: String?, stripeError: StripeError?) {
//                mCustomer = null
//                binding.btnAddressAddCard.isEnabled = false
////                binding.btnAddressCheckOut.isEnabled = false
//                mErrorDialogHandler.show(errorMessage!!)
//                binding.productProgressBar.visibility = View.INVISIBLE
//            }
//        })
    }


//    private fun setupPaymentSession() {
//        mPaymentSession = PaymentSession(this)
//        val paymentSessionInitialized = mPaymentSession!!.init(
//            PaymentSessionListenerImpl(this),
//            PaymentSessionConfig.Builder()
//                .setShippingInfoRequired(false)
//                .setShippingMethodsRequired(false)
//                .build()
//        )
//        if (paymentSessionInitialized) {
//            binding.btnAddressAddCard.isEnabled = true
//            mPaymentSession!!.setCartTotal(productTotalPrice.toLong() * 100)
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*if (requestCode == 101 && resultCode == RESULT_OK)
        {
            val bundle= data!!.extras
            card = bundle!!.getSerializable("card") as Cards
            btnAddressAddCard.text = "ADD CARD("+ card!!.CardBrand+" "+ card!!.CardLastFourDigit+")"
            this.card = card
            binding.btnAddressCheckOut.isEnabled = true
        }*/

//        data?.apply {
//            mPaymentSession?.handlePaymentData(requestCode, resultCode, data)
//        }

            if (requestCode == ADDRESS_EDIT) {
                data?.apply {
                    if (data.getBooleanExtra("IsAddressUpdate", false)) {
                        viewModel.fetchList(this@AddressMainActivity)
                    }

                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
//        mPaymentSession?.onDestroy()
    }

//    private fun onPaymentSessionDataChanged(data: PaymentSessionData) {
//        mPaymentSessionData = data
//        binding.productProgressBar.visibility = View.VISIBLE
//
//        CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
//            override fun onCustomerRetrieved(customer: Customer) {
//                mCustomer = customer
//                binding.productProgressBar.visibility = View.INVISIBLE
//                if (mPaymentSessionData != null) {
//                    if (data.selectedPaymentMethodId != null && mCustomer != null) {
//
//                        val source = mCustomer!!
//                            .getSourceById(data.selectedPaymentMethodId!!)
//                        if (source != null) {
//                            val cardSource = source.asSource()
//                            if (cardSource != null) {
//                                val scd = cardSource.sourceTypeModel as SourceCardData
//                                binding.btnAddressAddCard.text = String.format("ADD CARD(%s %s)", scd.brand?.toLowerCase()?.capitalize(), scd.last4)
//
//                            }
////                            binding.btnAddressCheckOut.isEnabled = data.isPaymentReadyToCharge
//                        }
//                    }
//                }
//            }
//
//            override fun onError(errorCode: Int, errorMessage: String?, stripeError: StripeError?) {
//                binding.productProgressBar.visibility = View.INVISIBLE
//            }
//        })
//    }

//    private class PaymentSessionListenerImpl(activity: AddressMainActivity) :
//        PaymentSession.ActivityPaymentSessionListener<AddressMainActivity>(activity) {
//
//        override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
//            val activity = listenerActivity ?: return
//
//            activity.binding.productProgressBar.visibility = if (isCommunicating) View.VISIBLE else View.INVISIBLE
//        }
//
//        override fun onError(errorCode: Int, errorMessage: String?) {
//            val activity = listenerActivity ?: return
//
//            activity.mErrorDialogHandler.show(errorMessage!!)
//        }
//
//        override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
//            val activity = listenerActivity ?: return
//
//            activity.onPaymentSessionDataChanged(data)
//        }
//    }

}
