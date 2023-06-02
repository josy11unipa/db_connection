package com.example.db_connection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.db_connection.databinding.ActivityMainBinding
import okhttp3.ResponseBody

import com.google.gson.JsonArray

import retrofit2.Response
import com.google.gson.JsonObject
import okhttp3.Connection
import retrofit2.Call
import retrofit2.Callback
import java.sql.DriverManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var nomeUtente:String
    private lateinit var password:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            Log.i("LOG", "Tasto login premuto")
            nomeUtente = binding.nomeUtente.text.toString()
            password = binding.password.text.toString()
            Log.i("LOG", "DAI CAMPI ---> Username: $nomeUtente - Psw: $password")
            val loginRequestLogin = RequestLogin(username=nomeUtente, password=password)
            Log.i("LOG", "chiamo la fun loginUtente passando: $loginRequestLogin ")
            loginUtente(loginRequestLogin)
            Log.i("LOG", "fine fun loginUtente passando $loginRequestLogin ")
        }
    }

    private fun loginUtente (requestLogin: RequestLogin){

        val query = "select * from persona where username = '${requestLogin.username}' and password = '${requestLogin.password}';"
        Log.i("LOG", "Query creata:$query ")

        ClientNetwork.retrofit.login(query).enqueue(
            object : Callback<JsonObject>{

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.i("onResponse", "Sono dentro la onResponse e l'esito sarà: ${response.isSuccessful}")
                    if (response.isSuccessful) {
                        Log.i("onResponse", "Sono dentro il primo if. dim response: ${(response.body()?.get("queryset") as JsonArray).size()}")
                        if ((response.body()?.get("queryset") as JsonArray).size() == 1) {
                            Log.i("onResponse", "Sono dentro il secondo if. e chiamo la getImageProfilo")
                            getImageProfilo((response.body()?.get("queryset") as JsonArray).get(0) as JsonObject)
                        } else {
                            Toast.makeText(this@MainActivity,"credenziali errate", Toast.LENGTH_LONG).show()
                            //binding.progressBar.visibility = View.GONE
                        }
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    //Toast.makeText(this@MainActivity,"onFailure1", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()


                }
            }
        )
    }

    private fun getImageProfilo(jsonObject: JsonObject){
        val url: String = jsonObject.get("image").asString
        ClientNetwork.retrofit.getAvatar(url).enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful) {
                        var avatar: Bitmap? = null
                        if (response.body()!=null) {
                            avatar = BitmapFactory.decodeStream(response.body()?.byteStream())
                            binding.imageView.setImageBitmap(avatar)
                        }
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@MainActivity,"onFailure2", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
