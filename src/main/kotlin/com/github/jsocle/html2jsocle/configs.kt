package com.github.jsocle.html2jsocle

object Config {
    val port: Int = (System.getenv("PORT") ?: "8080").toInt()
}

