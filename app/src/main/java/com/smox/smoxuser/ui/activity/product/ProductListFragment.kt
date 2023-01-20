package com.smox.smoxuser.ui.activity.product

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.JsonObject
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentProductListBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.Constants.API.BARBER_ID
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.CartItems
import com.smox.smoxuser.model.Products
import com.smox.smoxuser.ui.adapter.ProductAdapter
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.ProductViewModel

class ProductListFragment : Fragment(), ProductAdapter.ItemClickListner {

    lateinit var binding: FragmentProductListBinding
    private lateinit var adapter: ProductAdapter
    private var products: ArrayList<Products.ProductItem> = ArrayList()
    lateinit var productViewModel: ProductViewModel
    var cartListId = arrayListOf<Int>()
    var newcartItem = 0
    var pos = 0
    var cartCountt = 0
    lateinit var cartItem:CartItems
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        productViewModel = (activity as ProductsActivity).productViewModel

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "ProductList onResume: ", )
    }

    override fun onStop() {
        super.onStop()
        Log.e("TAG", "ProductList onStop: ", )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ProductAdapter(requireContext(),this)

        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = adapter

        (activity as ProductsActivity).txtTitle.text = "Products"
        (activity as ProductsActivity).imgCart.visibility = View.GONE

        productViewModel.userId.value=SessionManager.getInstance(requireContext()).userId.toString()

        binding.viewModel = productViewModel

        initObserver()

        productViewModel.getCartListByBarber(
            requireContext(),
            (activity as ProductsActivity).progressBar
        )
    }

    private fun initObserver() {
        productViewModel.productRes.observe(viewLifecycleOwner, Observer { productsRes ->
            if (productsRes != null) {
                if (productsRes.result.isNotEmpty()) {
                    val productList = productsRes.result
                    if (cartListId.size > 0) {
                        for (prod in productList) {
                            val ind = cartListId.find { it == prod.id }
                            if (ind != null) {
                                prod.is_cart_added = 1
                            }
                        }
                    }
                    products.clear()
                    products.addAll(productList)
                    adapter.setData(products)
                    productsRes.result.clear()
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.txtBtNoProduct.visibility = View.GONE
                } else {
                    //Toast.makeText(context, resources.getString(R.string.styler_product_no_found), Toast.LENGTH_LONG).show()
                    binding.txtBtNoProduct.visibility = View.VISIBLE
                    binding.rvProducts.visibility = View.GONE

                }
            }
        })

        productViewModel.isCartAdded.observe(viewLifecycleOwner, Observer {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (it) {
                    if (newcartItem != null) {
                        cartListId.add(newcartItem)
                    }
                    Log.e("TAG", "initObserver: before $cartCountt")
                    cartCountt++
                    Log.e("TAG", "initObserver: after $cartCountt")
                    productViewModel.cartCount.set(cartCountt)
                    products[pos].is_cart_added = 1
                    adapter.notifyItemChanged(pos)

                    productViewModel.cartData.value?.cart_items?.add(cartItem)

                }
            }
        })

        productViewModel.cartData.observe(viewLifecycleOwner, Observer {

            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                cartListId.clear()
                if (it.cart_items != null) {
                    for (item in it.cart_items)
                        cartListId.add(item.product_id)
                }
                cartCountt = productViewModel.cartCount.get()!!
                productViewModel.getAllProducts(
                    requireContext(),
                    (activity as ProductsActivity).progressBar
                )
            }
        })

        binding.flCart.setOnClickListener {
            if (cartListId.size > 0) {
                Navigation.findNavController(it).navigate(R.id.action_productListFragment_to_cartFragment)
            } else
                shortToast("Cart is empty")
        }
    }

    override fun onItemClickListner(product: Products.ProductItem) {
        if (product.is_cart_added == 1) {
            val qty =
                productViewModel.cartData.value?.cart_items?.find { it.product_id == product.id }?.quantity
            product.quantity = qty!!
        }
        val bundle = Bundle()
        bundle.putParcelable(Constants.API.PRODUCT, product)
        Navigation.findNavController(requireView()).navigate(R.id.action_productListFragment_to_productDetailsFragment, bundle)
    }

    override fun onAddToCartClickListner(product: Products.ProductItem, position: Int) {

        val jsonObject = JsonObject()
        jsonObject.addProperty("product_id", product.id)
        jsonObject.addProperty("barber_id", product.user_id)
        jsonObject.addProperty(
            "user_id",
            SessionManager.getInstance(requireContext().applicationContext).userId
        )
        jsonObject.addProperty("quantity", 1)
        cartItem=CartItems(0,product.id,product.user_id,SessionManager.getInstance(requireContext().applicationContext).userId
            ,1,0,"","",false, product)
        pos = position
        newcartItem = product.id
        productViewModel.addToCart(
            requireContext(),
            (activity as ProductsActivity).progressBar,
            jsonObject
        )
    }


}