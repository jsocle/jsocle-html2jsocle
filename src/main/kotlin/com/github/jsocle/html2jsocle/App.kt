package com.github.jsocle.html2jsocle

import com.github.jsocle.JSocle
import com.github.jsocle.form.BooleanField
import com.github.jsocle.form.Form
import com.github.jsocle.form.TextareaField
import com.github.jsocle.html.elements.Html
import com.github.jsocle.requests.handlers.RequestHandler0

public object app : JSocle() {
    private val index: RequestHandler0<Html> = route("/") { ->
        val form = object : Form() {
            val html by TextareaField()
            val includeBody by BooleanField()
        }
        val kt =
                if (form.html.value?.length() ?: 0 > 0) convert(form.html.value!!, form.includeBody.value ?: false)
                else ""
        Html {
            body {
                form(method = "post", action = index.url()) {
                    +form.html.render()
                    +form.includeBody.render()
                    button(text_ = "convert")
                    pre(text_ = kt)
                }
            }
        }
    }

}

fun main(args: Array<String>) {
    app.run(config.port)
}
