package org.delcom.pam_proyek1_ifs23049.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.delcom.pam_proyek1_ifs23049.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23049.helper.RouteHelper

@Composable
fun BottomNavComponent(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Triple(ConstHelper.RouteNames.Home.path, Icons.Default.Home, "Beranda"),
        Triple(ConstHelper.RouteNames.Books.path, Icons.Default.MenuBook, "Buku"),
        Triple(ConstHelper.RouteNames.Profile.path, Icons.Default.Person, "Profil"),
    )

    NavigationBar {
        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = {
                    RouteHelper.to(
                        navController = navController,
                        destination = route,
                        removeBackStack = false
                    )
                }
            )
        }
    }
}