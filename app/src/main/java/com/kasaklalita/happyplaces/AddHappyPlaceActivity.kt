package com.kasaklalita.happyplaces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_happy_place.*

class AddHappyPlaceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        setSupportActionBar(tbAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tbAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}