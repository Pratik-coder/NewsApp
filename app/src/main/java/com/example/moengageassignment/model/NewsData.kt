package com.example.moengageassignment.model

//Data class for holding the data
data class NewsData(
    val status:String,
    val articles:List<Article>?=null
)

data class Article(
    val source:SourceData?=null,
    var author:String?=null,
    var title:String?=null,
    val description:String?=null,
    val url:String?=null,
    var urlToImage:String?=null,
    val publishedAt:String?=null,
    val content:String?=null,
)

data class SourceData(
    val id:String,
    val name:String,
)
