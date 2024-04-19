package com.example.mapsapp.view.markers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mapsapp.models.MyMarker
import com.example.mapsapp.models.Routes
import com.example.mapsapp.viewModel.MapsViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.LatLng
import java.io.OutputStream

@Composable
fun TakePhotoScreen(navController: NavController, myViewModel: MapsViewModel) {
    val showBottomSheet by myViewModel.bottomSheet.observeAsState(false)
    val currentLocation by myViewModel.selectedLocation.observeAsState(LatLng(0.0, 0.0))
    val selectedMarker by myViewModel.selectedMarker.observeAsState(null)
    var image by remember { mutableStateOf<Pair<Uri?, String?>?>(null) }

    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            CameraController.IMAGE_CAPTURE
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (showBottomSheet) {
            Log.i("Photo taken", "Photo: ${image?.first}")
            AddMarkerBottomSheet(myViewModel, navController, MyMarker(
                selectedMarker?.userId, selectedMarker?.markerId, selectedMarker?.position?: currentLocation,
                selectedMarker?.title?: "", selectedMarker?.snippet?: "", selectedMarker?.color?: HUE_RED,
                image?.first.toString()
            ), image?.second)
        } else {
            CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
            IconButton (
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                },
                modifier = Modifier.offset(16.dp, 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Cameraswitch, "Switch Camera")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter))
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navController.navigate(Routes.GalleryScreen.route) }) {
                        Icon(imageVector = Icons.Default.Photo, contentDescription = "Open gallery")
                    }
                    IconButton (onClick = {
                        takePhoto(context, controller) { photo ->
                            image = bitmapToUri(context,photo)
                            myViewModel.selectImage(image!!.first)
                            myViewModel.showBottomSheet()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.PhotoCamera, "Take photo")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview (
    controller: LifecycleCameraController, modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView (
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        }, modifier = modifier
    )
}

private fun takePhoto(context: Context,
                      controller: LifecycleCameraController,
                      onPhotoTaken: (Bitmap) -> Unit) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                onPhotoTaken(image.toBitmap())
                val matri = Matrix().apply{
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matri,
                    true
                )
                onPhotoTaken(rotatedBitmap)
            }
            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Error taken photo", exception)
            }
        }
    )
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Pair<Uri?, String?> {
    val filename = "${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, filename)
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    }

    val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        val outstream: OutputStream? = context.contentResolver.openOutputStream(it)
        outstream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        outstream?.close()
    }

    return Pair(uri, filename)
}