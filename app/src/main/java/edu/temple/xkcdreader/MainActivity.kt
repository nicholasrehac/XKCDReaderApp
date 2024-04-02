package edu.temple.xkcdreader

import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


class MainActivity : AppCompatActivity() {

    lateinit var comicNumberEditText: EditText
    lateinit var fetchComicButton : Button
    lateinit var titleTextView: TextView
    lateinit var altTextView: TextView
    lateinit var comicImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        comicNumberEditText = findViewById(R.id.comicNumberEditText)
        fetchComicButton = findViewById(R.id.fetchComicButton)
        titleTextView = findViewById(R.id.titleTextView)
        altTextView = findViewById(R.id.altTextView)
        comicImageView = findViewById(R.id.comicImageView)

        fetchComicButton.setOnClickListener{
            lifecycleScope.launch(Dispatchers.Main) {
                fetchComic(comicNumberEditText.text.toString())
            }
        }



        if(intent.action!! == Intent.ACTION_VIEW) {
            val comicId = intent.data?.path!!.substringBeforeLast('/')
            lifecycleScope.launch(Dispatchers.Main) {
                fetchComic(comicId)
            }
        } else {
            if (getSystemService(DomainVerificationManager::class.java)
                    .getDomainVerificationUserState(packageName)
                    ?.hostToStateMap?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_NONE }?.isNotEmpty()!!)
                startActivity(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${packageName}")))
        }

    }

    suspend fun fetchComic(comicId: String) {

        val jsonObject: JSONObject

        withContext(Dispatchers.IO) {
            jsonObject = JSONObject(URL("https://xkcd.com/$comicId/info.0.json")
                .openStream()
                .bufferedReader()
                .readLine())
        }

        titleTextView.text = jsonObject.getString("safe_title")
        altTextView.text = jsonObject.getString("alt")
        comicImageView.contentDescription = jsonObject.getString("transcript")
        Picasso.get().load(jsonObject.getString("img")).into(comicImageView)

    }

}