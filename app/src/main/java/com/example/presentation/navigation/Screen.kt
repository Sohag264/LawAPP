package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AllLaws : Screen("all_laws")
    data object Search : Screen("search")
    data object Bookmarks : Screen("bookmarks")
    data object Settings : Screen("settings")
    data object LawReader : Screen("law_reader/{lawId}?sectionId={sectionId}") {
        fun createRoute(lawId: Long, sectionId: Long? = null): String {
            return if (sectionId != null) {
                "law_reader/$lawId?sectionId=$sectionId"
            } else {
                "law_reader/$lawId"
            }
        }
    }
}

enum class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        title = "মূলপাতা",
        route = Screen.Dashboard.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    ALL_LAWS(
        title = "সকল আইন",
        route = Screen.AllLaws.route,
        selectedIcon = Icons.Filled.FolderSpecial,
        unselectedIcon = Icons.Outlined.Folder,
        testTag = "nav_laws"
    ),
    SEARCH(
        title = "অনুসন্ধান",
        route = Screen.Search.route,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        testTag = "nav_search"
    ),
    BOOKMARKS(
        title = "বুকমার্ক ও নোট",
        route = Screen.Bookmarks.route,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        testTag = "nav_bookmarks"
    ),
    SETTINGS(
        title = "সেটিংস",
        route = Screen.Settings.route,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
}
