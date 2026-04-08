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

class UpdateDeleteExpenseFragment : Fragment() {

    private var _binding: ItemUpdateDeleteExpenseBinding? = null
    private val binding get() = _binding!!
    private val args: UpdateDeleteExpenseFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemUpdateDeleteExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).appDao()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        lifecycleScope.launch {
            val expense = dao.getExpenseById(args.expenseId) ?: return@launch
            val user = dao.getUserById(expense.userId)
            val budget = dao.getBudgetById(expense.budgetId) ?: return@launch
            val totalSpent = dao.getTotalSpentForBudget(budget.id).first() ?: 0.0

            binding.etExpenseName.setText(expense.name)
            binding.etUserName.setText(user?.username ?: "N/A")
            binding.etCategory.setText(budget.category)
            binding.etSumExpenses.setText(totalSpent.toString())
            binding.etBalance.setText((budget.limitAmount - totalSpent).toString())
            binding.etCost.setText(expense.amount.toString())
            binding.etDate.setText(expense.date)

            binding.btnUpdate.setOnClickListener {
                val newCost = binding.etCost.text.toString().toDoubleOrNull() ?: 0.0
                val newDate = binding.etDate.text.toString()

                lifecycleScope.launch {
                    val currentTotalWithoutThis = totalSpent - expense.amount
                    if (currentTotalWithoutThis + newCost > budget.limitAmount) {
                        Toast.makeText(requireContext(), "Update exceeds budget limit!", Toast.LENGTH_LONG).show()
                    } else {
                        dao.updateExpense(expense.copy(amount = newCost, date = newDate))
                        Toast.makeText(requireContext(), "Expense updated", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }

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
        _binding = null
    }
}