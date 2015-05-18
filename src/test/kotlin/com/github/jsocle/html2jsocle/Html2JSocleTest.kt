package com.github.jsocle.html2jsocle

import org.junit.Assert
import org.junit.Test

class Html2JSocleTest {
    Test
    fun testElement() {
        Assert.assertEquals("H1(text_=\"Hello world!\")", convert("<h1>Hello world!</h1>"))
    }
}

