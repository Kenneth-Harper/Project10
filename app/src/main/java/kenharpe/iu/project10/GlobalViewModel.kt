package kenharpe.iu.project10

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GlobalViewModel : ViewModel()
{
    private val TAG = "GlobalViewModel"

    private val _city = MutableLiveData<String>("Not yet Found")
    val city : LiveData<String> get() = _city

    private val _state = MutableLiveData<String>("Not yet Found")
    val state : LiveData<String> get() = _state

    private val _temp = MutableLiveData<Float>()
    val temp : LiveData<Float> get() = _temp

    private val _pressure = MutableLiveData<Float>()
    val pressure : LiveData<Float> get() = _pressure

    private val _gestures = MutableLiveData<MutableList<String>>()
    val gestures : LiveData<List<String>> get() = _gestures as LiveData<List<String>>

    fun setTemp(newTemp: Float)
    {
        _temp.value = (newTemp * 1.8f) + 32f
    }

    fun setPressure(pressure: Float)
    {
        _pressure.value = pressure
    }

    fun setCity(newCity: String)
    {
        _city.value = newCity
    }

    fun setState(newState: String)
    {
        _state.value = newState
    }

    fun addGesture(newGesture: String)
    {
        _gestures.value?.add(newGesture)
    }
}