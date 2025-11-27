package com.example.walert.navigation

sealed class AppScreens(val route: String) {
    object LoginScreen : AppScreens("login")
    object RegisterScreen : AppScreens("register")
    object MainScreen : AppScreens("main")
    object ProfileScreen : AppScreens("profile")
    object SettingsScreen : AppScreens("settings")
    object AchievementsScreen : AppScreens("achievements")
    object CalendarScreen : AppScreens("calendar")
    object ThemeScreen : AppScreens("theme")
    object GoalScreen : AppScreens("goals")
    object HistoryScreen : AppScreens("history")
    object NotificationsScreen : AppScreens("notifications")
    object HelpScreen : AppScreens("help")
    object AboutScreen : AppScreens("about")
    object RemindersScreen : AppScreens("reminders")
    object StatisticsScreen : AppScreens("statistics")
    object StoreScreen : AppScreens("store")
    object ThemeSelectionScreen : AppScreens("theme_selection")
}
