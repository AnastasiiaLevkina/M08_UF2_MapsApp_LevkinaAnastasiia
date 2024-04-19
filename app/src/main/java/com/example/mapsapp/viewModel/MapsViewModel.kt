package com.example.mapsapp.viewModel

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mapsapp.models.MyMarker
import com.example.mapsapp.models.Repository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.compose.MapType

class MapsViewModel: ViewModel() {
    private val repository: Repository = Repository()
    private val auth = FirebaseAuth.getInstance()

    private val _selectedMarker = MutableLiveData<MyMarker?>(null)
    val selectedMarker = _selectedMarker

    private val _currentLocation = MutableLiveData(LatLng(0.0, 0.0))
    val currentLocation = _currentLocation

    private val _selectedLocation = MutableLiveData(LatLng(0.0, 0.0))
    val selectedLocation = _selectedLocation

    private val _selectedImage = MutableLiveData<Uri?>(null)
    val selectedImage = _selectedImage

    private val _imageUrl = MutableLiveData<Uri?>(null)
    val imageUrl = _imageUrl

    private val _markerReady = MutableLiveData(false)
    val markerReady = _markerReady

    private val _userEnterError = MutableLiveData(false)
    val userEnterError = _userEnterError

    private val _bottomSheet = MutableLiveData(false)
    val bottomSheet = _bottomSheet

    private val _goToNext = MutableLiveData(false)
    val goToNext = _goToNext

    private val _isProcessing = MutableLiveData(true)
    val isProcessing = _isProcessing

    private val _userId = MutableLiveData<String?>(null)
    val userId = _userId

    private val _loggedUser = MutableLiveData<String?>(null)
    val loggedUser = _loggedUser

    private val _listOfMarkers = MutableLiveData(mutableListOf<MyMarker>())
    val listOfMarkers = _listOfMarkers

    private val mapTypes = listOf(MapType.NORMAL, MapType.SATELLITE, MapType.TERRAIN, MapType.HYBRID)
    private val _selectedMapTypeIndex = MutableLiveData(MapType.TERRAIN)
    val selectedMapType = _selectedMapTypeIndex

    private val _cameraPermissionGranted = MutableLiveData(false)
    val cameraPermissionGranted = _cameraPermissionGranted

    private val _shouldShowPermissionRationale = MutableLiveData(false)
    val shouldShowPermissionRationale = _shouldShowPermissionRationale

    private val _showPermissionDenied = MutableLiveData(false)
    val showPermissionDenied = _showPermissionDenied

    private val _locationPermissionGranted = MutableLiveData(false)
    val locationPermissionGranted = _locationPermissionGranted

    private val _shouldShowLocationPermissionRationale = MutableLiveData(false)
    val shouldShowLocationPermissionRationale = _shouldShowLocationPermissionRationale

    private val _locationPermissionDenied = MutableLiveData(false)
    val locationPermissionDenied = _locationPermissionDenied

    // SEARCH MARKERS
    private val _isSearching = MutableLiveData(false)
    val isSearching = _isSearching
    private val _searchText = MutableLiveData("")
    val searchText = _searchText

    private val _filteredMarkers = MutableLiveData(mutableListOf<MyMarker>())
    val filteredMarkers = _filteredMarkers

    // SEARCH MARKERS BY NAME
    fun onSearchTextChange(text: String){
        _searchText.value = text
    }

    fun confirmMarkerReady(save: Boolean) {
        _markerReady.value = save
    }

    fun filterMarkers(){
        _filteredMarkers.value = _listOfMarkers.value!!.filter {
            it.title.contains(_searchText.value?: "", ignoreCase = true)
        }.toMutableList()
    }

    fun selectMarker(marker: MyMarker?) {
        _selectedMarker.value = marker
    }

    fun setUserEnterError(error: Boolean) {
        _userEnterError.value = error
    }

    fun changeCurrentLocation(position: LatLng) {
        _selectedLocation.value = position
    }

    fun selectImage(image: Uri?) {
        _selectedImage.value = image
    }

    fun selectImageUrl(imageUrl: Uri?) {
        _imageUrl.value = imageUrl
    }

