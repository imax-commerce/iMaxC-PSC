package com.imaxcorp.imaxc.data

data class NotificationContent(
    var title: String? = "",
    var text: String? = ""
)

data class Data(
    var title: String? = "",
    var text: String? = "",
    var idClient: String? = "",
    var origin: String? = "",
    var destination: String? = "",
    var min: String? = "",
    var distance: String? = ""
)
data class FCMSend(
    var to: String? = "",
    var notification: NotificationContent? = null,
    var data: Data? = null,
    var ttl: String? = "4500s"
)