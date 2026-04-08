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

/**
 * Fragment that displays a list of expenses for a specific user.
 * It shows the user's name, their total budget limit, and all recorded transactions.
 */
class ExpenseListFragment : Fragment() {

    // ViewBinding for layout access
    private var _binding: ActivityExpenseListBinding? = null
    private val binding get() = _binding!!
    
    // Arguments passed via Navigation Safe Args (userId)
    private val args: ExpenseListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = ActivityExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database access
        val dao = AppDatabase.getDatabase(requireContext()).appDao()
        
        // Setup adapter with click listener to edit/delete expenses
        val adapter = ExpenseAdapter { expense ->
            if (findNavController().currentDestination?.id == R.id.expenseListFragment) {
                val action = ExpenseListFragmentDirections.actionExpenseListToUpdateDeleteExpense(expense.id)
                findNavController().navigate(action)
            }
        }

        // Configure the RecyclerView
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        // Back button to return to the previous screen (MainFragment)
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Navigate to the screen to update or delete the current user profile
        binding.btnUpdateDeleteUser.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.expenseListFragment) {
                val action = ExpenseListFragmentDirections.actionExpenseListToUpdateDeleteUser(args.userId)
                findNavController().navigate(action)
            }
        }

        // Load user info and budget details
        lifecycleScope.launch {
            val user = dao.getUserById(args.userId)
            binding.tvUserName.text = user?.username ?: "User"
            
            // Collect budget info to display limit and pass to AddExpense screen
            dao.getBudgetForUser(args.userId).collectLatest { budget ->
                binding.tvBudgetLimit.text = "$${budget?.limitAmount ?: 0.0}"
                
                binding.btnNewExpense.setOnClickListener {
                    if (budget != null && findNavController().currentDestination?.id == R.id.expenseListFragment) {
                        // Navigate to Add Expense screen with user and budget IDs
                        val action = ExpenseListFragmentDirections.actionExpenseListToAddExpense(args.userId, budget.id)
                        findNavController().navigate(action)
                    }
                }
            }
        }

        // Observe the list of expenses for this specific user
        var currentList = listOf<Expense>()

        fun updateList(list: List<Expense>) {
            currentList = list
            adapter.submitList(list)
        }

        binding.sortExpense.setOnClickListener {
            updateList(currentList.sortedBy { it.name.lowercase() })
        }

        binding.sortDate.setOnClickListener {
            updateList(currentList.sortedByDescending { it.date }) // Assuming YYYY-MM-DD or similar for string sort
        }

        binding.sortAmount.setOnClickListener {
            updateList(currentList.sortedByDescending { it.amount })
        }

        lifecycleScope.launch {
            dao.getExpensesForUser(args.userId).collectLatest {
                updateList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks
        _binding = null
    }
}

/**
 * Adapter for the RecyclerView displaying expenses.
 * @param onClick Callback triggered when an expense item is clicked.
 */
class ExpenseAdapter(private val onClick: (Expense) -> Unit) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    private var expenses: List<Expense> = emptyList()

    /**
     * Updates the data set and notifies the adapter.
     */
    fun submitList(list: List<Expense>) {
        expenses = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        // Inflate item layout
        val binding = ItemExpenseRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenses[position]
        // Map expense data to UI
        holder.binding.tvExpenseName.text = item.name
        holder.binding.tvDate.text = item.date
        holder.binding.tvAmount.text = "$${item.amount}"
        // Set click listener on the row
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = expenses.size

    /**
     * ViewHolder for Expense items.
     */
    class ExpenseViewHolder(val binding: ItemExpenseRowBinding) : RecyclerView.ViewHolder(binding.root)
}