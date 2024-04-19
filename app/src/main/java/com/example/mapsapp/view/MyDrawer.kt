package com.example.mapsapp.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapsapp.models.Routes
import com.example.mapsapp.view.user.UserPrefs
import com.example.mapsapp.viewModel.MapsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MyDrawer(myViewModel: MapsViewModel, mainNavController: NavController){
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val state: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val loggedUser by myViewModel.loggedUser.observeAsState("")

    val context = LocalContext.current
    val userPrefs = UserPrefs(context)
    val storedUserData = userPrefs.getUserData.collectAsState(initial = emptyList())

    ModalNavigationDrawer(drawerState = state, gesturesEnabled = false, drawerContent = {
        ModalDrawerSheet {
            IconButton(onClick = {
                scope.launch {
                    state.close()
                }
            }) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
            }
            //Text("Markers", modifier = Modifier.padding(16.dp))
            Divider()
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