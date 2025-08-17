# Reminder - Aplikacja mobilna

## Prezentacja działania:

https://youtu.be/pJYmCsCQt9A

## Opis projektu

Smart Reminder to menedżer zadań i notatek. Pozwala użytkownikowi na tworzenie, przeglądanie i
edytowanie zadań oraz notatek, z możliwością zarządzania powtarzalnymi terminami i
otrzymywania powiadomień o zaplanowanych zadaniach.

## Struktura projektu

<img width="244" height="622" alt="image" src="https://github.com/user-attachments/assets/d70a589d-d916-4c63-9d3b-41831af822fb" />

- calendargrep – widoki kalendarza i listy wykonanych zadań.
- createtaskgrep / edittaskgrep – kreatory (wizard) tworzenia i edycji zadań.
- notegrep – ekrany tworzenia, przeglądu i edycji notatek.
- db – konfiguracja bazy Room (AppDatabase).
- data / tasks – encje, DAO, repository, konwertery.
- viewmodel – klasy ViewModel i fabryki.
- ui.theme – definicje kolorów, typografii i motywu Material3.

## Architektura warstwowa

- Presentation (Screens, NavHost.kt): Renderowanie UI, obsługa zdarzeń i nawigacji
- ViewModel (TasksViewModel, NotesViewModel): Przekazywanie danych do UI, logika komunikacji z repo
- Domain/Repo (TasksRepository, NoteesRepository): Agregacja i transformacja danych z DAO
- Data Access (TaskDao, NoteDao): Definicje zapytań SQL, operacje CRUD
- Database (AppDatabase): Konfiguracja Room, migracje, TypeConverters

## Baza danych (Room)

**AppDatabase.kt**

- Konfiguruje Room: rejestruje encje NoteEntity i TaskEntity, ustawia wersję bazy.
- Rejestruje konwertery do przechowywania list i enumów jako tekst.
- Metoda build(context) tworzy jedyny egzemplarz bazy, dodając migrację
- MIGRATION_5_6 (dodaje kolumnę tasksDone).

**Migracja (MIGRATION_5_6)**

Modyfikuje istniejącą tabelę tasks, dodając kolumnę tasksDone, by móc
przechowywać historię zakończonych zadań (jest to próba dodania migracji do
projektu, ponieważ przedtem był destruktywna migracja).

```kotin

@Database(

 entities = [

 NoteEntity::class,

 TaskEntity::class

 ],

 version = 1,

 exportSchema = false

)

@TypeConverters(StringListConverters::class, Converters::class,

Converters.BooleanListConverter::class)

abstract class AppDatabase : RoomDatabase() {

 abstract fun taskDao(): TaskDao

 abstract fun noteDao(): NoteDao

 companion object {

 fun build(context: Context): AppDatabase {

 return Room.databaseBuilder(

 context.applicationContext,

 AppDatabase::class.java,

 "remainder_database"

 )

 .addMigrations(

 MIGRATION_5_6

 )

 .build()

 }

 }

}

val MIGRATION_5_6 = object : Migration(5, 6) {

 override fun migrate(database: SupportSQLiteDatabase) {

 database.execSQL("ALTER TABLE tasks ADD COLUMN tasksDone TEXT NOT NULL

DEFAULT 'false'")

 }

}

```

**Konwertery**

 Converters: zamiana RepeatInterval <-> String, List<Boolean> <-> String.

```kotlin

class Converters {

@TypeConverter

fun fromRepeatInterval(value: RepeatInterval): String = value.name

@TypeConverter

fun toRepeatInterval(value: String): RepeatInterval =

RepeatInterval.valueOf(value)

class BooleanListConverter {

@TypeConverter

fun fromBooleanList(list: List<Boolean>): String {

return list.joinToString(",") { it.toString() }

@TypeConverter

fun toBooleanList(data: String): List<Boolean> {

return data.split(",").mapNotNull {

when(it.trim()){

"true" -> true

"false" -> false

else -> null

```

StringListConverters: zamiana List<String> <-> JSON poprzez Gson.

```kotlin

class StringListConverters {

private val gson = Gson()

private val type = object : TypeToken<List<String>>() {}.type

@TypeConverter

fun fromList(list: List<String>): String = gson.toJson(list)

@TypeConverter

fun toList(json: String): List<String> = gson.fromJson(json, type)

}
```

### Encje i dostęp do danych (tasks)

**TaskEntity.kt**
Model zadania zawiera m.in. tytuł, opis, termin, powtarzalność, listę tagów oraz tablicę statusów dni

