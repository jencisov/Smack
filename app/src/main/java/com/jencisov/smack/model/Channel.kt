package com.jencisov.smack.model;

class Channel(val name: String, val description: String, val id: String) {
    override fun toString() = "#$name"
}