package com.example.jita.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ListNameEntity::class, 
        TaskEntity::class, 
        NoteEntity::class, 
        FolderEntity::class,
        ReminderEntity::class,
        TimeLogEntryEntity::class
    ],
    version = 14,  // Increment version number from 13 to 14
    exportSchema = false
)
@TypeConverters(Converters::class, StringListConverter::class, IntListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun listNameDao(): ListNameDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun reminderDao(): ReminderDao
    abstract fun timeLogDao(): TimeLogDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from 13 to 14 - Add time_logs table
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create time_logs table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS time_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        durationMillis INTEGER NOT NULL,
                        FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE
                    )
                    """
                )
            }
        }

        // Migration from 12 to 13 - Add completedSubtasks column to tasks table
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add completedSubtasks column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN completedSubtasks TEXT NOT NULL DEFAULT '[]'")
            }
        }

        // Migration from 11 to 12 - Add subtasks column to tasks table
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add subtasks column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN subtasks TEXT NOT NULL DEFAULT '[]'")
            }
        }
        
        // Direct migration from version 2 to 14
        private val MIGRATION_2_14 = object : Migration(2, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new tasks table with all the required columns
                database.execSQL(
                    """
                    CREATE TABLE tasks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        dueDate INTEGER NOT NULL,
                        priority TEXT NOT NULL,
                        list TEXT,
                        trackedTimeMillis INTEGER NOT NULL DEFAULT 0,
                        isTracking INTEGER NOT NULL DEFAULT 0,
                        trackingStartTime INTEGER NOT NULL DEFAULT 0,
                        completed INTEGER NOT NULL DEFAULT 0,
                        imagePaths TEXT NOT NULL DEFAULT '[]',
                        filePaths TEXT NOT NULL DEFAULT '[]',
                        subtasks TEXT NOT NULL DEFAULT '[]',
                        completedSubtasks TEXT NOT NULL DEFAULT '[]'
                    )
                    """
                )
                
                // Copy data from old table to new
                database.execSQL(
                    """
                    INSERT INTO tasks_new (
                        id, name, description, dueDate, priority, list, 
                        trackedTimeMillis, isTracking, trackingStartTime, completed
                    ) 
                    SELECT 
                        id, name, description, dueDate, priority, list, 
                        trackedTimeMillis, isTracking, trackingStartTime, completed
                    FROM tasks
                    """
                )
                
                // Drop old table
                database.execSQL("DROP TABLE tasks")
                
                // Rename new table to match original name
                database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")

                // Create the notes and folders tables
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS folders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        parentId INTEGER,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        color TEXT,
                        FOREIGN KEY (parentId) REFERENCES folders(id) ON DELETE CASCADE
                    )
                    """
                )
                
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        folderId INTEGER,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        styles TEXT,
                        checkboxItems TEXT,
                        voiceRecordings TEXT,
                        fileAttachments TEXT,
                        color TEXT,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        isPinned INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        imageAttachments TEXT,
                        FOREIGN KEY (folderId) REFERENCES folders(id) ON DELETE CASCADE
                    )
                    """
                )
                
                // Create reminders table if it doesn't exist yet
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        message TEXT NOT NULL,
                        timeInMillis INTEGER NOT NULL,
                        alarmSoundEnabled INTEGER NOT NULL,
                        vibrationEnabled INTEGER NOT NULL,
                        soundUri TEXT,
                        soundName TEXT,
                        attachedTaskIds TEXT NOT NULL DEFAULT ''
                    )
                    """
                )
                
                // Create list_names table if it doesn't exist yet
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS list_names (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """
                )
                
                // Create time_logs table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS time_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        durationMillis INTEGER NOT NULL,
                        FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE
                    )
                    """
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                // Use the migrations
                .addMigrations(MIGRATION_2_14, MIGRATION_13_14)
                .fallbackToDestructiveMigration() // Add this to handle severe migration issues
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
} 
