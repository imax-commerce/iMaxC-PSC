package com.imaxcorp.imaxc.data

import java.io.Serializable

class Token(): Serializable {
    var tokenMessaging: String = ""

    constructor(tokenMessaging: String) : this(){
        this.tokenMessaging = tokenMessaging
    }
}