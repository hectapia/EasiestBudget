package com.easybudget.easiestbudget.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Budget associated with a User.
 * Includes a foreign key relationship to the User entity with cascading deletion.
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Delete budget if the user is deleted
        )
    ]
)
data class Budget(
    /** Unique ID for the budget. */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** ID of the User who owns this budget. */
    val userId: Int,
    /** The category of spending (e.g., "Groceries"). */
    val category: String,
    /** The maximum amount allocated for this category. */
    val limitAmount: Double
)