package com.example.moengageassignment.activity

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moengageassignment.R
import com.example.moengageassignment.adapter.NewsAdapter
import com.example.moengageassignment.extension.showErrorMessage
import com.example.moengageassignment.internet.CheckNetwork
import com.example.moengageassignment.model.Article
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {

  private lateinit var newsAdapter:NewsAdapter
  private var newsList:ArrayList<Article> = ArrayList()
  private lateinit var recyclerViewNews:RecyclerView
  private var floatingActionButton:FloatingActionButton?=null
  private lateinit var progressDialog:ProgressDialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setUI()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setUI(){
        newsList=ArrayList<Article>()
        recyclerViewNews=findViewById(R.id.recyclerViewNews);
        floatingActionButton=findViewById(R.id.floatingActionButton)
        recyclerViewNews.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        getData()
        onFabClickListener()
    }

    //Method for displaying the Data from API
    private fun getData(){
        val fetchData=FetchData()
        floatingActionButton?.let {button->
            val checkNetwork = CheckNetwork()
            if (checkNetwork.isNetworkAvailable(this)) {
                fetchData.execute("https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json")
                button.visibility=View.VISIBLE
            } else {
                button.visibility=View.GONE
                showErrorMessage(getString(R.string.str_checkInternet))
            }
        }
    }

    //Async Class for Calling the API
    private inner class FetchData:AsyncTask<String,Void,String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog=ProgressDialog(this@HomeActivity)
            progressDialog.setMessage("Loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg string: String): String? {
            val urlString=string[0]
            var urlConnection:HttpURLConnection?=null
            var reader:BufferedReader?=null
            var result:String?=null

            try {
                val url=URL(urlString)
                urlConnection=url.openConnection() as HttpURLConnection
                urlConnection.requestMethod="GET"
                urlConnection.connect()

                val inputStream:InputStream=urlConnection.inputStream
                val buffer=StringBuffer()
                if (inputStream==null){
                    return  null
                }
                reader= BufferedReader(InputStreamReader(inputStream))

                var line:String?
                while (reader.readLine().also { line = it } != null) {
                    buffer.append(line).append("\n")
                }

                if (buffer.isEmpty())
                {
                    return null
                }
                result=buffer.toString()
            }
            catch (e: IOException){
               e.printStackTrace()
            }
            finally {
                 urlConnection?.disconnect()
                if (reader!=null){
                    try {
                        reader.close()
                    }
                    catch (e:IOException){
                        e.printStackTrace()
                    }
                }
            }
              return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            if (result!=null){
                try {
                    val jsonObject=JSONObject(result)
                    val status=jsonObject.getString("status")
                    val jsonArrayArticles=jsonObject.getJSONArray("articles")
                    val articles= mutableListOf<Article>()
                    for (i in 0 until jsonArrayArticles.length()){
                        val articleObject=jsonArrayArticles.getJSONObject(i)
                        val title=articleObject.getString("title")
                        val author=articleObject.getString("author")
                        val image=articleObject.getString("urlToImage")
                        val publishedAt=articleObject.getString("publishedAt")
                        val url=articleObject.getString("url")
                        val description=articleObject.getString("description")
                        val content=articleObject.getString("content")
                        val article=Article(
                            author=author,title=title,description=description,
                            url=url,urlToImage=image, publishedAt = publishedAt,content=content
                        )
                        articles.add(article)
                    }
                    newsList.addAll(articles)
                    newsAdapter= NewsAdapter(this@HomeActivity,newsList)
                    recyclerViewNews.adapter=newsAdapter

                    //Passing the data through intent for getting displayed at Browser
                    newsAdapter.setOnNewsClickListener {
                            val intent=Intent(Intent.ACTION_VIEW,Uri.parse(it.url))
                            startActivity(intent)
                    }
                }
                catch (e:JSONException){
                    e.printStackTrace()
                }
            } else{
                showErrorMessage(getString(R.string.str_emptyData))
            }
            Log.d("TAG",result.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    //Bottom Sheet UI for displaying for sorting the news by time.
    private fun onFabClickListener(){
        floatingActionButton?.setOnClickListener {
            val bottomSheetDialog=BottomSheetDialog(this@HomeActivity,R.style.MyBottomSheetDialog)
            val bottomView=LayoutInflater.from(this).inflate(R.layout.layout_bottomsheetsort,findViewById(R.id.linearLayoutBottomSheet))
            val radioGroup=bottomView.findViewById<RadioGroup>(R.id.radioGroup)
            val radioButtonOld=bottomView.findViewById<RadioButton>(R.id.radioButtonOld)
            val radioButtonLatest=bottomView.findViewById<RadioButton>(R.id.radioButtonLatest)
            val textViewTitle=bottomView.findViewById<TextView>(R.id.textViewTitle)
            val typeface = Typeface.createFromAsset(assets, "fonts/recursive_regular.ttf")
            val titleTypeface=Typeface.createFromAsset(assets,"fonts/recursive_extrabold.ttf")
            radioButtonOld.typeface=typeface
            radioButtonLatest.typeface=typeface
            textViewTitle.typeface=titleTypeface
            bottomSheetDialog.setContentView(bottomView)
            bottomSheetDialog.show()

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when(radioGroup.checkedRadioButtonId){
                    R.id.radioButtonOld->{
                            radioButtonOld.isChecked = true
                            radioButtonLatest.isChecked = false
                        //Getting the old newslist from start
                            newsList.sortWith(compareBy {
                                it.publishedAt?.let { it1 ->
                                    SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                        Locale.getDefault()
                                    ).parse(it1)
                                }
                            })
                            newsAdapter.notifyDataSetChanged()
                            recyclerViewNews.scrollToPosition(0)
                            bottomSheetDialog.dismiss()
                    }

                    R.id.radioButtonLatest->{
                            radioButtonLatest.isChecked = true
                            radioButtonOld.isChecked = false
                        //Getting the latest newslist from start
                            newsList.sortWith(compareByDescending {
                                it.publishedAt?.let { it1 ->
                                    SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                        Locale.getDefault()
                                    ).parse(it1)
                                }
                            })
                            newsAdapter.notifyDataSetChanged()
                            recyclerViewNews.scrollToPosition(0)
                            bottomSheetDialog.dismiss()
                    }
                }
            }
        }
    }
}