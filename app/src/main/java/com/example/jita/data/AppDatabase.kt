package com.example.jita.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskEntity::class, ListNameEntity::class, NoteEntity::class, FolderEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun listNameDao(): ListNameDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

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

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration() // Add this to handle severe migration issues
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
} 
