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
import com.easybudget.easiestbudget.databinding.ItemExpenseBinding
import com.easybudget.easiestbudget.models.Expense
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddExpenseFragment : Fragment() {

    private var _binding: ItemExpenseBinding? = null
    private val binding get() = _binding!!
    private val args: AddExpenseFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).appDao()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        lifecycleScope.launch {
            val budget = dao.getBudgetById(args.budgetId)
            if (budget != null) {
                binding.etBudgetLimit.setText(budget.limitAmount.toString())
                binding.etCategory.setText(budget.category)
            }
        }

        binding.btnAddExpense.setOnClickListener {
            val name = binding.etExpenseName.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val date = binding.etDate.text.toString()

            if (name.isBlank() || amountStr.isBlank() || date.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val budget = dao.getBudgetById(args.budgetId) ?: return@launch
                val totalSpent = dao.getTotalSpentForBudget(args.budgetId).first() ?: 0.0
                
                if (totalSpent + amount > budget.limitAmount) {
                    Toast.makeText(requireContext(), "Expense exceeds budget limit!", Toast.LENGTH_LONG).show()
                } else {
                    dao.insertExpense(Expense(
                        userId = args.userId,
                        budgetId = args.budgetId,
                        name = name,
                        amount = amount,
                        date = date
                    ))
                    Toast.makeText(requireContext(), "Expense added", Toast.LENGTH_SHORT).show()
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