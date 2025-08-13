package com.example.remainder.calendargrep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    completedDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val today = YearMonth.now()
    val firstDayOfWeek = remember { DayOfWeek.MONDAY }
    val calendarState = rememberCalendarState(
        firstVisibleMonth = today,
        startMonth = today.minusMonths(12),
        endMonth = today.plusMonths(12),
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(Unit) {
        calendarState.scrollToMonth(today)
    }

    Scaffold(
        topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text("Calendar")
            }
        )
    }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Nagłówek z nawigacją między miesiącami
            MonthNavigator(calendarState)

            HorizontalCalendar(
                state = calendarState,
                dayContent = { day: CalendarDay ->
                    val isCompleted = day.date in completedDates
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(2.dp)
                            .then(
                                if (isCompleted) Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                else Modifier
                            )
                            .clickable { onDateSelected(day.date) },
                        contentAlignment = Alignment.Center
                    ) {
                        val color =
                            if (day.position == DayPosition.MonthDate) LocalContentColor.current
                            else LocalContentColor.current.copy(alpha = 0.3f)
                        Text(text = day.date.dayOfMonth.toString(), color = color)
                    }
                }
            )
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                text = "Completed Tasks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )

        }
    }
}

@Composable
fun MonthNavigator(calendarState: CalendarState) {
    val coroutineScope = rememberCoroutineScope()
    val currentMonth: YearMonth = calendarState.firstVisibleMonth.yearMonth

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {
            coroutineScope.launch {
                calendarState.animateScrollToMonth(currentMonth.minusMonths(1))
            }
        }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Poprzedni miesiąc")
        }

        Text(
            text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = {
            coroutineScope.launch {
                calendarState.animateScrollToMonth(currentMonth.plusMonths(1))
            }
        }) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Następny miesiąc")
        }
    }
}
