package com.smox.smoxuser.model

class CartResponse(
    val error: Boolean,
    val message: String,
    val result: CartData
)

class CartData(
    val cart_items: ArrayList<CartItems>,
    val cart_count: Int,
    val subtotal: Double,
    val discounted_price: Double,
    val discount: Double,
    val shipping: Double,
    val zip_error: String,
    val total: Double,
    val default_address: AddressResponse.AddressData
)

class CartItems(
    var id: Int,
    val product_id: Int,
    val barber_id: Int,
    val user_id: Int,
    var quantity: Int,
    val is_deleted: Int,
    val created_at: String,
    val updated_at: String,
    val stock_error: Boolean,
    val product: Products.ProductItem?
)