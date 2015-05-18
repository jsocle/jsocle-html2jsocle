package com.github.jsocle.html2jsocle

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import kotlin.dom.*

class JScoleHtmlElement(private val element: Element) {
    private val tagName: String = element.getTagName().replaceAll("^[a-z]") { it.group().toUpperCase() }
    private val attrs: Map<String, String>
    private val children: List<Node>

    init {
        var singleTextNode = false
        if (element.children().size() == 1) {
            if (element.children()[0].isText()) {
                singleTextNode = true
            }
        }

        if (singleTextNode) {
            children = listOf()
        } else {
            children = element.children()
        }

        val attrs = hashMapOf<String, String>()
        if (singleTextNode) {
            attrs["text_"] = element.children()[0].text
        }
        this.attrs = attrs
    }

    fun render(): String {
        var response = ""
        response += tagName
        response += attrs.toList().map { "${it.first}=\"${it.second}\"" }.join(separator = " ", prefix = "(", postfix = ")")
        return response
    }
}

fun convert(source: String): String {
    val document = parseXml(ByteArrayInputStream(source.toByteArray()))
    return document.elements.map { JScoleHtmlElement(it).render() }.join("\n")
}