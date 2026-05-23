package com.astranavi.app.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

class AppNavigationActions(val navController: NavController) {

    fun navigateTopLevel(destination: AppDestination) {
        val currentBase = currentBaseRoute()
        if (currentBase == destination.baseRoute) {
            return
        }

        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateDetail(route: String) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == route) {
            return
        }
        val currentBase = currentRoute?.substringBefore("?")
        val targetBase = route.substringBefore("?")
        if (currentBase == targetBase && currentRoute == route) {
            return
        }

        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun replaceAuthStack(route: String) {
        navController.navigate(route) {
            popUpTo(0) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateIfNotCurrent(route: String) {
        navigateDetail(route)
    }

    fun currentBaseRoute(): String? {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        return currentRoute?.substringBefore("?")?.substringBefore("/")
    }
}
