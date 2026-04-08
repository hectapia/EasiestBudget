package com.easybudget.easiestbudget.database

import androidx.room.*
import com.easybudget.easiestbudget.models.Budget
import com.easybudget.easiestbudget.models.Expense
import com.easybudget.easiestbudget.models.User
import kotlinx.coroutines.flow.Flow

data class UserWithBudget(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val budget: Budget?
)

@Dao
interface AppDao {

    // --- User Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithBudgets(): Flow<List<UserWithBudget>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    // --- Budget Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId LIMIT 1")
    fun getBudgetForUser(userId: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Int): Budget?

    // --- Expense Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    fun getExpensesForUser(userId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Int): Expense?

    @Query("SELECT SUM(amount) FROM expenses WHERE budgetId = :budgetId")
    fun getTotalSpentForBudget(budgetId: Int): Flow<Double?>

    // --- Deletion ---
    @Delete
    suspend fun deleteUser(user: User)
}