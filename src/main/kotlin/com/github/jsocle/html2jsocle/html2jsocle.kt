package com.github.jsocle.html2jsocle

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import kotlin.dom.*

class JSocleHtmlElement(private val element: Element, private val depth: Int = 0) {
    private val indent = (1..depth).map { "    " } .join(separator = "")
    private val tagName: String =
            if (depth == 0) element.getTagName().replaceAll("^[a-z]") { it.group().toUpperCase() }
            else element.getTagName()
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
            children = element.children().filter { !it.isText() || it.text.trim().isNotEmpty() }
        }

        val attrs = linkedMapOf<String, String>()
        val namedNodeMap = element.getAttributes()
        for (i in (namedNodeMap.length - 1) downTo 0) {
            val node = namedNodeMap.item(i)
            attrs[node.nodeName] = node.text
        }
        if (singleTextNode) {
            attrs["text_"] = element.children()[0].text
        }
        this.attrs = attrs
    }


    fun render(): String {
        var response = StringBuilder()
        response.append("$indent$tagName")

        if (attrs.size() > 0) {
            response.append(
                    attrs.toList()
                            .map { "${it.first}=\"${it.second}\"" }
                            .join(separator = " ", prefix = "(", postfix = ")")
            )
        }

        if (children.size() > 0) {
            response.append(" {\n")
            val childrenBody = children
                    .map {
                        if (it.isText()) {
                            return@map it.text
                        }
                        when (it) {
                            is Element -> JSocleHtmlElement(it, depth + 1).render()
                            else -> throw IllegalArgumentException("" + it)
                        }
                    }
                    .join("\n")
            response.append(childrenBody)
            response.append("\n")
            response.append("$indent}")
        }

        return response.toString()
    }
}

fun convert(source: String): String {
    val document = parseXml(ByteArrayInputStream(source.toByteArray()))
    return JSocleHtmlElement(document.documentElement!!).render()
}