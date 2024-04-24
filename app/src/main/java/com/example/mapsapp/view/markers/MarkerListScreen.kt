package com.example.mapsapp.view.markers

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mapsapp.R
import com.example.mapsapp.models.MyMarker
import com.example.mapsapp.models.Routes
import com.example.mapsapp.viewModel.MapsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun MarkerListScreen(myViewModel: MapsViewModel, navController: NavController){
    myViewModel.getSavedMarkers()
    myViewModel.filterMarkers()
    val myMarkers by myViewModel.filteredMarkers.observeAsState()
    val showBottomSheet by myViewModel.bottomSheet.observeAsState(false)
    val selectedMarker by myViewModel.selectedMarker.observeAsState(null)

    if (showBottomSheet) {
        AddMarkerBottomSheet(
            myViewModel,
            navController,
            selectedMarker!!
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MySearchBar(myViewModel)
        if (!myMarkers!!.isEmpty()) {
            LazyColumn (
                Modifier.fillMaxSize()
            ) {
                items(myMarkers!!.reversed()) {
                    Box {
                        ElevatedCard(
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            ),
                            shape = RoundedCornerShape(5),
                            modifier = Modifier
                                .padding(vertical = 2.dp, horizontal = 7.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.3f)
                                .clickable {
                                    myViewModel.changeCurrentLocation(it.position)
                                    navController.navigate(Routes.MapScreen.route)
                                }
                        ) {
                            val cameraPermissionState =
                                rememberPermissionState(permission = Manifest.permission.CAMERA)
                            LaunchedEffect(Unit) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                            Row(
                                Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (it.photo != null && it.photo != "null") {
                                    GlideImage(
                                        model = it.photo,
                                        contentDescription = "Image from storage",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(60.dp)
                                    )
                                } else {
                                    Image(
                                        painterResource(id = R.drawable.mapsicon),
                                        "Map icon",
                                        Modifier.size(60.dp)
                                    )
                                }
                                //}
                                Column(
                                    modifier = Modifier.fillMaxWidth(0.5f)
                                ) {
                                    Text(it.title, fontWeight = FontWeight.Bold)
                                    Text(it.snippet)
                                }
                                IconButton(
                                    onClick = {
                                        myViewModel.selectMarker(it)
                                        myViewModel.selectImage(it.photo?.toUri())
                                        myViewModel.showBottomSheet()
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = "Edit marker"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        myViewModel.deleteMarker(it)
                                        if (it.photo != null && it.photo != "null") myViewModel.removeImage(it.photo!!)
                                        myViewModel.selectMarker(null)
                                        navController.navigate(Routes.MarkerListScreen.route)
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove marker"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(text = "No markers for the moment",
                fontSize = 20.sp,
                modifier = Modifier.padding(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySearchBar(myViewModel: MapsViewModel){
    val searchText by myViewModel.searchText.observeAsState("")
    SearchBar(
        query = searchText,
        onQueryChange ={
            myViewModel.onSearchTextChange(it)
            myViewModel.filterMarkers()
                       },
        active = true,
        onActiveChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f)
        ,
        colors = SearchBarDefaults.colors(
            containerColor = Color.White,
            dividerColor = Color.LightGray,
            inputFieldColors = TextFieldDefaults.colors(
                Color.Black
            )
        ),
        onSearch =  { myViewModel.filterMarkers() },
        placeholder = {
            Text(text = "Search for a marker...")
        },
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Erase query",
                tint = Color.Black,
                modifier = Modifier.clickable {
                    myViewModel.onSearchTextChange("")
                    myViewModel.filterMarkers()
                }
            )
        }
    )
    { }
}
