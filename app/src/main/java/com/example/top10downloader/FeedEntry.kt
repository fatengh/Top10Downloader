package com.example.top10downloader

class FeedEntry {
    var name: String = ""
    override fun toString(): String {
        return """
            name = $name
           """.trimIndent()
    }

}