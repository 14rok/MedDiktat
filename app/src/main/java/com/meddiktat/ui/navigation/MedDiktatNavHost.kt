package com.meddiktat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meddiktat.ui.detail.DictationDetailScreen
import com.meddiktat.ui.list.DictationListScreen
import com.meddiktat.ui.record.RecordScreen

/** Verdrahtet die Screens. Jeder Screen bezieht sein ViewModel via hiltViewModel(). */
@Composable
fun MedDiktatNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            DictationListScreen(
                onRecordClick = { navController.navigate(Routes.RECORD) },
                onDictationClick = { id -> navController.navigate(Routes.detail(id)) },
            )
        }

        composable(Routes.RECORD) {
            RecordScreen(onFinished = { navController.popBackStack() })
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_DICTATION_ID) { type = NavType.StringType },
            ),
        ) {
            DictationDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
