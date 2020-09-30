package se.ctescape.coroutinesex

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var text: TextView
    private val RESULT_1 = "Result #1"
    private val RESULT_2 = "Result #2"
    private val RESULT_3 = "Result #3"

    // Coroutine är ett job i en Thread (det kan finnas flera)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        text = findViewById(R.id.textView)
        button.setOnClickListener {
            //IO för Databaser, nätverk mm, Main är för huvudtråden, Default är för tunggung
            CoroutineScope(IO).launch {
                //coroutine 1 och 2 startar efter varandra
                fakeApiRequest()
            }
            CoroutineScope(IO).launch {
                //coroutine 3 startar samtidigt som 1 och blir färdig för coroutine 2
                setTextOnMainThread(getResult3FromApi())
            }
        }
    }

    private fun setNewText(inStr: String){
        val newText = text.text.toString() + "\n$inStr"
        text.text = newText
    }

    private suspend fun setTextOnMainThread(inStr: String){
        // detta händer på huvudtråden:
        withContext(Main){
            setNewText(inStr)
        }
        //Coroutines kan på så sätt hoppa mellan trådar!!!
    }

    private suspend fun fakeApiRequest(){
        setTextOnMainThread(getResult1FromApi())
        setTextOnMainThread(getResult2FromApi())
    }

    //Suspend betyder att den KAN köras i bakrunden
    private suspend fun getResult1FromApi(): String{
        logThread("getResult1FromApi")
        delay(1000) //Kommer bara att stoppa coroutenen inte hela tråden..
        return RESULT_1
    }

    private suspend fun getResult2FromApi(): String{
        logThread("getResult2FromApi")
        delay(4000) //Kommer bara att stoppa coroutenen inte hela tråden..
        return RESULT_2
    }

    private suspend fun getResult3FromApi(): String{
        logThread("getResult3FromApi")
        delay(3000) //Kommer bara att stoppa coroutenen inte hela tråden..
        return RESULT_3
    }

    private fun logThread(methodName: String){
        Log.d("AIK!!", "$methodName: ${Thread.currentThread().name}")
    }
}