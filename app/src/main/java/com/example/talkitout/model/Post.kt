package com.example.talkitout.model

class Post {
    private  var postid: String = ""
    private  var postimage: String = ""
    private  var publisher: String = ""
    private  var description: String = ""
    private  var categoryName: String = ""


    constructor()


    constructor(postid: String, postimage: String, publisher: String, description: String, categoryName: String) {
        this.postid = postid
        this.postimage = postimage
        this.publisher = publisher
        this.description = description
        this.categoryName = categoryName
    }


    fun getPostid(): String{
        return postid
    }

    fun getPostimage(): String{
        return postimage
    }

    fun getPublisher(): String{
        return publisher
    }

    fun getDescription(): String{
        return description
    }
    fun getCategory(): String{
        return categoryName
    }

    fun setPostid(postid: String)
    {
        this.postid = postid
    }

    fun setPostimage(postimage: String)
    {
        this.postimage = postimage
    }

    fun setPublisher(publisher: String)
    {
        this.publisher = publisher
    }

    fun setDescription(description: String)
    {
        this.description = description
    }
    fun setCategory(categoryName: String)
    {
        this.categoryName = categoryName
    }


}