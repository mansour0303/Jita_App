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
    version = 4,
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

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
} 