```kotlin

@Entity(tableName = "tasks")

data class TaskEntity(

@PrimaryKey(autoGenerate = true) val id: Int = 0,

val listTitle: String,

val tasksList: List<String>,

val dueTimestamp: Long,

val isRepeating: Boolean = false,

val repeatInterval: RepeatInterval = RepeatInterval.NONE,

val repeatEndTimestamp: Long? = null,

val isDone: Boolean = false,

val completedTimestamp: Long? = null,

val createdAt: Long = System.currentTimeMillis(),

val tasksDone: List<Boolean> = List(tasksList.size) { false }

```

**TaskDao.kt**

- Metody CRUD: wstawianie, aktualizacja, usuwanie.
  
- Zapytania zwracające zadania na konkretną datę lub zbiór ukończonych dat.

```kotlin

@Dao

interface TaskDao {

@Query("SELECT * FROM tasks")

fun getAllTasks(): Flow<List<TaskEntity>>

@Query("SELECT * FROM tasks WHERE isDone = 0 and isRepeating = 0")

fun getPendingTasks(): Flow<List<TaskEntity>>

@Query("SELECT * FROM tasks WHERE isDone = 1")

fun getDoneTasks(): Flow<List<TaskEntity>>

@Insert(onConflict = OnConflictStrategy.REPLACE)

suspend fun insert(task: TaskEntity)

@Update

suspend fun update(task: TaskEntity)

@Delete

suspend fun delete(task: TaskEntity)

@Query("SELECT * FROM tasks WHERE isRepeating = 0")

fun getOneTimeTasks(): Flow<List<TaskEntity>>

@Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")

fun getById(id: Int): Flow<TaskEntity?>

@Query("""

SELECT * FROM tasks

WHERE completedTimestamp BETWEEN :dayStart AND :dayEnd

ORDER BY completedTimestamp ASC

""")

fun getCompletedTasksBetween(dayStart: Long, dayEnd: Long): Flow<List<TaskEntity>>

@Query("""

SELECT DISTINCT date(completedTimestamp / 1000, 'unixepoch')

FROM tasks

WHERE completedTimestamp IS NOT NULL

""")

fun getCompletedDays(): Flow<List<String>>

}

```

**TasksRepository.kt**

- Przy zapisie zadania planuje alarmy w AlarmManager.

- Dostarcza ViewModelowi listę zadań i dat wykonania do obsługi kalendarza.

**TasksViewModel.kt, TasksViewModelFactory.kt**
  
- Steruje komunikacją między UI a repozytorium.

- Eksponuje Flow gotowy do obserwacji w Compose.

### Moduł notatek (notegrep)

**CreateNoteScreen.kt**

- Ekran składa się z pola tekstowego, które użytkownik wypełnia treścią notatki, oraz przycisku Zapisz.

- Po kliknięciu wywołuje ViewModel, który przekazuje nową notatkę do repozytorium.

<img width="265" height="596" alt="image" src="https://github.com/user-attachments/assets/6f14e1d3-d86a-4f1d-ac1e-111f760138af" />

**NotepadScreen.kt**

- Wyświetla listę notatek w LazyColumn. Każdą notatkę renderuje jako kartę z treścią i datą.

- Umożliwia włączenie trybu usuwania: długie przytrzymanie usuwa notatkę.

- Obsługuje wybór notatki: w trybie poziomym od razu otwiera jej edycję w sąsiednim panelu.

<img width="345" height="786" alt="image" src="https://github.com/user-attachments/assets/ca8edfba-71ba-4e2d-bf57-afffad2645ac" />

**EditNoteScreen.kt**

- Odczytuje notatkę z bazy po jej identyfikatorze.

- Pozwala użytkownikowi zmienić treść, a następnie aktualizuje wpis w bazie.

<img width="307" height="710" alt="image" src="https://github.com/user-attachments/assets/370f3160-6867-43ad-9c81-f943f8af2fe7" />

**NoteEntity.kt, NoteDao.kt, NotesRepository.kt, NotesViewModel.kt, NotesViewModelFactory.kt**

- Definicja struktury notatki (id, content, createdAt).

- Flow przekazuje strumień notatek do UI.

- Repozytorium izoluje warstwę bazy od ViewModelu.

### Kreator tworzenia zadania (createtaskgrep)

**TaskDateStep.kt**

- Pozwala wybrać datę i godzinę wykonania zadania za pomocą wbudowanych pickerów.

- Lista wyboru częstotliwości powtarzania (codziennie, co tydzień, miesięcznie.).

