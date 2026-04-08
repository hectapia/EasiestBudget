package com.easybudget.easiestbudget.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val category: String,
    val limitAmount: Double
)