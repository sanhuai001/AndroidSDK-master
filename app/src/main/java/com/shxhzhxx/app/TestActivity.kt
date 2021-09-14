package com.shxhzhxx.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.shxhzhxx.sdk.activity.CoroutineActivity
import com.shxhzhxx.sdk.net
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TestActivity : AppCompatActivity(), CoroutineScope {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TestActivity", "onCreate")
        setResult(Activity.RESULT_OK)

        launch {
            net.postCoroutine<Config>(api2, onResponse = { msg, data ->
                println("onResponse  $msg   $data")
            }, onFailure = { errno, msg, data ->
                println("onFailure   $errno   $msg   $data")
            })
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("TestActivity", "onNewIntent")
    }

    override val coroutineContext: CoroutineContext
        get() = vm.coroutineContext
    private val vm by lazy {
        ViewModelProviders.of(this).get(CoroutineActivity.MyViewModel::class.java)
    }
}