<img width="320" height="727" alt="image" src="https://github.com/user-attachments/assets/e45e08e9-6924-4c3f-8bea-85a6cac9ead4" />

**TaskInfoStep.kt**

- Pola TextField dla tytułu i zadań.

<img width="305" height="684" alt="image" src="https://github.com/user-attachments/assets/ba8a2426-c32b-4d08-bd5f-dad81f6f126c" />

**TaskWizard.kt**

- Łączy kroki w sekwencję; może przechodzić w przód i w tył, weryfikując
poprawność danych przed zapisem.

```kotlin

@Composable

fun TaskWizard(

rootNavController: NavController,

tasksViewModel: TasksViewModel

) {

val wizardNav = rememberNavController()

var listTitle by rememberSaveable { mutableStateOf("") }

var tasksList by rememberSaveable { mutableStateOf(emptyList<String>()) }

var dueTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

var isRepeating by rememberSaveable { mutableStateOf(false) }

var repeatInterval by rememberSaveable { mutableStateOf(RepeatInterval.NONE) }

var repeatEndTimestamp by rememberSaveable { mutableStateOf(dueTimestamp) }

var tasksDone by rememberSaveable { mutableStateOf(emptyList<Boolean>()) }

NavHost(

navController = wizardNav,

startDestination = "info"

) {

composable("info") {

TaskInfoStep(

listTitle = listTitle,

onListTitleChange = { listTitle = it },

tasks = tasksList,

onTaskChange = { index, value ->

tasksList = tasksList.toMutableList().also { it[index] = value }

},

onAddTask = {

tasksList = tasksList + ""

tasksDone = tasksDone + false

},

onRemoveTask = { index ->

tasksList = tasksList.toMutableList().also { it.removeAt(index) }

tasksDone = tasksDone.toMutableList().also { it.removeAt(index) }

},

onNext = { wizardNav.navigate("date") },

onBack = { rootNavController.popBackStack() }

)

}

composable("date") {

TaskDateStep(

dueTimestamp = dueTimestamp,

onDateChange = { dueTimestamp = it },

onTimeChange = { dueTimestamp = it },

isRepeating = isRepeating,

onIsRepeatingChange = { isRepeating = it },

repeatInterval = repeatInterval,

onRepeatIntervalChange = { repeatInterval = it },

repeatEndTimestamp = repeatEndTimestamp,

onRepeatEndTimestampChange = { repeatEndTimestamp = it },

onBack = { wizardNav.popBackStack() },

onNext = {

val task = TaskEntity(

listTitle = listTitle,

tasksList = tasksList,

dueTimestamp = dueTimestamp,

isRepeating = isRepeating,

repeatInterval = if (isRepeating) repeatInterval else RepeatInterval.NONE,

repeatEndTimestamp = if (isRepeating) repeatEndTimestamp else null,

isDone = false,

createdAt = System.currentTimeMillis(),

tasksDone = tasksDone

)

tasksViewModel.addTask(task)

rootNavController.popBackStack()

```

### Kreator edycji zadania (edittaskgrep)

**EditTaskDateStep.kt, EditTaskInfoStep.kt**

- Dostosowane do ładowania istniejących danych zadania i ich aktualizacji

<img width="269" height="620" alt="image" src="https://github.com/user-attachments/assets/ac5f2b4c-a98b-4862-80e1-5ba170d2e8bc" />

<img width="284" height="663" alt="image" src="https://github.com/user-attachments/assets/3416c52a-ad25-40d7-9c1b-cfee3041146d" />

**EditTaskWizard.kt**

- Analogiczna logika jak przy tworzeniu, ale dodaje flagi edycyjne i pre-wypełnienie.

### Kalendarz i lista ukończonych zadań (calendargrep)

**CalendarScreen.kt**

- Rysuje widok kalendarza, podświetla daty z zakończonymi zadaniami.

<img width="276" height="639" alt="image" src="https://github.com/user-attachments/assets/7be3214d-f10a-4046-8ba8-7840d31a3f1c" />

### Moduł nawigacji i interakcji (NavHost.kt)
**SmartReminderApp()**
- Konfiguruje główny Scaffold, definiuje górny i dolny pasek nawigacji oraz przycisk akcji.
- Warunkowo pokazuje/ukrywa paski i FAB w zależności od aktualnego ekranu i orientacji.

