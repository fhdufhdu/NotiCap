package com.fhdufhdu.noticap.util

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CoroutineManager {
    companion object {
        @OptIn(DelicateCoroutinesApi::class)
        fun <T> run(function: suspend () -> T) {
            GlobalScope.launch(Dispatchers.IO) {
                function()
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun <T> runUI(function: suspend () -> T) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    function()
                }
            }
        }

        fun <T> runSync(function: () -> T): T {
            var result: T
            runBlocking {
                result = GlobalScope.async(Dispatchers.IO) {
                    function()
                }.await()
            }
            return result
        }
    }
}
