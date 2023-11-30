package kenharpe.iu.project10


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Intent called to start SensorActivity")
        val intent = Intent(this, SensorActivity::class.java)
        startActivity(intent)
        finish()
    }
}