package com.example.mapsapp.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapsapp.models.Routes
import com.example.mapsapp.view.markers.ColorRadioButtons
import com.example.mapsapp.view.user.UserPrefs
import com.example.mapsapp.viewModel.MapsViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDrawer(myViewModel: MapsViewModel, mainNavController: NavController){
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val state: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val loggedUser by myViewModel.loggedUser.observeAsState("")

    val trafficEnabled by myViewModel.trafficEnabled.observeAsState(false)

    var mapTypeText by remember { mutableStateOf("TERRAIN") }
    val mapTypeList: List<String> = listOf("NORMAL", "SATELLITE", "TERRAIN", "HYBRID")
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userPrefs = UserPrefs(context)

    ModalNavigationDrawer(
        drawerState = state,
        gesturesEnabled = false,
        drawerContent = {
        ModalDrawerSheet {
            IconButton(onClick = {
                scope.launch {
                    state.close()
                }
            }) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
            }
            NavigationDrawerItem(
                label = { Text("Map") },
                selected = false,
                onClick = {
                    scope.launch {
                        state.close() //Position and close the drawer
                    }
                    navController.navigate(Routes.GeolocationScreen.route)
                }
            )
            NavigationDrawerItem(
                label = { Text("My Markers") },
                selected = false,
                onClick = {
                    scope.launch {
                        state.close() //Position and close the drawer
                    }
                    navController.navigate(Routes.MarkerListScreen.route)
                }
            )
            Divider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier
                        .padding(10.dp)
                        .fillMaxWidth(0.5f)
                ){
                    OutlinedTextField(
                        value = mapTypeText,
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        label = {
                            Text(text = "Map Type", color = Color.Black,
                                fontSize = 15.sp)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        modifier = Modifier.clickable { expanded = true },
                        colors = TextFieldDefaults.colors()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        mapTypeList.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        fontSize = 25.sp,
                                        color = Color.Black
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    myViewModel.changeMapType(mapTypeList.indexOf(option))
                                    mapTypeText = option
                                }
                            )
                        }
                    }
                }
                // FILTER MARKERS BY COLOR
                ColorCheckBoxes(myViewModel)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("Traffic", modifier = Modifier.padding(15.dp))
                Checkbox(
                    checked = trafficEnabled,
                    onCheckedChange = { myViewModel.enableTraffic() }
                )
            }
            Divider()
            Text(text = loggedUser?: "Guest",
                modifier = Modifier
                    .padding(10.dp)
            )
            if (loggedUser != null) {
                Button(
                    onClick = {
                        scope.launch {
                            state.close()
                        }
                        myViewModel.logout()
                        CoroutineScope(Dispatchers.IO).launch {
                            userPrefs.deleteUserData()
                        }
                        mainNavController.navigate(Routes.OnCreate.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .padding(horizontal = 10.dp),
                    shape = RoundedCornerShape(10),
                ) {
                    Text(text = "Logout")
                }
            } else {
                Button(
                    onClick = {
                        mainNavController.navigate(Routes.OnCreate.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .padding(horizontal = 10.dp),
                    shape = RoundedCornerShape(10),
                ) {
                    Text(text = "Login")
                }
            }

        }
    } )
    {
        MyScaffold(myViewModel, navController, state)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorCheckBoxes(myViewModel: MapsViewModel){
    val filterColors by myViewModel.filterColors.observeAsState(listOf())
    val markerColors: Array<Pair<Color, Float>> = arrayOf(
        Pair(Color.Red, BitmapDescriptorFactory.HUE_RED), Pair(Color.Blue,
            BitmapDescriptorFactory.HUE_BLUE
        ), Pair(Color.Green, BitmapDescriptorFactory.HUE_GREEN),
        Pair(Color.Yellow, BitmapDescriptorFactory.HUE_YELLOW), Pair(Color.Cyan,
            BitmapDescriptorFactory.HUE_CYAN
        ), Pair(Color.Magenta, BitmapDescriptorFactory.HUE_MAGENTA)
    )
    FlowRow (horizontalArrangement = Arrangement.Center){
        markerColors.forEach { color ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Icon(Icons.Filled.LocationOn, "Coloured Marker Icon", tint = color.first)
                Checkbox(
                    checked = filterColors.contains(color.second),
                    onCheckedChange = {
                        if (!filterColors.contains(color.second))
                            myViewModel.addFilterColor(color.second)
                        else myViewModel.removeFilterColor(color.second)
                        myViewModel.getSavedMarkers()
                    }
                )
            }
        }
    }
}
