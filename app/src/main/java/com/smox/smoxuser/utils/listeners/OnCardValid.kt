package com.smox.smoxuser.utils.listeners

import com.stripe.android.model.CardParams
import com.stripe.android.model.PaymentMethodCreateParams


interface OnCardValid {
    fun onCardEntered(CardParam: CardParams)
    fun onNewCard(card: PaymentMethodCreateParams?)
}