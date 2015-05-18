package com.github.jsocle.html2jsocle

import org.junit.Assert
import org.junit.Test

class Html2JSocleTest {
    Test
    fun testElement() {
        Assert.assertEquals("H1(text_=\"Hello world!\")", convert("<h1>Hello world!</h1>"))
    }

    Test
    fun testNested() {
        val html = """
<body>
    <h1>Hello world!</h1>
</body>
"""
        val kt = """Body {
    h1(text_="Hello world!")
}"""
        Assert.assertEquals(kt, convert(html))
    }

    Test
    fun testNestedMultiples() {
        val html = """
<head>
    <link type="text/css" rel="stylesheet" href="/site.css"/>
    <title>Hello</title>
</head>
"""
        val kt = """Head {
    link(type="text/css" rel="stylesheet" href="/site.css")
    title(text_="Hello")
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
        link(type="text/css" rel="stylesheet" href="/site.css")
        title(text_="Hello")
    }
    body {
        h1(text_="Hello world!")
    }
}"""
        Assert.assertEquals(kt, convert(html))
    }
}

