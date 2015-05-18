import com.github.jsocle.html.elements.Html

val test = Html {
    head {
        link(type = "text/css", rel = "stylesheet", href = "/site.css")
        title(text_ = "Hello")
    }
    body {
        h1(text_="Hello world!")
    }
}