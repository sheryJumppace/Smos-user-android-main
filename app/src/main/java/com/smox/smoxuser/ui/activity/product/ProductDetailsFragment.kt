package com.smox.smoxuser.ui.activity.product

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentProductDetailsBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.Products
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.ProductViewModel


class ProductDetailsFragment : Fragment() {

    lateinit var binding: FragmentProductDetailsBinding
    lateinit var product: Products.ProductItem
    lateinit var productViewModel: ProductViewModel
    lateinit var clickRunnable: Runnable
    val handler = Handler(Looper.getMainLooper())
    var cartCount=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        productViewModel = (activity as ProductsActivity).productViewModel
        productViewModel.isCartAdded.value=false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            product = (arguments?.getParcelable(Constants.API.PRODUCT) as Products.ProductItem?)!!
            binding.product = product
            (activity as ProductsActivity).txtTitle.text = product.product_name.split(' ').joinToString(" ") { it.capitalize() }
        }
        cartCount=productViewModel.cartCount.get()!!
        (activity as ProductsActivity).imgCart.visibility = View.GONE
        (activity as ProductsActivity).cartCount.text=cartCount.toString()

        if (product.quantity == 0)
            product.quantity = 1

        clickListners()
        updateFinalPrice()
        setObservers()
    }

    private fun setObservers() {
        productViewModel.isCartAdded.observe(viewLifecycleOwner, Observer {
            if (it) {
                product.is_cart_added = 1
                cartCount++
                binding.btnAddToCart.text=getString(R.string.addedCart)
                (activity as ProductsActivity).cartCount.text=cartCount.toString()
            }
        })
        binding.productPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    private fun clickListners() {
        binding.txtAbout.setOnClickListener {
            binding.txtFeatures.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_unselect)
            binding.txtFeatures.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.txtAbout.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_corner)
            binding.txtAbout.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.llAboutView.visibility = View.VISIBLE
            binding.llFeaturesView.visibility = View.GONE
        }

        binding.txtFeatures.setOnClickListener {
            binding.txtFeatures.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_corner)
            binding.txtFeatures.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            binding.txtAbout.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_unselect)
            binding.txtAbout.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.llAboutView.visibility = View.GONE
            binding.llFeaturesView.visibility = View.VISIBLE
        }

        binding.btnAddToCart.setOnClickListener {
            if (product.is_cart_added == 0) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("product_id", product.id)
                jsonObject.addProperty("barber_id", product.user_id)
                jsonObject.addProperty(
                    "user_id",
                    SessionManager.getInstance(requireContext().applicationContext).userId
                )
                jsonObject.addProperty("quantity", product.quantity)
                productViewModel.addToCart(
                    requireContext(),
                    (activity as ProductsActivity).progressBar,
                    jsonObject
                )
            }
        }

        binding.imgRemove.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity--
                updateFinalPrice()
                handler.removeCallbacks(clickRunnable)
                handler.postDelayed(clickRunnable, 1000)
            } else
                shortToast("quantity can't be less than 1")
        }

        binding.imgAdd.setOnClickListener {
            if (product.stock >= product.quantity) {
                product.quantity++
                updateFinalPrice()
                handler.removeCallbacks(clickRunnable)
                handler.postDelayed(clickRunnable, 1000)
            } else
                shortToast("Product stock full")
        }

        clickRunnable = Runnable {
            if (product.is_cart_added == 1)
                updateQuantity()
        }
    }

    private fun updateQuantity() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("product_id", product.id)
        jsonObject.addProperty("barber_id", product.user_id)
        jsonObject.addProperty(
            "user_id",
            SessionManager.getInstance(requireContext().applicationContext).userId
        )
        jsonObject.addProperty("quantity", product.quantity)
        productViewModel.updateCartQuantity(
            requireContext(),
            (activity as ProductsActivity).progressBar,
            jsonObject,
            true
        )
    }

    private fun updateFinalPrice() {
        binding.txtFinalPrice.text =
            getString(R.string.format_price, (product.discountedPrice * product.quantity))
        binding.txtQuantity.text = getString(R.string.format_quantity, product.quantity)
    }
}