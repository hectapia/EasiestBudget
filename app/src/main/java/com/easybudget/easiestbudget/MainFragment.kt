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

class MainFragment : Fragment() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).appDao()
        val adapter = UserAdapter { userWithBudget ->
            val action = MainFragmentDirections.actionMainToExpenseList(userWithBudget.user.id)
            findNavController().navigate(action)
        }

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        binding.btnNewUser.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_addUserBudget)
        }

        lifecycleScope.launch {
            dao.getUsersWithBudgets().collectLatest {
                adapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class UserAdapter(private val onClick: (UserWithBudget) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var users: List<UserWithBudget> = emptyList()

    fun submitList(list: List<UserWithBudget>) {
        users = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = users[position]
        holder.binding.tvUserName.text = item.user.username
        holder.binding.tvCategory.text = item.budget?.category ?: "N/A"
        holder.binding.tvAmount.text = "$${item.budget?.limitAmount ?: 0.0}"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = users.size

    class UserViewHolder(val binding: ItemUserRowBinding) : RecyclerView.ViewHolder(binding.root)
}