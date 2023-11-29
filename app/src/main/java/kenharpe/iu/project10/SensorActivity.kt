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

/**
 * <h1> SensorActivity Class </h1>
 * The class extending ComponentActivity responsible for the display of the first screen where sensor information is displayed
 */

class SensorActivity : ComponentActivity()
{
    /**
     * <h2> Member Attributes </h2>
     * <ul>
     *     <li> viewModel: The ViewModel for the app
     *     <li> sensorManager: A SensorManager class object received from Context.SENSOR_SERVICE
     *     <li> lmManager: A LocationManager class object received from Context.LOCATION_SERVICE
     *     <li> geocoder: A Geocoder class object
     *     <li> amTempSensor: A Sensor object specified for Ambient Temperature
     *     <li> tempListener: A SensorEventListener object set to listen to amTempSensor
     *     <li> pressureSensor: A Sensor object specified for Air Pressure
     *     <li> pressureListener: A SensorEventListener object set to listen to pressureSensor
     * </ul>
     */

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

        // Assigns the Manager member attributes
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lmManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(this, Locale.getDefault())

        // Calls the function to get the city and state, if allowed, of the phone
        getLocationInfo()

        // Checks to see that their is a SensorManager object assigned
        if (sensorManager != null)
        {
            // Checks that the phone has an ambient temperature sensor
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null)
            {
                // Assigns amTempSensor to the default ambient temperature sensor of the phone, and assigns a created listener object to it that updates viewModel's temp value
                amTempSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
                tempListener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
                    override fun onSensorChanged(event: SensorEvent) { viewModel.setTemp(event.values[0]) }
                }
                sensorManager!!.registerListener(tempListener, amTempSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }

            // Checks that the phone has an air pressure sensor
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE) != null)
            {
                // Assigns pressureSensor to the default pressure sensor of the phone, and assigns a created listener object to it that updates viewModel's pressure value
                pressureSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)
                pressureListener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
                    override fun onSensorChanged(event: SensorEvent) { viewModel.setPressure(event.values[0]) }
                }
                sensorManager!!.registerListener(pressureListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        // Sets the view contents by calling composable functions
        setContent{
            Project10Theme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SensorsPlayground(viewModel = viewModel)
                }
            }
        }
    }


    /**
     * <h2> getLocationInfo </h2>
     * Method that checks if the app has location permissions, and if so, sets an object of LocationListener to request location updates from
     * lmManager, which updates the city and state value of viewModel when an update to location occurs
     * @return <code>Unit</code>
     */
    private fun getLocationInfo()
    {
        // Checks if the app has permission to access the phone's fine location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // Creates a LocationListener that updates viewModel's state and city values, then register it to receive location updates through lmManager
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
            // Requests permissions to access the necessary information
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

}

/**
 * <h2> SensorsPlayground </h2>
 * Function that generates the UI through Jetpack Compose, and sets updatable elements based on livedata from viewModel
 * @param viewModel the viewModel of the application
 * @return          <code>Unit</code>
 */
@Composable
fun SensorsPlayground(viewModel: GlobalViewModel)
{
    // Gets the current context from LocalContext, so when setting the change activity intent for onFling, context is already stored
    val context = LocalContext.current
    // Forcing the layout to take up the maximum width, ensuring that it will stretch across the screen
    Column(Modifier.width(IntrinsicSize.Max)){
        // Create a title box that's text is centered in the space
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center)
        {
            Text(text = "Sensors Playground", textAlign = TextAlign.Center, fontSize = 25.sp)
        }
        // Creates states out of the livedata in the viewModel (I definitely could have done state hoisting, but considering this screen is not that complex, it seemed like more work than it was worth)
        val city = viewModel.city.observeAsState()
        val state = viewModel.state.observeAsState()
        val tempState = viewModel.temp.observeAsState()
        val pressure = viewModel.pressure.observeAsState()

        // Defines updatable text within the view using the state values assigned above
        Text(text="Location: ", color = MaterialTheme.colorScheme.primary)
        Text(text="City: ${city.value}", color = MaterialTheme.colorScheme.primary)
        Text(text="State: ${state.value}", color = MaterialTheme.colorScheme.primary)
        Text(text="Temperature: ${tempState.value}f", color = MaterialTheme.colorScheme.primary, modifier= Modifier.padding(0.dp,20.dp))
        Text(text="Air Pressure: ${pressure.value}", color = MaterialTheme.colorScheme.primary)

        // Create a button box that ensures the button is centered within the screen, and adds proper spacing through padding
        Box(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 40.dp, 0.dp, 0.dp),
            contentAlignment = Alignment.Center)
        {
            // Calls FlingButton using a lambda expression for onFling that starts the next activity
            FlingButton(onFling = {
                val intent = Intent(context, GestureActivity::class.java)
                context.startActivity(intent)
            })
        }
    }
}

/**
 * <h2> FlingButton </h2>
 * Function that generates the the button for transitioning to the next scene, using the onFling parameter to set what happens when the button detects a fling gesture
 * @param onFling   Lambda expression taking in no parameters that says what to do upon detecting a fling gesture
 * @return          <code>Unit</code>
 */
@Composable
fun FlingButton(onFling: () -> Unit)
{
    // Creates a state variable for the difference between where a drag gesture starts and ends
    var difference by remember { mutableStateOf(Offset.Zero)}

    // Creates a modifier that, using pointerInput, detects a fling gesture and calls on fling
    val modifier = Modifier.pointerInput(Unit)
    {
        detectDragGestures { _, dragAmount ->
            difference += dragAmount
            val threshold = 10
            if (difference.x > threshold)
            {
                onFling()
            }
        }
    }
    // Creates a button with the previously created modifier
    Button(onClick = {}, modifier = modifier)
    {
        Text(text = "Gesture Playground")
    }
}

// Preview function only used in designing the layout
@Preview(showBackground = true)
@Composable
fun SensorsPlaygroundPreview()
{
    Project10Theme {
        SensorsPlayground(GlobalViewModel())
    }
}