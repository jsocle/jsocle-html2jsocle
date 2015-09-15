package com.github.jsocle.html2jsocle

import com.github.jsocle.JSocle
import com.github.jsocle.html.elements.Html
import com.github.jsocle.html.elements.Label
import com.github.jsocle.request
import com.github.jsocle.requests.handlers.RequestHandler0

public class App : JSocle() {
    val index: RequestHandler0<Html> = route("/") { ->

        val html = request.parameter("html")?.trim() ?: ""

        var language = request.parameter("language")
        if(language == null) language = "kotlin"

        val includeBody = request.parameter("includeBody") != null
        val kt = if (html.length() == 0) "" else if (language == "kotlin") convert(html, includeBody = includeBody) else convertJava(html, includeBody = includeBody)

        return@route Html {
            body {
                form(method = "post", action = index.url()) {
                    input(type = "radio", name = "language", value = "kotlin", checked = if (language == "kotlin") "true" else null)
                    +Label("Kotlin")
                    input(type = "radio", name = "language", value = "java", checked = if (language == "java") "true" else null)
                    +Label("Java")
                    br()
                    textarea(name = "html", text_ = html, style = "width:500px; height:400px")
                    input(type = "checkbox", name = "includeBody", value = "includeBody", checked = if (includeBody) "true" else null)
                    button(text_ = "convert")
                    pre(text_ = kt)
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    App().run(config.port)
}
