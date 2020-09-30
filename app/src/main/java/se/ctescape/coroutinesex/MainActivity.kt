package se.ctescape.coroutinesex

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private val RESULT_1 = "Result #1"

    // Coroutine är ett job i en Thread (det kan finnas flera)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        button.setOnClickListener {
            //IO för Databaser, nätverk mm, Main är för huvudtråden, Default är för tunggung
            CoroutineScope(IO).launch {
                fakeApiRequest()
            }
        }
    }

    private suspend fun fakeApiRequest(){
        val result1 = getResult1FromApi()
        println("AIK!!  $result1")
    }

    //Suspend betyder att den KAN köras i bakrunden
    private suspend fun getResult1FromApi(): String{
        logThread("getResult1FromApi")
        delay(1000) //Kommer bara att stoppa coroutenen inte hela tråden..
        return RESULT_1
    }

    private fun logThread(methodName: String){
        Log.d("AIK!!", "$methodName: ${Thread.currentThread().name}")
    }
}