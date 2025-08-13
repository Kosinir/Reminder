package com.example.remainder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.remainder.data.notes.NoteDao
import com.example.remainder.data.notes.NoteEntity
import com.example.remainder.data.tasks.Converters
import com.example.remainder.data.tasks.StringListConverters
import com.example.remainder.data.tasks.TaskDao
import com.example.remainder.data.tasks.TaskEntity


@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class
               ],
    version = 1,
    exportSchema = false
)

@TypeConverters(StringListConverters::class, Converters::class, Converters.BooleanListConverter::class)
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
        database.execSQL("ALTER TABLE tasks ADD COLUMN tasksDone TEXT NOT NULL DEFAULT 'false'")
    }
}

