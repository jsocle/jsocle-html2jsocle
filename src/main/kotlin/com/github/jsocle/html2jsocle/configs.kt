package com.github.jsocle.html2jsocle

private class Config {
    public val port: Int = (System.getenv("PORT") ?: "8080").toInt()
}

public val config: Config = Config()

