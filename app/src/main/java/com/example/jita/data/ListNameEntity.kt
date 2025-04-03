package com.example.jita.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_names")
data class ListNameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    // Add an order index if you want to persist the drag-and-drop order
    // val orderIndex: Int
) 