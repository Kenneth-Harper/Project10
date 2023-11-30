package kenharpe.iu.project10

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


/**
 * <h1> GlobalViewModel Class </h1>
 * The class extending ViewModel responsible for the data
 * where gestures determine how to move a ball, and are displayed on screen as well
 */
class GlobalViewModel : ViewModel()
{
//    private val TAG = "GlobalViewModel"
    /**
     * <h2> Member Attributes </h2>
     * <ul>
     *     <li> _city: Mutable live data for the city the phone is in
     *     <li> city: Live data object to access _city
     *     <li> _state: Mutable live data for the state the phone is in
     *     <li> state: Live data object to access _state
     *     <li> _temp: Mutable live data for the ambient temperature detected by the phone's sensors
     *     <li> state: Live data object to access _temp
     *     <li> _pressure: Mutable live data for the air pressure detected by the phone's sensors
     *     <li> pressure: Live data object to access _pressure
     *     <li> _gestures: Mutable live data for the list of gestures performed by the user
     *     <li> gestures: Live data object to access _gestures
     * </ul>
     */
    private val _city = MutableLiveData("Not yet Found")
    val city : LiveData<String> get() = _city

    private val _state = MutableLiveData("Not yet Found")
    val state : LiveData<String> get() = _state

    private val _temp = MutableLiveData<Float>()
    val temp : LiveData<Float> get() = _temp

    private val _pressure = MutableLiveData<Float>()
    val pressure : LiveData<Float> get() = _pressure

    private val _gestures = MutableLiveData(mutableListOf<String>())
    val gestures : LiveData<List<String>> get() = _gestures as LiveData<List<String>>

    /**
     * <h2> setTemp </h2>
     * Function that sets the _temp value to newTemp
     * @param newTemp   Float value representing the temp in Celsius
     * @return          <code>Unit</code>
     */
    fun setTemp(newTemp: Float)
    {
        _temp.value = (newTemp * 1.8f) + 32f
    }

    /**
     * <h2> setPressure </h2>
     * Function that sets the _pressure value to pressure
     * @param pressure  Float value representing the air pressure
     * @return          <code>Unit</code>
     */
    fun setPressure(pressure: Float)
    {
        _pressure.value = pressure
    }

    /**
     * <h2> setCity </h2>
     * Function that sets the _city value to newCity
     * @param newCity   String representing the name of the city the phone is in
     * @return          <code>Unit</code>
     */
    fun setCity(newCity: String)
    {
        _city.value = newCity
    }

    /**
     * <h2> setCity </h2>
     * Function that sets the _state value to newState
     * @param newState   String representing the name of the state the phone is in
     * @return          <code>Unit</code>
     */
    fun setState(newState: String)
    {
        _state.value = newState
    }

    /**
     * <h2> addGesture </h2>
     * Function that adds the string gesture to the _gestures value
     * @param newGesture    String representing the gesture of the User
     * @return              <code>Unit</code>
     */
    fun addGesture(newGesture: String)
    {
        _gestures.value?.add(newGesture)
    }
}