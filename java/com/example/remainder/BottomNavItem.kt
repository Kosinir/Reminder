package com.example.remainder

import androidx.annotation.DrawableRes
import com.example.remainder.R

sealed class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    object Home : BottomNavItem("home", "Home", R.drawable.ic_home)
    object Calendar : BottomNavItem("calendar", "Calendar", R.drawable.ic_calendar)
    object Notepad : BottomNavItem("notepad", "Notepad", R.drawable.ic_notepad)

    companion object {
        val items = listOf(Home, Calendar, Notepad)
    }
}
