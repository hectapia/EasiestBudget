package com.easybudget.easiestbudget.database

import androidx.room.*
import com.easybudget.easiestbudget.models.Budget
import com.easybudget.easiestbudget.models.Expense
import com.easybudget.easiestbudget.models.User
import kotlinx.coroutines.flow.Flow

/**
 * Data class representing a User and their associated Budget using Room's Relationship mapping.
 */
data class UserWithBudget(
    @Embedded val user: User, // Embed the User entity
    @Relation(
        parentColumn = "id", // The primary key in User
        entityColumn = "userId" // The foreign key in Budget
    )
    val budget: Budget? // The associated Budget, if any
)

/**
 * Data Access Object (DAO) for EasiestBudget.
 * Defines the SQL queries and database operations for Users, Budgets, and Expenses.
 */
@Dao
interface AppDao {

    // --- User Queries ---
    /**
     * Inserts a new user into the database.
     * @return The row ID of the newly inserted user.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    /**
     * Retrieves all users as a reactive Flow.
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Retrieves all users along with their budgets using a Transaction.
     */
    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithBudgets(): Flow<List<UserWithBudget>>

    /**
     * Finds a specific user by their ID.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    // --- Budget Queries ---
    /**
     * Inserts or updates a budget.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    /**
     * Updates an existing budget's details.
     */
    @Update
    suspend fun updateBudget(budget: Budget)

    /**
     * Retrieves the budget for a specific user as a reactive Flow.
     */
    @Query("SELECT * FROM budgets WHERE userId = :userId LIMIT 1")
    fun getBudgetForUser(userId: Int): Flow<Budget?>

    /**
     * Finds a specific budget by its ID.
     */
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Int): Budget?

    // --- Expense Queries ---
    /**
     * Inserts a new expense record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    /**
     * Updates an existing expense record.
     */
    @Update
    suspend fun updateExpense(expense: Expense)

    /**
     * Deletes a specific expense record.
     */
    @Delete
    suspend fun deleteExpense(expense: Expense)

    /**
     * Retrieves all expenses associated with a specific user.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId")
    fun getExpensesForUser(userId: Int): Flow<List<Expense>>

    /**
     * Finds a specific expense by its ID.
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Int): Expense?

    /**
     * Calculates the total amount spent for a specific budget.
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE budgetId = :budgetId")
    fun getTotalSpentForBudget(budgetId: Int): Flow<Double?>

    // --- Deletion ---
    /**
     * Deletes a user record. Cascading behavior should handle associated data if configured.
     */
    @Delete
    suspend fun deleteUser(user: User)
}