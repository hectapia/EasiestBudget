package com.easybudget.easiestbudget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.easybudget.easiestbudget.database.AppDatabase
import com.easybudget.easiestbudget.databinding.ItemUpdateDeleteExpenseBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Fragment for updating or deleting an individual Expense record.
 */
class UpdateDeleteExpenseFragment : Fragment() {

    // ViewBinding for layout access
    private var _binding: ItemUpdateDeleteExpenseBinding? = null
    private val binding get() = _binding!!
    
    // Arguments passed via Navigation Safe Args (expenseId)
    private val args: UpdateDeleteExpenseFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = ItemUpdateDeleteExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database DAO
        val dao = AppDatabase.getDatabase(requireContext()).appDao()

        // Return to the expense list without changes
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Load specific expense details and related info (user/budget) to populate the form
        lifecycleScope.launch {
            val expense = dao.getExpenseById(args.expenseId) ?: return@launch
            val user = dao.getUserById(expense.userId)
            val budget = dao.getBudgetById(expense.budgetId) ?: return@launch
            // Calculate current total spent for the parent budget
            val totalSpent = dao.getTotalSpentForBudget(budget.id).first() ?: 0.0

            // Pre-fill the form with current values
            binding.etExpenseName.setText(expense.name)
            binding.etUserName.setText(user?.username ?: "N/A")
            binding.etCategory.setText(budget.category)
            binding.etSumExpenses.setText(totalSpent.toString())
            binding.etBalance.setText((budget.limitAmount - totalSpent).toString())
            binding.etCost.setText(expense.amount.toString())
            binding.etDate.setText(expense.date)

            // Update button action: saves changes to the expense cost or date
            binding.btnUpdate.setOnClickListener {
                val newCost = binding.etCost.text.toString().toDoubleOrNull() ?: 0.0
                val newDate = binding.etDate.text.toString()

                lifecycleScope.launch {
                    // Safety check: Ensure the updated cost doesn't push the budget over its limit
                    val currentTotalWithoutThis = totalSpent - expense.amount
                    if (currentTotalWithoutThis + newCost > budget.limitAmount) {
                        Toast.makeText(requireContext(), "Update exceeds budget limit!", Toast.LENGTH_LONG).show()
                    } else {
                        // Persist the updated expense record
                        dao.updateExpense(expense.copy(amount = newCost, date = newDate))
                        Toast.makeText(requireContext(), "Expense updated", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }

            // Delete button action: removes this specific expense from the database
            binding.btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    dao.deleteExpense(expense)
                    Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks
        _binding = null
    }
}