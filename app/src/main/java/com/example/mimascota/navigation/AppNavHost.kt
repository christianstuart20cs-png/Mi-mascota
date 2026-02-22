package com.example.mimascota.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mimascota.MascotaDatabase
import com.example.mimascota.ui.screens.AgregarRecordatorioScreen
import com.example.mimascota.ui.screens.HistorialMedicoScreen
import com.example.mimascota.ui.screens.MascotaFormScreen
import com.example.mimascota.ui.screens.MiMascotaApp
import com.example.mimascota.ui.screens.RecordatoriosMascotaScreen

@Composable
fun AppNavHost(db: MascotaDatabase) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MiMascotaApp(
                mascotaDao = db.mascotaDao(),
                navToAdd = { navController.navigate("add_pet") },
                navToEdit = { id -> navController.navigate("edit_pet/$id") },
                navToHistory = { id -> navController.navigate("history/$id") },
                navToReminders = { id -> navController.navigate("reminders/$id") }
            )
        }
        composable("add_pet") {
            MascotaFormScreen(
                title = "Registrar mascota",
                mascotaDao = db.mascotaDao(),
                mascotaId = null
            ) { navController.popBackStack() }
        }
        composable(
            route = "edit_pet/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            MascotaFormScreen(
                title = "Editar mascota",
                mascotaDao = db.mascotaDao(),
                mascotaId = backStack.arguments?.getInt("id")
            ) { navController.popBackStack() }
        }
        composable(
            route = "history/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            HistorialMedicoScreen(
                mascotaId = backStack.arguments?.getInt("id") ?: -1,
                historialDao = db.historialMedicoDao()
            ) { navController.popBackStack() }
        }
        composable(
            route = "reminders/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val mascotaId = backStack.arguments?.getInt("id") ?: -1
            RecordatoriosMascotaScreen(
                mascotaId = mascotaId,
                recordatorioDao = db.recordatorioDao(),
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate("add_reminder/$mascotaId") }
            )
        }
        composable(
            route = "add_reminder/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            AgregarRecordatorioScreen(
                mascotaId = backStack.arguments?.getInt("id") ?: -1,
                recordatorioDao = db.recordatorioDao(),
                historialDao = db.historialMedicoDao()
            ) { navController.popBackStack() }
        }
    }
}
