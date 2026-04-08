package com.easybudget.easiestbudget.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Room Entity representing an individual Expense record.
 * Includes relationships to both User and Budget entities with cascading deletion.
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Delete expense if the associated user is deleted
        ),
        ForeignKey(
            entity = Budget::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE // Delete expense if the associated budget is deleted
        )
    ]
)
data class Expense(
    /** Unique ID for the expense record. */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** ID of the User who made this expense. */
    val userId: Int,
    /** ID of the Budget category this expense belongs to. */
    val budgetId: Int,
    /** The name/description of the expense (e.g., "Apples"). */
    val name: String,
    /** The cost of the expense. */
    val amount: Double,
    /** The date the expense was recorded (stored as a String). */
    val date: String
)