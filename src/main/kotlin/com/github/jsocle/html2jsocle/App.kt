package com.github.jsocle.html2jsocle

import com.github.jsocle.JSocle
import com.github.jsocle.html.elements.Html
import com.github.jsocle.request

public class App : JSocle() {
    init {
        route("/") { ->
            val html = request.parameter("html")?.trim() ?: ""
            val includeBody = request.parameter("includeBody") != null
            val kt = if (html.length() == 0) "" else {
                convert(html, includeBody = includeBody)
            }
            return@route Html {
                body {
                    form(method = "post") {
                        textarea(name = "html", text_ = html)
                        input(type = "checkbox", name = "includeBody", value = "includeBody", checked = if (includeBody) "true" else null)
                        button(text_ = "convert")
                        pre(text_ = kt)
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    App().run(config.port)
}
