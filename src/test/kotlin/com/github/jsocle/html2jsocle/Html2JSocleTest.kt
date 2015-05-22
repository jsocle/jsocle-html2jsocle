package com.github.jsocle.html2jsocle

import org.junit.Assert
import org.junit.Test

class Html2JSocleTest {
    Test
    fun testElement() {
        Assert.assertEquals("H1(text_ = \"Hello world!\")", convert("<h1>Hello world!</h1>"))
    }

    Test
    fun testEmptyElement() {
        Assert.assertEquals("Img(src = \"../../favicon.ico\")", convert("<img src=\"../../favicon.ico\">"))
    }

    Test
    fun testNested() {
        val html = """
<div>
    <h1>Hello world!</h1>
</div>
"""
        val kt = """Div {
    h1(text_ = "Hello world!")
}"""
        Assert.assertEquals(kt, convert(html))
    }

    Test
    fun testNestedMultiples() {
        val html = """
<div>
    <input type="text" name="user-name"/>
    <p>Hello</p>
</div>
"""
        val kt = """Div {
    input(type = "text", name = "user-name")
    p(text_ = "Hello")
}"""
        Assert.assertEquals(kt, convert(html))
    }

    Test
    fun testHtml() {
        val html = """
<html>

<head>
    <link type="text/css" rel="stylesheet" href="/site.css"/>
    <title>Hello</title>
</head>

<body>
<h1>Hello world!</h1>
</body>

</html>
"""
        val kt = """Html {
    head {
        link(type = "text/css", rel = "stylesheet", href = "/site.css")
        title(text_ = "Hello")
    }
    body {
        h1(text_ = "Hello world!")
    }
}"""
        Assert.assertEquals(kt, convert(html, includeBody = true))
    }

    Test
    fun testTextPlus() {
        val html = """
<div>
    message
    <!-- comment -->
</div>
"""
        val kt = """Div {
    +"message"
    // comment
}"""
        Assert.assertEquals(kt, convert(html))
    }

    Test
    fun testHtmlMeta() {
        val html = """
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
  </head>
  <body></body>
</html>
"""
        val kt = """Html(lang = "en") {
    head {
        meta(charset = "utf-8")
        meta(httpEquiv = "X-UA-Compatible", content = "IE=edge")
    }
    body()
}"""
        Assert.assertEquals(kt, convert(html, includeBody = true))
    }

    Test
    fun testRemoveDocType() {
        val html = """<!DOCTYPE html><html lang="en"></html>"""
        val kt = """Html(lang = "en") {
    head()
    body()
}"""

        Assert.assertEquals(kt, convert(html, includeBody = true))
    }

    Test
    fun testClassAttribute() {
        val html = """<div class="col-md-12"></div>"""
        val kt = """Div(class_ = "col-md-12")"""
        Assert.assertEquals(kt, convert(html))
    }

    Test
    fun testHyphenAttribute() {
        val html = """<div http-equiv="X-UA-Compatible" content = "IE=edge"></div>"""
        val kt = """Div(httpEquiv = "X-UA-Compatible", content = "IE=edge")"""
        Assert.assertEquals(kt, convert(html))
    }
}

