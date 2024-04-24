package com.example.mapsapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mapsapp.models.Routes
import com.example.mapsapp.view.Launch
import com.example.mapsapp.view.LaunchScreen
import com.example.mapsapp.view.MyDrawer
import com.example.mapsapp.view.user.LoginScreen
import com.example.mapsapp.view.user.RegisterScreen
import com.example.mapsapp.viewModel.MapsViewModel

@Composable
fun OnCreate(myViewModel: MapsViewModel) {
    val mainNavController = rememberNavController()
    NavHost(
        navController = mainNavController as NavHostController,
        startDestination = Routes.LaunchScreen.route
    ) {
        composable(
            Routes.LaunchScreen.route
        ) {
            LaunchScreen(
                mainNavController
            )
        }
        composable(
            Routes.LoginScreen.route
        ) {
            LoginScreen(
                myViewModel,
                mainNavController
            )
        }
        composable(
            Routes.RegisterScreen.route
        ) {
            RegisterScreen(
                myViewModel,
                mainNavController
            )
        }
        composable(
            Routes.MyDrawer.route
        ) {
            MyDrawer(
                myViewModel,
                mainNavController
            )
        }
        composable(
            Routes.OnCreate.route
        ) {
            OnCreate(
                myViewModel
            )
        }
    }
}