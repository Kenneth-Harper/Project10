package kenharpe.iu.project10

import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kenharpe.iu.project10.ui.theme.Project10Theme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * <h1> GestureActivity Class </h1>
 * The class extending ComponentActivity responsible for the display of the second screen
 * where gestures determine how to move a ball, and are displayed on screen as well
 */

class GestureActivity : ComponentActivity()
{
    /**
     * <h2> Member Attributes </h2>
     * <ul>
     *     <li> viewModel: The ViewModel for the app
     * </ul>
     */
    private val viewModel : GlobalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent{
            Project10Theme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisplayScreen(viewModel)
                }
            }
        }
    }
}

/**
 * <h2> DisplayScreen </h2>
 * Function that generates the main UI structure for the activity
 * @param viewModel viewModel for the whole application
 * @return          <code>Unit</code>
 */
@Composable
fun DisplayScreen(viewModel: GlobalViewModel)
{
    // Setup a State object that will display all gestures that have been performed, either from viewModel, or an empty list
    val gesturesShown = if (viewModel.gestures.isInitialized) remember { mutableStateOf(viewModel.gestures.value!!.takeLast(10)) }
                        else remember { mutableStateOf(listOf()) }
    val coroutineScope = rememberCoroutineScope()

    // Check the orientation of the phone
    val currentOrientation = LocalConfiguration.current.orientation
    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
    {
        // Set the landscape UI of the activity
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly)
        {
            BoxWithConstraints( modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(10.dp, 10.dp)
                .background(color = Color.DarkGray))
            {
                Text("Gesture Playground", color = Color.Gray, fontSize = 40.sp)
                // Get the maximum values position can be
                val maxX = (constraints.maxWidth - (50 * LocalDensity.current.density)).toInt()
                val maxY = (constraints.maxHeight - (50 * LocalDensity.current.density)).toInt()

                // Set the initial position to the middle of the gesture area
                val position = remember { Animatable(Offset(maxX/2f, maxY/2f), Offset.VectorConverter)}
                // Set a lambda expression to be performed whenever a gesture is detected
                val onGesturePerformed = {deltaX : Float, deltaY : Float, maxWidth: Int, maxHeight: Int ->
                    val newDescription = getSwipeDescription(deltaX, deltaY)
                    viewModel.addGesture(newDescription)
                    gesturesShown.value = viewModel.gestures.value!!.takeLast(10)
                    val targetX = clamp(position.value.x + deltaX, maxWidth)
                    val targetY = clamp(position.value.y + deltaY, maxHeight)
                    val targetOffset = Offset(targetX.toFloat(), targetY.toFloat())
                    coroutineScope.launch { position.animateTo(targetOffset) }
                }
                GestureCanvas(position.value, onGesturePerformed, maxX, maxY)
            }
            // Creates the modifier for the second half of the screen, which shows what gestures have been made
            val listModifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(0.dp, 10.dp, 10.dp, 10.dp)
            GestureList(gesturesShown.value, listModifier)
        }
    }
    else
    {
        // Set the portrait UI of the activity
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly)
        {
            BoxWithConstraints( modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp, 10.dp)
                .background(color = Color.DarkGray), contentAlignment = Alignment.Center)
            {
                Text("Gesture Playground", color = Color.Gray, fontSize = 40.sp)
                // Get the maximum values position can be
                val maxX = (constraints.maxWidth - (50 * LocalDensity.current.density)).toInt()
                val maxY = (constraints.maxHeight - (50 * LocalDensity.current.density)).toInt()

                // Set the initial position to the middle of the gesture area
                val position = remember { Animatable(Offset(maxX/2f, maxY/2f), Offset.VectorConverter)}
                // Set a lambda expression to be performed whenever a gesture is detected
                val onGesturePerformed = {deltaX : Float, deltaY : Float, maxWidth: Int, maxHeight: Int ->
                    val newDescription = getSwipeDescription(deltaX, deltaY)
                    viewModel.addGesture(newDescription)
                    gesturesShown.value = viewModel.gestures.value?.takeLast(10) ?: listOf()
                    val targetX = clamp(position.value.x + deltaX, maxWidth)
                    val targetY = clamp(position.value.y + deltaY, maxHeight)
                    val targetOffset = Offset(targetX.toFloat(), targetY.toFloat())
                    coroutineScope.launch { position.animateTo(targetOffset) }
                }
                GestureCanvas(position.value, onGesturePerformed, maxX, maxY)
            }
            // Creates the modifier for the second half of the screen, which shows what gestures have been made
            val listModifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp, 0.dp, 10.dp, 10.dp)
            GestureList(gesturesShown.value, listModifier)
        }
    }
}

