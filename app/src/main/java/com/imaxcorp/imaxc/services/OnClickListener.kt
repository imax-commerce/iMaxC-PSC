package com.imaxcorp.imaxc.services

interface OnClickListener {
    fun onClickEvent(id: String,position: Int, title: String)
}

interface ClickListener {
    fun clickEvent(position: Int)
}