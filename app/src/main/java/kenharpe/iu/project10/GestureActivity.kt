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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
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

class GestureActivity : ComponentActivity()
{

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

@Composable
fun DisplayScreen(viewModel: GlobalViewModel)
{
    val currentOrientation = LocalConfiguration.current.orientation
    val gesturesShown = if (viewModel.gestures.isInitialized) remember {mutableStateOf(viewModel.gestures.value!!.takeLast(10))}
                        else remember { mutableStateOf(listOf<String>())}
    val coroutineScope = rememberCoroutineScope()

    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
    {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly)
        {
            BoxWithConstraints( modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(10.dp, 10.dp)
                .background(color = Color.DarkGray))
            {
                Text("Gesture Playground", color = Color.Gray, fontSize = 40.sp)

                val maxX = (constraints.maxWidth - (50 * LocalDensity.current.density)).toInt()
                val maxY = (constraints.maxHeight - (50 * LocalDensity.current.density)).toInt()

                var position = remember { Animatable(Offset(maxX/2f, maxY/2f), Offset.VectorConverter)}
                val onGesturePerformed = {deltaX : Float, deltaY : Float, maxWidth: Int, maxHeight: Int ->
                    val newDescription = getSwipeDescription(deltaX, deltaY)
                    viewModel.addGesture(newDescription)
                    gesturesShown.value = viewModel.gestures.value!!.takeLast(10)
                    val targetX = clamp(position.value.x + deltaX, maxWidth)
                    val targetY = clamp(position.value.y + deltaY, maxHeight)
                    val targetOffset = Offset(targetX.toFloat(), targetY.toFloat())
                    coroutineScope.launch { position.animateTo(targetOffset) }
                }

                GestureCanvas(
                    position = position.value,
                    onGesturePerformed = onGesturePerformed,
                    maxX = maxX,
                    maxY = maxY
                )
            }

            val listModifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(0.dp, 10.dp, 10.dp, 10.dp)
            GestureList(gesturesShown.value, listModifier)
        }
    }
    else
    {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly)
        {
            BoxWithConstraints( modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp, 10.dp)
                .background(color = Color.DarkGray), contentAlignment = Alignment.Center)
            {
                Text("Gesture Playground", color = Color.Gray, fontSize = 40.sp)

                val maxX = (constraints.maxWidth - (50 * LocalDensity.current.density)).toInt()
                val maxY = (constraints.maxHeight - (50 * LocalDensity.current.density)).toInt()

                var position = remember { Animatable(Offset(maxX/2f, maxY/2f), Offset.VectorConverter)}
                val onGesturePerformed = {deltaX : Float, deltaY : Float, maxWidth: Int, maxHeight: Int ->
                    val newDescription = getSwipeDescription(deltaX, deltaY)
                    viewModel.addGesture(newDescription)
                    gesturesShown.value = viewModel.gestures.value?.takeLast(10) ?: listOf()
                    val targetX = clamp(position.value.x + deltaX, maxWidth)
                    val targetY = clamp(position.value.y + deltaY, maxHeight)
                    val targetOffset = Offset(targetX.toFloat(), targetY.toFloat())
                    coroutineScope.launch { position.animateTo(targetOffset) }
                }
                GestureCanvas(
                    position = position.value,
                    onGesturePerformed = onGesturePerformed,
                    maxX = maxX,
                    maxY = maxY
                )
            }
            val listModifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp, 0.dp, 10.dp, 10.dp)
            GestureList(gesturesShown.value, listModifier)
        }
    }
}

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

fun clamp(currentValue: Float, maxValue: Int): Int
{
    return when
    {
        currentValue < 0 -> 0
        currentValue > maxValue -> maxValue
        else -> currentValue.roundToInt()
    }
}

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
        for (i in 1..numberEmpty)
        {
            Text("", textModifier.background(Color.White), fontSize = 20.sp)
        }
    }
}

private fun getSwipeDescription(deltaX: Float, deltaY: Float): String {
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