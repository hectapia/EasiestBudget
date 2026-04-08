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
import com.easybudget.easiestbudget.databinding.ItemUpdateDeleteUserBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Fragment for updating or deleting an existing User and their Budget.
 */
class UpdateDeleteUserFragment : Fragment() {

    // ViewBinding for layout access
    private var _binding: ItemUpdateDeleteUserBinding? = null
    private val binding get() = _binding!!
    
    // Arguments passed via Navigation Safe Args (userId)
    private val args: UpdateDeleteUserFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = ItemUpdateDeleteUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database access
        val dao = AppDatabase.getDatabase(requireContext()).appDao()

        // Back button to return to the previous screen
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Load current user and budget details to populate the form
        lifecycleScope.launch {
            val user = dao.getUserById(args.userId) ?: return@launch
            val budget = dao.getBudgetForUser(args.userId).first() ?: return@launch
            // Calculate total spent to show balance and summary
            val totalSpent = dao.getTotalSpentForBudget(budget.id).first() ?: 0.0

            // Set current values in the UI
            binding.etUserName.setText(user.username)
            binding.etCategory.setText(budget.category)
            binding.etSumExpenses.setText(totalSpent.toString())
            binding.etBalance.setText((budget.limitAmount - totalSpent).toString())
            binding.etBudgetLimit.setText(budget.limitAmount.toString())

            // Update button action: saves the new budget limit
            binding.btnUpdate.setOnClickListener {
                val newLimit = binding.etBudgetLimit.text.toString().toDoubleOrNull() ?: 0.0
                lifecycleScope.launch {
                    // Update the budget record with the new limit
                    dao.updateBudget(budget.copy(limitAmount = newLimit))
                    Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }

            // Delete button action: removes the user and all their associated data (budgets, expenses)
            binding.btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    // This triggers a cascade delete if configured in the entities
                    dao.deleteUser(user)
                    Toast.makeText(requireContext(), "User and history deleted", Toast.LENGTH_SHORT).show()
                    // Return to the main screen as the current user no longer exists
                    findNavController().navigate(R.id.action_mainFragment) 
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