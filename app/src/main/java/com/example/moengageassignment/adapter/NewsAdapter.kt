package com.example.moengageassignment.adapter

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moengageassignment.R
import com.example.moengageassignment.activity.HomeActivity
import com.example.moengageassignment.model.Article
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewsAdapter(val context:Context, var newsList:List<Article> =ArrayList()): RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.layout_newslist,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsData=newsList[position]
        holder.textViewTitle.text=newsData.title
        val typeface = Typeface.createFromAsset(context.assets, "fonts/recursive_extrabold.ttf")
        val dateTypeface = Typeface.createFromAsset(context.assets, "fonts/recursive_regular.ttf")
        holder.textViewTitle.typeface = typeface
        Glide.with(context).load(newsData.urlToImage).into(holder.imageViewNews)
        val publishedDate=newsData.publishedAt
        val inputFormat=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat=SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        try {
            val date=inputFormat.parse(publishedDate)
            val formattedDate=outputFormat.format(date)
            holder.textViewTitle.typeface = dateTypeface
            holder.textViewPublishedDate.text="Published on:"+ formattedDate
        }
        catch (e:Exception){
            e.printStackTrace()
        }
        holder.cardViewNews.setOnClickListener{
           onItemNewsClickListener?.let { it(newsData) }
            Log.d("TAG",newsList[position].toString())
        }
    }

    override fun getItemCount(): Int {
     return newsList.size
    }


    private var onItemNewsClickListener:((Article)->Unit)?=null


    fun setOnNewsClickListener(listener: (Article)->Unit){
       onItemNewsClickListener=listener
    }



    class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
        val imageViewNews:ImageView=view.findViewById(R.id.imageViewNews)
        val textViewTitle:TextView=view.findViewById(R.id.textViewTitle)
        val textViewPublishedDate:TextView=view.findViewById(R.id.textViewPublishedDate)
        val cardViewNews:CardView=view.findViewById(R.id.cardViewNews)
    }
}
