package com.smox.smoxuser.model

import java.io.Serializable

class Verify : Serializable {
    private var ids  = ""
    var disabledReason:String = ""
    var accountToken:String = ""
    var fields:ArrayList<VerifyField> = ArrayList()

    var requiredFields: String
        get() {
            return ids
        }
        set(data) {
            ids = data
            if(fields.isEmpty()){
                val items = requiredFields.split(",").forEach{
                    fields.add(VerifyField(it))
                }
            }
        }
}
