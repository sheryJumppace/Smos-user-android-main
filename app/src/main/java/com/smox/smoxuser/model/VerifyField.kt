package com.smox.smoxuser.model

import java.io.Serializable

class VerifyField(id: String) : Serializable {
    var id = ""
    var title = ""
    var value = ""

    init {
        this.id = id
        this.value = ""
        setTitle()
    }

    private fun setTitle() {
        val ids = id.split(".").toTypedArray()
        if (ids.size > 1) {
            val param = ids[1]
            if (param == "id_number") {
                this.title = "Personal ID number"
            } else {
                this.title = ids[ids.size - 1].replace("_"," ").capitalize()
            }
        } else {
            this.title = ids[0].capitalize()
        }
    }
}
