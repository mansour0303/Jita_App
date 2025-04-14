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
        ReminderEntity::class
    ],
    version = 2,  // Increment version number
    exportSchema = false
)
@TypeConverters(Converters::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun listNameDao(): ListNameDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 2 to 3 (adding imagePaths and filePaths lists)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create temporary table with new schema
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
                        filePaths TEXT NOT NULL DEFAULT '[]'
                    )
                    """
                )
                
                // Copy data from old table to new, converting single paths to lists
                database.execSQL(
                    """
                    INSERT INTO tasks_new (
                        id, name, description, dueDate, priority, list, 
                        trackedTimeMillis, isTracking, trackingStartTime, completed,
                        imagePaths, filePaths
                    ) 
                    SELECT 
                        id, name, description, dueDate, priority, list, 
                        trackedTimeMillis, isTracking, trackingStartTime, completed,
                        CASE WHEN imagePath IS NULL THEN '[]' ELSE '[' || '"' || imagePath || '"' || ']' END,
                        CASE WHEN filePath IS NULL THEN '[]' ELSE '[' || '"' || filePath || '"' || ']' END
                    FROM tasks
                    """
                )
                
                // Drop old table
                database.execSQL("DROP TABLE tasks")
                
                // Rename new table to match original name
                database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }
        }

        // Migration from version 3 to 4 (adding notes and folders tables)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create folders table
                database.execSQL(
                    """
                    CREATE TABLE folders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        parentId INTEGER,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (parentId) REFERENCES folders(id) ON DELETE CASCADE
                    )
                    """
                )
                
                // Create notes table
                database.execSQL(
                    """
                    CREATE TABLE notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        folderId INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (folderId) REFERENCES folders(id) ON DELETE CASCADE
                    )
                    """
                )
            }
        }

        // Migration from version 4 to 5 (handling schema changes)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This empty migration handles any schema hash changes
                // without requiring specific schema modifications
            }
        }

        // Migration from version 5 to 6 (adding styles column to notes table)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN styles TEXT")
            }
        }

        // Migration from version 6 to 7 (making folderId nullable in notes table)
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create temporary table with new schema
                database.execSQL(
                    """
                    CREATE TABLE notes_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        folderId INTEGER,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        styles TEXT,
                        FOREIGN KEY (folderId) REFERENCES folders(id) ON DELETE CASCADE
                    )
                    """
                )
                
                // Copy data from old table to new
                database.execSQL(
                    """
                    INSERT INTO notes_new (
                        id, title, content, folderId, createdAt, updatedAt, styles
                    ) 
                    SELECT 
                        id, title, content, folderId, createdAt, updatedAt, styles
                    FROM notes
                    """
                )
                
                // Drop old table
                database.execSQL("DROP TABLE notes")
                
                // Rename new table to match original name
                database.execSQL("ALTER TABLE notes_new RENAME TO notes")
            }
        }

        // Migration from version 7 to 8 (adding checkboxItems column to notes table)
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new column
                database.execSQL("ALTER TABLE notes ADD COLUMN checkboxItems TEXT")
            }
        }

        // Migration from version 8 to 9 (adding voiceRecordings and fileAttachments columns to notes table)
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check and add columns only if they don't exist
                // First, get column information for the notes table
                val tableInfo = database.query("PRAGMA table_info(notes)")
                val columnNames = mutableSetOf<String>()
                
                // Collect existing column names
                tableInfo.use {
                    while (it.moveToNext()) {
                        val columnName = it.getString(it.getColumnIndex("name"))
                        columnNames.add(columnName)
                    }
                }
                
                // Add voiceRecordings column if it doesn't exist
                if (!columnNames.contains("voiceRecordings")) {
                    database.execSQL("ALTER TABLE notes ADD COLUMN voiceRecordings TEXT")
                }
                
                // Add fileAttachments column if it doesn't exist
                if (!columnNames.contains("fileAttachments")) {
                    database.execSQL("ALTER TABLE notes ADD COLUMN fileAttachments TEXT")
                }
                
                // Add color column if it doesn't exist
                if (!columnNames.contains("color")) {
                    database.execSQL("ALTER TABLE notes ADD COLUMN color TEXT")
                }
                
                // Add isArchived column if it doesn't exist
                if (!columnNames.contains("isArchived")) {
                    database.execSQL("ALTER TABLE notes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                }
                
                // Add isPinned column if it doesn't exist
                if (!columnNames.contains("isPinned")) {
                    database.execSQL("ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                }
                
                // Add isDeleted column if it doesn't exist
                if (!columnNames.contains("isDeleted")) {
                    database.execSQL("ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        // Migration from version 9 to 10 (adding imageAttachments column to notes table)
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add imageAttachments column if it doesn't exist
                val tableInfo = database.query("PRAGMA table_info(notes)")
                val columnNames = mutableSetOf<String>()
                
                // Collect existing column names
                tableInfo.use {
                    while (it.moveToNext()) {
                        val columnName = it.getString(it.getColumnIndex("name"))
                        columnNames.add(columnName)
                    }
                }
                
                // Add imageAttachments column if it doesn't exist
                if (!columnNames.contains("imageAttachments")) {
                    database.execSQL("ALTER TABLE notes ADD COLUMN imageAttachments TEXT")
                }
            }
        }

        // Migration from 10 to 11 - Add color column to folders table
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add color column to folders table
                database.execSQL("ALTER TABLE folders ADD COLUMN color TEXT")
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
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .fallbackToDestructiveMigration() // Add this to handle severe migration issues
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
} 
