package com.easybudget.easiestbudget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.easybudget.easiestbudget.database.AppDatabase
import com.easybudget.easiestbudget.databinding.ItemUserBudgetBinding
import com.easybudget.easiestbudget.models.Budget
import com.easybudget.easiestbudget.models.User
import kotlinx.coroutines.launch

/**
 * Fragment for creating a new User and their initial Budget category.
 */
class AddUserBudgetFragment : Fragment() {

    // ViewBinding for layout access
    private var _binding: ItemUserBudgetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = ItemUserBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cancel and return to the main dashboard
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Action to save the new user and budget
        binding.btnAddUser.setOnClickListener {
            val name = binding.etUserName.text.toString()
            val category = binding.etCategory.text.toString()
            val limit = binding.etBudgetLimit.text.toString().toDoubleOrNull() ?: 0.0

            // Validation: Username and Category cannot be empty
            if (name.isBlank() || category.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val dao = AppDatabase.getDatabase(requireContext()).appDao()
                
                // 1. Insert the new user and get their generated ID
                val userId = dao.insertUser(User(username = name))
                
                // 2. Create the associated budget record for that user
                dao.insertBudget(Budget(userId = userId.toInt(), category = category, limitAmount = limit))
                
                Toast.makeText(requireContext(), "User and Budget added", Toast.LENGTH_SHORT).show()
                
                // Return to the dashboard
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks by clearing binding
        _binding = null
    }
}