package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presentation.LawViewModel
import com.example.presentation.bookmarks.BookmarksNotesScreen
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.lawlist.AllLawsScreen
import com.example.presentation.lawreader.LawReaderScreen
import com.example.presentation.navigation.BottomNavItem
import com.example.presentation.navigation.Screen
import com.example.presentation.search.SearchScreen
import com.example.presentation.settings.SettingsScreen
import com.example.ui.theme.BangladeshLawsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LawViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkThemePref by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val useDarkTheme = isDarkThemePref ?: isSystemInDarkTheme()

            BangladeshLawsTheme(darkTheme = useDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: LawViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isReaderScreen = currentRoute?.startsWith("law_reader") == true

    Scaffold(
        bottomBar = {
            if (!isReaderScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    val bottomNavItems = BottomNavItem.entries
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToLaw = { lawId ->
                        navController.navigate(Screen.LawReader.createRoute(lawId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToAllLaws = { categoryId ->
                        viewModel.selectCategory(categoryId)
                        navController.navigate(Screen.AllLaws.route)
                    }
                )
            }

            composable(Screen.AllLaws.route) {
                AllLawsScreen(
                    viewModel = viewModel,
                    onNavigateToLaw = { lawId ->
                        navController.navigate(Screen.LawReader.createRoute(lawId))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToLawReader = { lawId, sectionId ->
                        navController.navigate(Screen.LawReader.createRoute(lawId, sectionId))
                    }
                )
            }

            composable(Screen.Bookmarks.route) {
                BookmarksNotesScreen(
                    viewModel = viewModel,
                    onNavigateToLawReader = { lawId, sectionId ->
                        navController.navigate(Screen.LawReader.createRoute(lawId, sectionId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.LawReader.route,
                arguments = listOf(
                    navArgument("lawId") { type = NavType.LongType },
                    navArgument("sectionId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val lawId = backStackEntry.arguments?.getLong("lawId") ?: 1L
                val sectionIdStr = backStackEntry.arguments?.getString("sectionId")
                val sectionId = sectionIdStr?.toLongOrNull()

                LawReaderScreen(
                    lawId = lawId,
                    targetSectionId = sectionId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
