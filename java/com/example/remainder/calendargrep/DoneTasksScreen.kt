package com.example.remainder.calendargrep

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DoneTasksScreen(navController: NavController) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("DoneTasks Screen", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
