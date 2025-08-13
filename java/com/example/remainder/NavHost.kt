// NavHost.kt
package com.example.remainder

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.remainder.calendargrep.CalendarScreen
import com.example.remainder.calendargrep.DoneTasksScreen
import com.example.remainder.creattaskgrep.TaskWizard
import com.example.remainder.edittaskgrep.EditTaskWizard
import com.example.remainder.notegrep.CreateNoteScreen
import com.example.remainder.notegrep.EditNoteScreen
import com.example.remainder.notegrep.NotepadScreen
import com.example.remainder.viewmodel.NotesViewModel
import com.example.remainder.viewmodel.TasksViewModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.remainder.ui.SplashScreen
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReminderApp(
    notesViewModel: NotesViewModel,
    tasksViewModel: TasksViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val notes by notesViewModel.notes.collectAsState(initial = emptyList())
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val items = listOf(
        BottomNavItem.Calendar,
        BottomNavItem.Home,
        BottomNavItem.Notepad
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var isDeleteMode by remember { mutableStateOf(false) }
    var selectedNoteId by remember { mutableStateOf<Int?>(null) }

    val showBottomBar = currentRoute in items.map { it.route }
    val showTopBar = showBottomBar || currentRoute in listOf(
        "createTask", "createNote", "doneTasks", "settings", "editNote"
    )

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("Smart Reminder") },
                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = "Settings"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar && !isLandscape) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isDeleteMode && (currentRoute == BottomNavItem.Home.route || currentRoute == BottomNavItem.Notepad.route)) {
                val label = if (currentRoute == BottomNavItem.Home.route) "Add Task" else "Add Note"
                val destination =
                    if (currentRoute == BottomNavItem.Home.route) "createTask" else "createNote"

                Box(modifier = Modifier.fillMaxWidth()) {
                    FloatingActionButton(
                        onClick = { navController.navigate(destination) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(100.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceBright
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = label)
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        val routeOrder = listOf(
            BottomNavItem.Calendar.route,
            BottomNavItem.Home.route,
            BottomNavItem.Notepad.route
        )

        var offsetX by remember { mutableStateOf(0f) }
        val swipeThreshold = 100f

        if (isLandscape && currentRoute == BottomNavItem.Notepad.route) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    NotepadScreen(
                        navController = navController,
                        notesViewModel = notesViewModel,
                        onDeleteModeChanged = { isDeleteMode = it },
                        onNoteSelected = { selectedNoteId = it }
                    )
                }

                LaunchedEffect(notes, selectedNoteId) {
                    if (selectedNoteId == null && notes.isNotEmpty()) {
                        selectedNoteId = notes.first().id
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        selectedNoteId != null -> {
                            EditNoteScreen(
                                navController = navController,
                                notesViewModel = notesViewModel,
                                id = selectedNoteId!!,
                                showTopBar = false
                            )
                        }

                        notes.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Brak notatek")
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLandscape && showBottomBar) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(72.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        items.forEach { item ->
                            IconButton(
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = item.label,
                                        tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentRoute) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount -> offsetX += dragAmount },
                                onDragEnd = {
                                    val currentIndex = routeOrder.indexOf(currentRoute)
                                    val nextIndex = when {
                                        offsetX > swipeThreshold -> (currentIndex - 1 + routeOrder.size) % routeOrder.size
                                        offsetX < -swipeThreshold -> (currentIndex + 1) % routeOrder.size
                                        else -> currentIndex
                                    }
                                    if (nextIndex != currentIndex) {
                                        navController.navigate(routeOrder[nextIndex]) {
                                            launchSingleTop = true
                                        }
                                    }
                                    offsetX = 0f
                                }
                            )
                        }
                ) {
                    if (isLandscape && currentRoute == BottomNavItem.Notepad.route) {
                        Row(Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                NotepadScreen(
                                    navController = navController,
                                    notesViewModel = notesViewModel,
                                    onDeleteModeChanged = { isDeleteMode = it },
                                    onNoteSelected = { selectedNoteId = it }
                                )
                            }

                            LaunchedEffect(notes, selectedNoteId) {
                                if (selectedNoteId == null && notes.isNotEmpty()) {
                                    selectedNoteId = notes.first().id
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                when {
                                    selectedNoteId != null -> {
                                        EditNoteScreen(
                                            navController = navController,
                                            notesViewModel = notesViewModel,
                                            id = selectedNoteId!!,
                                            showTopBar = false
                                        )
                                    }

                                    notes.isEmpty() -> {
                                        Box(
                                            Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Brak notatek")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = "splash",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("splash") {
                                SplashScreen(
                                    onTimeout = {
                                        navController.navigate(BottomNavItem.Home.route) {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("createNote") {
                                CreateNoteScreen(navController, notesViewModel)
                            }
                            composable(
                                route = "editNote/{noteId}",
                                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                            ) {
                                val id = it.arguments!!.getInt("noteId")
                                EditNoteScreen(navController, notesViewModel, id)
                            }
                            composable(BottomNavItem.Notepad.route) {
                                NotepadScreen(
                                    navController, notesViewModel,
                                    onDeleteModeChanged = { isDeleteMode = it },
                                    onNoteSelected = {
                                        if (!isLandscape) {
                                            navController.navigate("editNote/$it")
                                        } else {
                                            selectedNoteId = it
                                        }
                                    }
                                )
                            }
                            composable(BottomNavItem.Home.route) {
                                MainScreen(navController, tasksViewModel) { isDeleteMode = it }
                            }
                            composable(
                                route = "editTask/{taskId}",
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) {
                                val id = it.arguments!!.getInt("taskId")
                                EditTaskWizard(navController, tasksViewModel, id)
                            }
                            composable(BottomNavItem.Calendar.route) {
                                val completedDates by tasksViewModel.completedDates.collectAsState(
                                    initial = emptySet()
                                )
                                CalendarScreen(navController, completedDates)
                            }
                            composable("createTask") {
                                TaskWizard(navController, tasksViewModel)
                            }
                            composable("doneTasks") { DoneTasksScreen(navController) }
                            composable("settings") {
                                SettingsScreen(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = onToggleTheme,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
