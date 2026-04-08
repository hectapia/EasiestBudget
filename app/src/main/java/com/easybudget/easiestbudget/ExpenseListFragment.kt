package com.easybudget.easiestbudget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easybudget.easiestbudget.database.AppDatabase
import com.easybudget.easiestbudget.databinding.ActivityExpenseListBinding
import com.easybudget.easiestbudget.databinding.ItemExpenseRowBinding
import com.easybudget.easiestbudget.models.Expense
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExpenseListFragment : Fragment() {

    private var _binding: ActivityExpenseListBinding? = null
    private val binding get() = _binding!!
    private val args: ExpenseListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).appDao()
        val adapter = ExpenseAdapter { expense ->
            val action = ExpenseListFragmentDirections.actionExpenseListToUpdateDeleteExpense(expense.id)
            findNavController().navigate(action)
        }

        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnUpdateDeleteUser.setOnClickListener {
            val action = ExpenseListFragmentDirections.actionExpenseListToUpdateDeleteUser(args.userId)
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            val user = dao.getUserById(args.userId)
            binding.tvUserName.text = user?.username ?: "User"
            
            dao.getBudgetForUser(args.userId).collectLatest { budget ->
                binding.tvBudgetLimit.text = "$${budget?.limitAmount ?: 0.0}"
                
                binding.btnNewExpense.setOnClickListener {
                    if (budget != null) {
                        val action = ExpenseListFragmentDirections.actionExpenseListToAddExpense(args.userId, budget.id)
                        findNavController().navigate(action)
                    }
                }
            }
        }

        lifecycleScope.launch {
            dao.getExpensesForUser(args.userId).collectLatest {
                adapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ExpenseAdapter(private val onClick: (Expense) -> Unit) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    private var expenses: List<Expense> = emptyList()

    fun submitList(list: List<Expense>) {
        expenses = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenses[position]
        holder.binding.tvExpenseName.text = item.name
        holder.binding.tvDate.text = item.date
        holder.binding.tvAmount.text = "$${item.amount}"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = expenses.size

    class ExpenseViewHolder(val binding: ItemExpenseRowBinding) : RecyclerView.ViewHolder(binding.root)
}