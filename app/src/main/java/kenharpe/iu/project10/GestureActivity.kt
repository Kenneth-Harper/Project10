package kenharpe.iu.project10

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import kenharpe.iu.project10.ui.theme.Project10Theme
import kotlin.math.absoluteValue

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
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
        LandscapeView(viewModel)
    }
    else
    {
        PortraitView(viewModel)
    }
}

@Composable
fun LandscapeView(viewModel: GlobalViewModel)
{
    Row()
    {

    }
}

@Composable
fun PortraitView(viewModel: GlobalViewModel)
{
    Column()
    {

    }
}

private fun GetSwipeDescription(deltaX: Float, deltaY: Float): String {
    val tolerance = 0.25f
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
        else -> "No Gesture"
    }
}