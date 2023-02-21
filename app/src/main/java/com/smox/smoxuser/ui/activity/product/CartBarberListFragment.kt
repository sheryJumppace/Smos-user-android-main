package com.smox.smoxuser.ui.activity.product

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentCartBarberListBinding
import com.smox.smoxuser.model.CartBarberItem
import com.smox.smoxuser.model.CartBarberResponse
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.ui.adapter.CartBarberListAdapter
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.ProductViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class CartBarberListFragment : Fragment(), CartBarberListAdapter.OnCartBarberSelect {

    lateinit var binding: FragmentCartBarberListBinding
    lateinit var productViewModel: ProductViewModel
    var cartBarberList = arrayListOf<CartBarberItem>()
    lateinit var cartBarberAdapter: CartBarberListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCartBarberListBinding.inflate(inflater, container, false)
        productViewModel =  (activity as ProductsActivity).productViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartBarberAdapter = CartBarberListAdapter(requireContext(), cartBarberList, this)
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartBarberAdapter
        (activity as ProductsActivity).txtTitle.text = "Cart by stylers"
        (activity as ProductsActivity).imgCart.visibility = View.GONE
        (activity as ProductsActivity).imgSearch.visibility = View.GONE
        //if (isAdded)
        getCartBarberList()

    }

    private fun getCartBarberList() {
        Log.d("++--++","getCartBarberList is called")

        val progressBar = (activity as ProductsActivity).progressBar
        progressBar.show()
        ApiRepository(requireContext()).getCartBarberList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CartBarberResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onNext(res: CartBarberResponse) {
                    progressBar.dismiss()

                    if (!res.error) {
                        Log.d("++--++","getCartBarberList is onNext : "+res.message)
                        Log.d("++--++","getCartBarberList is onNext : "+ (Gson().toJson(res)))
                        if (res.result.isNotEmpty()) {
                            cartBarberList.clear()
                            cartBarberList.addAll(res.result)
                            binding.rvCart.visibility = View.VISIBLE
                            binding.txtEmptyCart.visibility = View.GONE
                        }
                    } else {
                        binding.rvCart.visibility = View.GONE
                        binding.txtEmptyCart.visibility = View.VISIBLE
                    }

                }

                override fun onError(e: Throwable) {
                    progressBar.dismiss()
                    Log.e("TAG", "onError: ${e.message}")
                    Log.d("++--++","getCartBarberList is calledonError : "+e.message)
                    /*if ((e as HttpException).code()==401)
                                           shortToast(requireContext().getString(R.string.authError))
                                       else
                                           shortToast(e.message())*/
                }

                override fun onComplete() {

                }
            })
    }

    override fun onCartBarberSelected(pos: Int) {

        productViewModel.barberId.value=cartBarberList[pos].barberId.toString()
        Navigation.findNavController(requireView()).navigate(R.id.action_cartBarberListFragment_to_cartFragment)

    }
}