#### Przesunięcie gestem:
- W poziomie wykrywa ruch palcem w osi X; próg przesunięcia decyduje o
zmianie zakładki.
- Umożliwia przełączanie między widokami kalendarza, listą zadań i
notatnikiem bez dotykania paska.
```kotlin
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
```

#### NavHost:

- Definiuje trasy (splash, createNote, editNote/{noteId}, Notepad, Home, Calendar, createTask, editTask/{taskId}, doneTasks, settings).

- Przekazuje parametry przez navArgument i dynamicznie wprowadza dane do
ekranów.

```kotlin
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

```
### Moduł stylów (ui.theme)

#### Color.kt, Type.kt

- Zestawy kolorów i typografii dostosowane do Material3.

#### Theme.kt

- Tworzy MaterialTheme z ciemnym i jasnym motywem; definiuje spójne style dla komponentów.

#### Moduł powiadomień (TaskReminderReceiver.kt)

#### TaskReminderReceiver

- Odbiera alarm z Androida za pomocą BroadcastReceiver.

- Buduje powiadomienie: ikona, tytuł, czas, kategoria REMINDER.

- Ustawia PendingIntent, by po kliknięciu otworzyć aplikację.

- Jeśli zadanie się powtarza, oblicza datę kolejnego alarmu i rejestruje go ponownie z AlarmManager.

- Obsługuje API 31+ exact alarms oraz fallback do alarmów nieprecyzyjnych

```kotlin

class TaskReminderReceiver : BroadcastReceiver() {

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)

override fun onReceive(context: Context, intent: Intent) {

val taskId = intent.getIntExtra("extra_task_id", -1)

val listTitle = intent.getStringExtra("extra_list_title") ?: "Task"

val timeText = intent.getStringExtra("extra_time_text") ?: ""

val timestamp = intent.getLongExtra("extra_timestamp", 0L)

val isRepeating = intent.getBooleanExtra("extra_is_repeating", false)

val intervalName = intent.getStringExtra("extra_repeat_interval") ?: RepeatInterval.NONE.name

val repeatEnd = intent.getLongExtra("extra_repeat_end", -1L)

// Wyświetlenie powiadomienia

val launchIntent = Intent(context, MainActivity::class.java).apply {

flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

}

val contentPI = PendingIntent.getActivity(

context, taskId, launchIntent,

PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

)

val notif = NotificationCompat.Builder(context, App.CHANNEL_ID_TASK)

.setSmallIcon(R.drawable.ic_launcher_foreground)

.setContentTitle(listTitle)

.setContentText("⏰ $timeText")

.setPriority(NotificationCompat.PRIORITY_HIGH)

.setDefaults(NotificationCompat.DEFAULT_ALL)

.setCategory(NotificationCompat.CATEGORY_REMINDER)

.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

.setContentIntent(contentPI)

.setAutoCancel(true)

.build()

NotificationManagerCompat.from(context)

.notify(taskId, notif)

// Jeśli zadanie się powtarza i nie przekroczyliśmy daty końca — zaplanuj następny

if (isRepeating && intervalName != RepeatInterval.NONE.name) {

val interval = RepeatInterval.valueOf(intervalName)

val nextTs = when (interval) {

RepeatInterval.DAILY -> timestamp + TimeUnit.DAYS.toMillis(1)

RepeatInterval.WEEKLY -> timestamp + TimeUnit.DAYS.toMillis(7)

RepeatInterval.MONTHLY -> Calendar.getInstance().run {

timeInMillis = timestamp

add(Calendar.MONTH, 1)

timeInMillis

}

else -> null

}

if (nextTs != null && (repeatEnd < 0L || nextTs <= repeatEnd)) {

val nextIntent = Intent(context, TaskReminderReceiver::class.java).apply {

putExtras(intent.extras!!)

putExtra("extra_timestamp", nextTs)

}

val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

val pi = PendingIntent.getBroadcast(

context, taskId, nextIntent,

PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

)

try {

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

if (alarmManager.canScheduleExactAlarms()) {

alarmManager.setExactAndAllowWhileIdle(

AlarmManager.RTC_WAKEUP,

nextTs,

pi

)

} else {

alarmManager.set(

AlarmManager.RTC_WAKEUP,

nextTs,

pi

)

}

} else {

alarmManager.setExactAndAllowWhileIdle(

AlarmManager.RTC_WAKEUP,

nextTs,

pi

)

}

} catch (e: SecurityException) {

Log.w("TaskReminderReceiver", "Cannot schedule exact alarm, falling back to inexact", e)

alarmManager.set(

AlarmManager.RTC_WAKEUP,

nextTs,

pi

```
