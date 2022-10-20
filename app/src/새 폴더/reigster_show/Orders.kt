package com.example.reigster_show

data class Orders(
    var customerid: String="",
    var deliveryid: String="",
    var orderaddress: String="",
    var orderlib: String="",
    var orderCheck:Boolean=false,
    var ordercomplete:Boolean=false
)