package com.github.jsocle.html2jsocle

import org.jsoup.Jsoup
import org.jsoup.nodes
import org.jsoup.nodes.Comment
import org.jsoup.nodes.DocumentType
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.util.*

val defNames = ArrayList<String>()

open class JSocleJavaHtmlElement(private val parentElement: String = "", private val element: nodes.Node, private val depth: Int = 0) {
    private val tagName: String = element.nodeName().replaceAll("^[a-z]") { it.group().toUpperCase() }
    private val attrs: Map<String, String>
    private val children: List<Node>
    private val data = element.attributes().filter { it.key.startsWith("data-") }.map { it.key to it.value }.toMap()

    var defName: String = (tagName.charAt(0) + 32).toChar() + tagName.substring(1) + "_0"

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
                !it.isText() || (it as TextNode).text().replaceAll("\\r?\\n", "").trim().isNotEmpty()
            }
        }

        val attrs = linkedMapOf<String, String>()
        element.attributes().filter { !it.key.startsWith("data-") }.forEach { attrs[escape(it.key)] = it.value }
        if (singleTextNode) {
            attrs["text_"] = (element.childNodes()[0] as TextNode).text()
        }
        this.attrs = attrs

        var count: Int = 0
        defNames.forEach {
            if (it.substring(0, it.lastIndexOf("_") + 1) == defName.substring(0, defName.lastIndexOf("_") + 1)) {
                count++
            }
        }
        if (count != 0) {
            defName = defName.substring(0, defName.lastIndexOf("_") + 1) + count
        }
        defNames.add(defName)

    }

    open fun render(): String {
        var response = StringBuilder()

        response.append("$tagName $defName = new $tagName();")

        attrs.forEach {
            if (it.getKey().endsWith("_")) {
                var text_ = it.getValue()
                response.append("\n")
                response.append("$defName.addNode(\"${text_}\");")
                return@forEach
            }
        }

        if (attrs.size() > 0) {
            response.append(
                    attrs.toList()
                            .filter {
                                if (it.first.endsWith("_")) false
                                else true
                            }
                            .map {
                                "$defName.set${(it.first.charAt(0) - 32).toChar() + it.first.substring(1)}(\"${it.second}\");"
                            }
                            .join(separator = "\n", prefix = "\n", postfix = "\n")
            )
        }

        if (data.isNotEmpty() || children.isNotEmpty()) {
            response.append("\n")
            if (data.isNotEmpty()) {
                val dataBody = data
                        .map { "data_[\"${deHyphen(it.key.substring(5))}\"] = \"${it.value}\"" }
                        .join("\n")
                response.append(dataBody)
                response.append("\n")
            }
            if (children.isNotEmpty()) {
                val childrenBody = children
                        .map {
                            when (it) {
                                is TextNode -> JSocleJavaHtmlTextElement(defName, it, depth + 1).render()
                                //is Comment -> JSocleJavaHtmlComment(it, depth + 1).render()
                                else -> JSocleJavaHtmlElement(defName, it, depth + 1).render()
                            }
                        }
                        .join("\n")
                response.append(childrenBody)
                response.append("\n")
            }
        }

        if (attrs.size() == 0 && !(data.isNotEmpty() || children.isNotEmpty())) {
            response.append("()")
        }

        if (parentElement != "") {
            response.append("${parentElement}.addNode(${defName});\n")
        }

        return response.toString()
    }
}

private class JSocleJavaHtmlTextElement(private val parentElement: String, private val element: TextNode, depth: Int = 0) : JSocleHtmlElement(element, depth) {
    override fun render(): String {
        return "${parentElement}.addNode(new Label(\"${element.text().trim()}\"));\n"
    }
}

private class JSocleJavaHtmlComment(private val element: Comment, depth: Int = 0) : JSocleHtmlElement(element, depth) {
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

fun convertJava(source: String, includeBody: Boolean = false): String {
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
            .map { JSocleJavaHtmlElement("", it).render() }.join("\n")
}