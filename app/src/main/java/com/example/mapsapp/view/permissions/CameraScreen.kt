package com.example.mapsapp.view.permissions

import android.Manifest
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.mapsapp.models.MyMarker
import com.example.mapsapp.models.Routes
import com.example.mapsapp.view.markers.AddMarkerBottomSheet
import com.example.mapsapp.viewModel.MapsViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.LatLng

@Composable
fun CameraScreen(myViewModel: MapsViewModel, navController: NavController) {
    val context = LocalContext.current

    val showBottomSheet by myViewModel.bottomSheet.observeAsState(false)
    val currentLocation by myViewModel.selectedLocation.observeAsState(LatLng(0.0, 0.0))
    val selectedMarker by myViewModel.selectedMarker.observeAsState(null)

    val isCameraPermissionGranted by myViewModel.cameraPermissionGranted.observeAsState(false)
    val shouldShowPermissionRationale by myViewModel.shouldShowPermissionRationale.observeAsState(false)
    val showPermissionDenied by myViewModel.showPermissionDenied.observeAsState(false)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                myViewModel.setCameraPermissionGranted(true)
            } else {
                myViewModel.setShouldPermissionRationale(
                    shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.CAMERA
                    )
                )
                if (!shouldShowPermissionRationale) {
                    Log.i("CameraScreen", "Cannot ask permission.")
                    myViewModel.setShowPermissionDenied(true)
                }
            }
        }
    )
    if (!isCameraPermissionGranted) {
        SideEffect { // Para evitar java.lang.IllegalStateException: Launcher has not been initialized
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick =
            { navController.navigate(Routes.TakePhotoScreen.route) },
                shape = RoundedCornerShape(20),
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(2.dp)
            )
            {
                Text("Take a photo")
            }
            Button(onClick =
            { navController.navigate(Routes.GalleryScreen.route) },
                shape = RoundedCornerShape(20),
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(2.dp)
            )
            {
                Text("Go to gallery")
            }

            Button(onClick =
            {
                /*myViewModel.selectMarker(
                    MyMarker(
                        selectedMarker!!.userId,
                        selectedMarker!!.markerId,
                        selectedMarker!!.position,
                        selectedMarker!!.title,
                        selectedMarker!!.snippet,
                        selectedMarker!!.color,
                        null
                    )
                )*/
                myViewModel.showBottomSheet()
            },
                shape = RoundedCornerShape(20),
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(2.dp)
            )
            {
                Text("No photo")
            }
        }
    }
    if (showPermissionDenied) {
        PermissionDeclinedScreen()
    } else if (showBottomSheet) {
        myViewModel.selectImage(null)
        AddMarkerBottomSheet(myViewModel, navController,
            MyMarker(
                selectedMarker?.userId,
                selectedMarker?.markerId,
                selectedMarker?.position?: currentLocation,
                selectedMarker?.title?: "",
                selectedMarker?.snippet?: "",
                selectedMarker?.color?: HUE_RED,
                null
            ),
            ""
        )
    }
}