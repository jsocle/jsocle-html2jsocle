package com.github.jsocle.html2jsocle

import com.google.common.base.CaseFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Comment
import org.jsoup.nodes.DocumentType
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

val words = arrayListOf("class")

fun deHyphen(str: String): String {
    return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, str)
}

fun escape(name: String): String {
    return deHyphen(if (name !in words) name else name + "_")
}

open class JSocleHtmlElement(private val element: Node, private val depth: Int = 0) {
    private val defaultIndent = "    "
    protected val indent: String = (1..depth).map { defaultIndent }.join(separator = "")
    private val tagName: String =
            if (depth == 0) element.nodeName().replace("^[a-z]".toRegex()) { it.value }
            else element.nodeName()
    private val attrs: Map<String, String>
    private val children: List<Node>
    private val data = element.attributes().filter { it.key.startsWith("data-") }.map { it.key to it.value }.toMap()

    init {
        var singleTextNode = false
        if (element.childNodes().size() == 1) {
            if (element.childNodes()[0].isText()) {
                singleTextNode = true
            }
        }

        if (singleTextNode) {
            children = listOf()
        } else {
            children = element.childNodes().filter {
                !it.isText() || (it as TextNode).text().replace("\\r?\\n", "").trim().isNotEmpty()
            }
        }

        val attrs = linkedMapOf<String, String>()
        element.attributes().filter { !it.key.startsWith("data-") }.forEach { attrs[escape(it.key)] = it.value }
        if (singleTextNode) {
            attrs["text_"] = (element.childNodes()[0] as TextNode).text()
        }
        this.attrs = attrs
    }


    open fun render(): String {
        var response = StringBuilder()
        response.append("$indent$tagName")

        if (attrs.size() > 0) {
            response.append(
                    attrs.toList()
                            .map { "${it.first} = \"${it.second}\"" }
                            .join(separator = ", ", prefix = "(", postfix = ")")
            )
        }

        if (data.isNotEmpty() || children.isNotEmpty()) {
            response.append(" {\n")
            if (data.isNotEmpty()) {
                val dataBody = data
                        .map { "$indent${defaultIndent}data_[\"${deHyphen(it.key.substring(5))}\"] = \"${it.value}\"" }
                        .join("\n")
                response.append(dataBody)
                response.append("\n")
            }
            if (children.isNotEmpty()) {
                val childrenBody = children
                        .map {
                            when (it) {
                                is TextNode -> JSocleHtmlTextElement(it, depth + 1).render()
                                is Comment -> JSocleHtmlComment(it, depth + 1).render()
                                else -> JSocleHtmlElement(it, depth + 1).render()
                            }
                        }
                        .join("\n")
                response.append(childrenBody)
                response.append("\n")
            }
            response.append("$indent}")
        }

        if (attrs.size() == 0 && !(data.isNotEmpty() || children.isNotEmpty())) {
            response.append("()")
        }

        return response.toString()
    }
}

fun Map<*, *>.isNotEmpty(): Boolean {
    return !isEmpty()
}

private class JSocleHtmlTextElement(private val element: TextNode, depth: Int = 0) : JSocleHtmlElement(element, depth) {
    override fun render(): String {
        return "$indent+\"${element.text().trim()}\""
    }
}

private class JSocleHtmlComment(private val element: Comment, depth: Int = 0) : JSocleHtmlElement(element, depth) {
    override fun render(): String {
        val comment = element.getData()
        if (!comment.contains("\n")) {
            return "$indent// ${comment.trim()}"
        }
        return StringBuilder {
            append("$indent/*\n")
            comment.split('\n').forEach { append("$indent  ${it.trim()}\n") }
            append("$indent*/\n")
        }.toString()
    }
}

internal fun Node.isText(): Boolean {
    return this is TextNode
}

fun convert(source: String, includeBody: Boolean = false): String {
    val document = Jsoup.parse(source)
    val root = if (includeBody) document else document.body()
    return root.childNodes()
            .filter {
                when (it) {
                    is TextNode -> false
                    is Comment -> false
                    is DocumentType -> false
                    else -> true
                }
            }
            .map { JSocleHtmlElement(it).render() }.join("\n")
}