package com.example.mapsapp.view.markers

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.mapsapp.models.MyMarker
import com.example.mapsapp.viewModel.MapsViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryScreen(navController: NavController, myViewModel: MapsViewModel) {
    val showBottomSheet by myViewModel.bottomSheet.observeAsState(false)
    val currentLocation by myViewModel.selectedLocation.observeAsState(LatLng(0.0, 0.0))
    val selectedMarker by myViewModel.selectedMarker.observeAsState(null)

    val context = LocalContext.current

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
    val now = Date()
    val fileName by remember { mutableStateOf(formatter.format(now)) }

    var uri: Uri? by remember { mutableStateOf(null) }

    val launchImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            try {
                bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = it?.let {it1 ->
                        ImageDecoder.createSource(context.contentResolver, it1)
                    }
                    source?.let { it1 ->
                        ImageDecoder.decodeBitmap(it1)
                    }!!
                }
                uri = it
            } catch (_: NullPointerException) {
                println("Caught a null pointer exception while closing the gallery.")
            }
        }
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(), contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier
                    .clip(RoundedCornerShape(20))
                    .size(250.dp)
                    .background(Color.LightGray)
                    .border(
                        width = 1.dp, color = Color.White,
                        shape = RoundedCornerShape(20)
                    )
                    .padding(15.dp)
            )
        }
        Button(onClick = {
            launchImage.launch("image/*")
        },
            shape = RoundedCornerShape(20),
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(2.dp)
        ) {
            Text(text = "Open gallery")
        }
        Button(onClick = {
            myViewModel.selectImage(uri)
            myViewModel.showBottomSheet()
        },
            shape = RoundedCornerShape(20),
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(2.dp),
            enabled = (uri != null)
        ) {
            Text(text = "Upload image")
        }
        if (showBottomSheet) {
            myViewModel.selectImage(uri)
            AddMarkerBottomSheet(myViewModel,
                navController,
                selectedMarker?: MyMarker(
                    null, null,
                    currentLocation,
                    "", "", HUE_RED, uri.toString()
                ),
                fileName = fileName
            )
        }
    }
}