    fun changeMapType(index: Int) {
        this._selectedMapTypeIndex.value = mapTypes[index]
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        this._cameraPermissionGranted.value = granted
    }
    fun setShouldPermissionRationale(should: Boolean) {
        _shouldShowPermissionRationale.value = should
    }
    fun setShowPermissionDenied(denied: Boolean) {
        _showPermissionDenied.value = denied
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        this._locationPermissionGranted.value = granted
    }
    fun setShouldLocationPermissionRationale(should: Boolean) {
        _shouldShowLocationPermissionRationale.value = should
    }
    fun setShowLocationPermissionDenied(denied: Boolean) {
        _locationPermissionDenied.value = denied
    }

    fun saveMarker(newMarker: MyMarker) {
        repository.saveMarker(newMarker)
    }

    fun getSavedMarkers() {
        repository.getMarkers().whereEqualTo("userId", userId.value).addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore error", error.message.toString())
                    return //@addSnapshotListener
                }
                val tempList = mutableListOf<MyMarker>()
                for(dc: DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        val newMarker = MyMarker(
                            dc.document.get("userId").toString(),
                            dc.document.id,
                            LatLng(dc.document.get("markerLatitude")!!.toString().toDouble(),
                            dc.document.get("markerLongitude")!!.toString().toDouble()),
                            dc.document.get("markerTitle")!!.toString(),
                            dc.document.get("markerSnippet")!!.toString(),
                            dc.document.get("markerColor")!!.toString().toFloat(),
                            dc.document.get("markerPhoto")?.toString()
                        )
                        //println("MARKER: $newMarker")
                        //.toObject(MyMarker::class.java) NO FUNCIONA
                        tempList.add(newMarker)

                    }
                }
                _listOfMarkers.value = tempList
            }
        })
    }

    fun editMarker(editedMarker: MyMarker) {
        repository.editMarker(editedMarker)
    }

    fun deleteMarker(deletedMarker: MyMarker) {
        repository.deleteMarker(deletedMarker)
    }

    fun showBottomSheet() { _bottomSheet.value = true }
    fun hideBottomSheet() {
        _bottomSheet.value = false
        selectImage(null)
    }

    fun uploadImage(imageUri: Uri?, fileName: String, deleteUrl: String?) {
        val storage = FirebaseStorage.getInstance().getReference("images/$fileName")
        storage.putFile(imageUri?: "null".toUri())
            .addOnSuccessListener {
                Log.i("IMAGE UPLOAD", "Image uploaded successfully")
                storage.downloadUrl.addOnSuccessListener {
                    println("Image saved in Storage: $it")
                    selectImageUrl(it)
                    if (deleteUrl != null && deleteUrl != "null") removeImage(deleteUrl)
                    confirmMarkerReady(true)
                }
            }
            .addOnFailureListener {
                Log.i("IMAGE UPLOAD", "Image upload failed")
                selectImageUrl(deleteUrl?.toUri())
                confirmMarkerReady(true)
            }
    }

    fun removeImage(url: String) {
        try {
            val storage = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            storage.delete()
        } catch (_: IllegalArgumentException) {
            Log.i("removeImage", "Caught an IllegalArgumentException (probably tried to remove a null image)")
        }

    }

    fun login(username: String?, password: String?) {
        auth.signInWithEmailAndPassword(username!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _userId.value = task.result.user?.uid
                    _loggedUser.value = task.result.user?.email?.split("@")?.get(0)
                    _goToNext.value = true
                    modifyProcessing()
                    setUserEnterError(false)
                } else {
                    _goToNext.value = false
                }
            }
            .addOnFailureListener {
                setUserEnterError(true)
            }
    }

    fun register(username: String, password: String) {
        auth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _userId.value = task.result.user?.uid
                    _loggedUser.value = task.result.user?.email?.split("@")?.get(0)
                    _goToNext.value = task.isSuccessful
                    modifyProcessing()
                    setUserEnterError(false)
                } else {
                    _goToNext.value = false
                }
            }
            .addOnFailureListener {
                setUserEnterError(true)
            }
    }

    fun logout() {
        _userId.value = null
        _loggedUser.value = null
        _goToNext.value = false
        modifyProcessing()
        auth.signOut()
    }

    fun modifyProcessing() {
        _isProcessing.value = !_isProcessing.value!!
    }
    fun setCurrentLocation(loc: LatLng) {
        _currentLocation.value = loc
    }
}