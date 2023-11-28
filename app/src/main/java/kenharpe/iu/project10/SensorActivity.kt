package kenharpe.iu.project10

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kenharpe.iu.project10.ui.theme.Project10Theme
import java.util.Locale

class SensorActivity : ComponentActivity()
{
    val viewModel : GlobalViewModel by viewModels()

    private var sensorManager : SensorManager? = null
    private var lmManager : LocationManager? = null
    private var geocoder : Geocoder? = null


    private var amTempSensor : Sensor? = null
    private var tempListener : SensorEventListener? = null
    private var pressureSensor : Sensor? = null
    private var pressureListener : SensorEventListener? = null



    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lmManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(this, Locale.getDefault())

        getLocationInfo()

        if (sensorManager != null)
        {
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null)
            {
                amTempSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

                tempListener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
                    override fun onSensorChanged(event: SensorEvent)
                    {
                        viewModel.setTemp(event.values[0])
                    }
                }
                sensorManager!!.registerListener(tempListener, amTempSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }

            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE) != null)
            {
                pressureSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)

                pressureListener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
                    override fun onSensorChanged(event: SensorEvent)
                    {
                        viewModel.setPressure(event.values[0])
                    }
                }
                sensorManager!!.registerListener(pressureListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        setContent{
            Project10Theme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SensorsPlayground(viewModel = viewModel)
                }
            }
        }
    }

    private fun getLocationInfo()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            val locationListener = LocationListener { location ->
                val addresses = geocoder!!.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull()
                address?.locality?.let { viewModel.setCity(it) }
                address?.adminArea?.let { viewModel.setState(it) }
            }
            lmManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

}

@Composable
fun SensorsPlayground(viewModel: GlobalViewModel)
{
    val context = LocalContext.current
    Column(Modifier.width(IntrinsicSize.Max)){
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 20.dp), contentAlignment = Alignment.Center){
            Text(text = "Sensors Playground", textAlign = TextAlign.Center, fontSize = 25.sp)
        }

        val city = viewModel.city.observeAsState()
        val state = viewModel.state.observeAsState()
        Text(text="Location: ", color = MaterialTheme.colorScheme.primary)
        Text(text="City: ${city.value}", color = MaterialTheme.colorScheme.primary)
        Text(text="State: ${state.value}", color = MaterialTheme.colorScheme.primary)

        val tempState = viewModel.temp.observeAsState()
        val pressure = viewModel.pressure.observeAsState()
        Text(text="Temperature: ${tempState.value}f", color = MaterialTheme.colorScheme.primary, modifier= Modifier.padding(0.dp,20.dp))
        Text(text="Air Pressure: ${pressure.value}", color = MaterialTheme.colorScheme.primary)

        Box(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 40.dp, 0.dp, 0.dp),
            contentAlignment = Alignment.Center)
        {
            FlingButton(onFling = {
                val intent = Intent(context, GestureActivity::class.java)
                context.startActivity(intent)
            })
        }
    }
}

@Composable
fun FlingButton(onFling: () -> Unit)
{
    var difference by remember { mutableStateOf(Offset.Zero)}

    val modifier = Modifier.pointerInput(Unit){
        detectDragGestures { change, dragAmount ->
            difference += dragAmount
            val threshold = 10
            if (difference.x > threshold)
            {
                onFling()
            }
        }
    }
    Button(onClick = {}, modifier = modifier)  {
        Text(text = "Gesture Playground")
    }
}

@Preview(showBackground = true)
@Composable
fun SensorsPlaygroundPreview() {
    Project10Theme {
        SensorsPlayground(GlobalViewModel())
    }
}