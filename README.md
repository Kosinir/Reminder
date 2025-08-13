# Reminder - Aplikacja mobilna

## Prezentacja działania:

https://youtu.be/AqAUxKYYkp4

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

<img width="494" height="289" alt="image" src="https://github.com/user-attachments/assets/08d1c966-413c-4d16-816a-39c4ee63c360" />

<img width="599" height="516" alt="image" src="https://github.com/user-attachments/assets/53777cb0-3fac-4102-b33a-168dd5955e35" />

**Konwertery**
 Converters: zamiana RepeatInterval <-> String, List<Boolean> <-> String.

 <img width="599" height="306" alt="image" src="https://github.com/user-attachments/assets/3ce271ed-8279-4620-b8e2-ba2dd593ce13" />

 <img width="596" height="261" alt="image" src="https://github.com/user-attachments/assets/4bb97b22-9fe3-4a82-9bd1-949c1feb3354" />

StringListConverters: zamiana List<String> <-> JSON poprzez Gson.

<img width="744" height="250" alt="image" src="https://github.com/user-attachments/assets/c1d03963-352b-4eed-8c8a-d850dffe63b8" />