/**
 * <h2> GestureCanvas </h2>
 * Function that generates the box in which gestures are to be made, and where the ball is shown
 * @param position              Offset object that tracks where the ball is supposed to be
 * @param onGesturePerformed    Lambda expression which is called when a gesture is performed
 * @param maxX                  Int value representing the maximum x-Coordinate of the ball
 * @param maxY                  Int value representing the maximum y-Coordinate of the ball
 * @return                      <code>Unit</code>
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GestureCanvas(position: Offset, onGesturePerformed: (Float, Float, Int, Int) -> Job, maxX: Int, maxY: Int)
{
    val pixelDensity = LocalDensity.current.density
    var startGesturePosition by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints (Modifier
        .fillMaxSize()
        .pointerInteropFilter {
        motionEvent ->
        when (motionEvent.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                startGesturePosition = Offset(motionEvent.x, motionEvent.y)

                true
            }
            MotionEvent.ACTION_UP ->
            {
                val deltaX = motionEvent.x - startGesturePosition.x
                val deltaY = motionEvent.y - startGesturePosition.y
                onGesturePerformed(deltaX / pixelDensity, deltaY / pixelDensity, maxX, maxY)

                true
            }
            else -> false
        }
    })
    {
        val circleDiameter = 50f * pixelDensity
        val maxWidth = (constraints.maxWidth - circleDiameter).toInt()
        val maxHeight = (constraints.maxHeight - circleDiameter).toInt()

        val clampedX = clamp(position.x, maxWidth)
        val clampedY = clamp(position.y, maxHeight)

        Box(
            modifier = Modifier
                .size(50.dp)
                .offset{ IntOffset(clampedX, clampedY) }
                .clip(CircleShape)
                .background(Color.Red)
        )
    }
}

/**
 * <h2> Clamp </h2>
 * Function that clamps currentValue to between or equal to 0 and maxValue.
 * @param currentValue  The value being checked
 * @param maxValue      The maximum value currentValue is allowed to be
 * @return              <code>Int</code>
 */
fun clamp(currentValue: Float, maxValue: Int): Int
{
    return when
    {
        currentValue < 0 -> 0
        currentValue > maxValue -> maxValue
        else -> currentValue.roundToInt()
    }
}

/**
 * <h2> GestureList </h2>
 * Function that generates the list of gestures in the second half of the screen
 * @param gestureList   The list of gestures which will be displayed
 * @param modifier      The modifier meant to be applied to the column element
 * @return              <code>Unit</code>
 */
@Composable
fun GestureList(gestureList: List<String>, modifier: Modifier)
{
    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceEvenly)
    {
        val textModifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
        val numberEmpty = 10 - gestureList.size
        gestureList.forEach { gesture -> Text(gesture, textModifier, fontSize = 20.sp) }
        // Set the rest of the 10 elements so the visible gestures are spaced correctly
        for (i in 1..numberEmpty) { Text("", textModifier.background(Color.White), fontSize = 20.sp) }
    }
}

/**
 * <h2> getSwipeDescription </h2>
 * Function that takes in the change in coordinates of the ball, and returns a string stating what gesture it was
 * @param deltaX    Float value for the change in the xCoordinate of position
 * @param deltaY    Float value for the change in the yCoordinate of position
 * @return          <code>String</code>
 */
private fun getSwipeDescription(deltaX: Float, deltaY: Float): String
{
    val tolerance = 0.6f
    val absDeltaX = deltaX.absoluteValue
    val absDeltaY = deltaY.absoluteValue

    return when
    {
        absDeltaX > (absDeltaY + (absDeltaY * tolerance)) && deltaX > 0 -> "You Swiped Right"
        absDeltaX > (absDeltaY + (absDeltaY * tolerance)) && deltaX < 0 -> "You Swiped Left"
        absDeltaY > (absDeltaX + (absDeltaX * tolerance)) && deltaY > 0 -> "You Swiped Down"
        absDeltaY > (absDeltaX + (absDeltaX * tolerance)) && deltaY < 0 -> "You Swiped Up"
        absDeltaX > (absDeltaY - (absDeltaY * tolerance))
                && absDeltaX < (absDeltaY + (absDeltaY * tolerance))
                && deltaX > 0 -> if (deltaY > 0) "You Swiped Towards the Bottom-Right" else "You Swiped Towards the Top-Right"
        absDeltaX > (absDeltaY - (absDeltaY * tolerance))
                && absDeltaX < (absDeltaY + (absDeltaY * tolerance))
                && deltaX < 0 -> if (deltaY > 0) "You Swiped Towards the Bottom-Left" else "You Swiped Towards the Top-Left"
        absDeltaX < 10 && absDeltaY < 10 -> "You tapped the Screen"
        else -> "No Gesture"
    }
}