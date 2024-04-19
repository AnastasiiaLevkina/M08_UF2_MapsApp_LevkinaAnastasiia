package com.example.mapsapp.models

sealed class Routes (val route: String) {
    object LaunchScreen: Routes("launch_screen")
    object LoginScreen: Routes("login_screen")
    object RegisterScreen: Routes("register_screen")
    object MyDrawer: Routes("my_drawer")

    object OnCreate: Routes("on_create")
    object GeolocationScreen: Routes("geolocatoin_screen")
    object MapScreen: Routes("map_screen")
    object CameraScreen: Routes("camera_screen")
    object TakePhotoScreen: Routes("take_photo_screen")
    object MarkerListScreen: Routes("marker_list_screen")
    object GalleryScreen: Routes("gallery_screen")
}