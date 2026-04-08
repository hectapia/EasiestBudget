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

class UpdateDeleteUserFragment : Fragment() {

    private var _binding: ItemUpdateDeleteUserBinding? = null
    private val binding get() = _binding!!
    private val args: UpdateDeleteUserFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemUpdateDeleteUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).appDao()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        lifecycleScope.launch {
            val user = dao.getUserById(args.userId) ?: return@launch
            val budget = dao.getBudgetForUser(args.userId).first() ?: return@launch
            val totalSpent = dao.getTotalSpentForBudget(budget.id).first() ?: 0.0

            binding.etUserName.setText(user.username)
            binding.etCategory.setText(budget.category)
            binding.etSumExpenses.setText(totalSpent.toString())
            binding.etBalance.setText((budget.limitAmount - totalSpent).toString())
            binding.etBudgetLimit.setText(budget.limitAmount.toString())

            binding.btnUpdate.setOnClickListener {
                val newLimit = binding.etBudgetLimit.text.toString().toDoubleOrNull() ?: 0.0
                lifecycleScope.launch {
                    dao.updateBudget(budget.copy(limitAmount = newLimit))
                    Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }

            binding.btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    dao.deleteUser(user)
                    Toast.makeText(requireContext(), "User and history deleted", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_mainFragment) // Go back to start
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}