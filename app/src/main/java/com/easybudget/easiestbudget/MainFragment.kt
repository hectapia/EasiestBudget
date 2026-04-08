package com.easybudget.easiestbudget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easybudget.easiestbudget.database.AppDatabase
import com.easybudget.easiestbudget.database.UserWithBudget
import com.easybudget.easiestbudget.databinding.ActivityMainBinding
import com.easybudget.easiestbudget.databinding.ItemUserRowBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The main dashboard fragment of the application.
 * Displays a list of all users and their associated budgets.
 */
class MainFragment : Fragment() {

    // ViewBinding instance to access UI elements safely
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = ActivityMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the database DAO
        val dao = AppDatabase.getDatabase(requireContext()).appDao()
        
        // Initialize the RecyclerView adapter with a click listener
        val adapter = UserAdapter { userWithBudget ->
            // Navigate to the expense list for the selected user
            val action = MainFragmentDirections.actionMainToExpenseList(userWithBudget.user.id)
            findNavController().navigate(action)
        }

        // Set up the RecyclerView with a layout manager and the adapter
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        // Navigate to the screen for creating a new user and budget
        binding.btnNewUser.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_addUserBudget)
        }

        // Sorting logic
        var currentList = listOf<UserWithBudget>()
        
        fun updateList(list: List<UserWithBudget>) {
            currentList = list
            adapter.submitList(list)
        }

        binding.sortUser.setOnClickListener {
            updateList(currentList.sortedBy { it.user.username.lowercase() })
        }

        binding.sortCategory.setOnClickListener {
            updateList(currentList.sortedBy { it.budget?.category?.lowercase() ?: "" })
        }

        binding.sortAmount.setOnClickListener {
            updateList(currentList.sortedByDescending { it.budget?.limitAmount ?: 0.0 })
        }

        // Collect the list of users and budgets from the database reactively
        lifecycleScope.launch {
            dao.getUsersWithBudgets().collectLatest {
                updateList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear binding to avoid memory leaks
        _binding = null
    }
}

/**
 * Adapter for the RecyclerView displaying users.
 * @param onClick Callback function triggered when a user item is clicked.
 */
class UserAdapter(private val onClick: (UserWithBudget) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var users: List<UserWithBudget> = emptyList()

    /**
     * Updates the data set and refreshes the RecyclerView.
     */
    fun submitList(list: List<UserWithBudget>) {
        users = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Inflate the item layout using ViewBinding
        val binding = ItemUserRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = users[position]
        // Bind data to the UI components
        holder.binding.tvUserName.text = item.user.username
        holder.binding.tvCategory.text = item.budget?.category ?: "N/A"
        holder.binding.tvAmount.text = "$${item.budget?.limitAmount ?: 0.0}"
        // Set the click listener for the entire row
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = users.size

    /**
     * ViewHolder for the UserAdapter.
     */
    class UserViewHolder(val binding: ItemUserRowBinding) : RecyclerView.ViewHolder(binding.root)
}