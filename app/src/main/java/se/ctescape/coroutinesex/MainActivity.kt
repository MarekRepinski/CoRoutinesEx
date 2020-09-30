package se.ctescape.coroutinesex

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var text: TextView
    private lateinit var jobButton: Button
    private lateinit var job: CompletableJob
    private val RESULT_1 = "Result #1"
    private val RESULT_2 = "Result #2"
    private val RESULT_3 = "Result #3"
    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000 //ms

    // Coroutine är ett job i en Thread (det kan finnas flera)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        jobButton = findViewById(R.id.job_button)
        text = findViewById(R.id.textView)

        jobButton.setOnClickListener {
            if(!::job.isInitialized){
                initJob()
            }
            job_progress_bar.startJobOrCancel(job)
        }

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

    fun ProgressBar.startJobOrCancel(job: Job){
        if (this.progress > 0){
            println("AIK!! This $job is already active. Cancelling...")
            resetJob()

        } else {
            jobButton.text = "Cancel job #1"
            //IO + job skapar ett nytt unikt context
            CoroutineScope(IO + job).launch{
                println("AIK!! Coroutine $this is activated on job $job")
                for(i in PROGRESS_START..PROGRESS_MAX){
                    delay((JOB_TIME/PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                setTextOnMainThread("Job #1 completed")
            }
        }
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted){
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    private fun initJob(){
        jobButton.text = "Start Job #1"
        setTextOnMainThread(" ")
        job = Job()
        // När jobbet avslutas kommer koden nedan att köras (även vid cancel)
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank()){
                    msg = "Unknown cancellation error."
                }
                println("AIK!! $job was cancelled. Reason: $msg")
                showToast(msg)
            }
        }
        job_progress_bar.max = PROGRESS_MAX
        job_progress_bar.progress = PROGRESS_START
    }

    fun showToast(msg: String){
        GlobalScope.launch(Main) {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setNewText(inStr: String){
        val newText = text.text.toString() + "\n$inStr"
        text.text = newText
    }

    private fun setTextOnMainThread(inStr: String){
        // detta händer på huvudtråden:
//        withContext(Main){//Funkar bara från coroutines
        GlobalScope.launch(Main){//Körs alltid i huvudtråden
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