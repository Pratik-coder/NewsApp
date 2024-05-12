package com.example.moengageassignment.extension

import android.content.Context
import android.widget.Toast

fun Context.showErrorMessage(message:String){
    Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}