package com.example.mapsapp.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mapsapp.R
import com.example.mapsapp.models.MyMarker
import com.example.mapsapp.view.markers.AddMarkerBottomSheet
import com.example.mapsapp.viewModel.MapsViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(myViewModel: MapsViewModel, navController: NavController) {
    val currentLocation: LatLng by myViewModel.selectedLocation.observeAsState(LatLng(0.0, 0.0))
    val selectedMarker by myViewModel.selectedMarker.observeAsState(null)
    val mapType by myViewModel.selectedMapType.observeAsState(MapType.TERRAIN)
    val trafficEnabled by myViewModel.trafficEnabled.observeAsState(false)

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentLocation, 6f)
        }
    LaunchedEffect(currentLocation) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }
    val showBottomSheet by myViewModel.bottomSheet.observeAsState(false)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                myViewModel.changeCurrentLocation(it)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                         },
            onMapLongClick = {
                myViewModel.changeCurrentLocation(it)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                myViewModel.showBottomSheet()
            },
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = true,
                isTrafficEnabled = trafficEnabled)
        ) {
            val myMarkers by myViewModel.listOfMarkers.observeAsState()
            myViewModel.getSavedMarkers()
            myMarkers!!.forEach {
                Marker(
                    state = MarkerState(position = it.position),
                    title = it.title,
                    snippet = it.snippet,
                    icon = BitmapDescriptorFactory.defaultMarker(it.color),
                    onInfoWindowLongClick = { marker ->
                            myViewModel.deleteMarker(it)
                            if (it.photo != null && it.photo != "null") myViewModel.removeImage(it.photo!!)
                    }
                )
            }
        }
    }
    if (showBottomSheet) {
        myViewModel.selectMarker(
            MyMarker(null, null, currentLocation,
            "", "", HUE_RED, null))
        AddMarkerBottomSheet(
            myViewModel,
            navController,
            selectedMarker!!
        )
    }
}