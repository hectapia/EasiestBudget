package com.easybudget.easiestbudget

import android.app.DatePickerDialog
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
import com.easybudget.easiestbudget.databinding.ItemExpenseBinding
import com.easybudget.easiestbudget.models.Expense
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * Fragment for adding a new expense record.
 * Handles user input, validation, and budget limit checks.
 */
class AddExpenseFragment : Fragment() {

    // ViewBinding instance to access layout views
    private var _binding: ItemExpenseBinding? = null
    private val binding get() = _binding!!
    
    // Arguments passed via Navigation Safe Args (userId and budgetId)
    private val args: AddExpenseFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = ItemExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database access
        val dao = AppDatabase.getDatabase(requireContext()).appDao()

        // Back button returns to the previous screen without saving
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Fetch and display current budget details (limit and category) as read-only info
        lifecycleScope.launch {
            val budget = dao.getBudgetById(args.budgetId)
            if (budget != null) {
                binding.etBudgetLimit.setText(budget.limitAmount.toString())
                binding.etCategory.setText(budget.category)
            }
        }

        // Calendar Picker for the date field
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    binding.etDate.setText(formattedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Action when the "Add Expense" button is clicked
        binding.btnAddExpense.setOnClickListener {
            val name = binding.etExpenseName.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val date = binding.etDate.text.toString()

            // Basic validation: ensure all fields are filled
            if (name.isBlank() || amountStr.isBlank() || date.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure the amount is a valid positive number
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Fetch current budget and total spent to check if this new expense is allowed
                val budget = dao.getBudgetById(args.budgetId) ?: return@launch
                val totalSpent = dao.getTotalSpentForBudget(args.budgetId).first() ?: 0.0
                
                // Advanced requirement: Check for "Over-Budget" condition
                if (totalSpent + amount > budget.limitAmount) {
                    Toast.makeText(requireContext(), "Expense exceeds budget limit!", Toast.LENGTH_LONG).show()
                } else {
                    // Insert the new expense into the database
                    dao.insertExpense(Expense(
                        userId = args.userId,
                        budgetId = args.budgetId,
                        name = name,
                        amount = amount,
                        date = date
                    ))
                    Toast.makeText(requireContext(), "Expense added", Toast.LENGTH_SHORT).show()
                    // Return to the expense list
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding to prevent memory leaks
        _binding = null
    }
}