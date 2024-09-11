package br.edu.ifes.firebaseapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifes.firebaseapp.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura RecyclerView
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        messagesAdapter = MessagesAdapter(mutableListOf()) // Inicializa o adaptador
        binding.recyclerViewMessages.adapter = messagesAdapter

        // Observa as mudanças no ViewModel
        observeViewModel()

        // Configura listener de rolagem para paginação
        binding.recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                // Verifica se chegou ao fim da lista e carrega mais mensagems
                if (lastVisibleItemPosition + 1 >= totalItemCount) {
                    homeViewModel.loadMoreMessages()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        homeViewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            if (messages != null && messages.isNotEmpty()) {
                messagesAdapter.updateMessages(messages)
                binding.recyclerViewMessages.visibility = View.VISIBLE
                binding.textHome.visibility = View.GONE
            }
        })

        homeViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        homeViewModel.isEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
            binding.textHome.visibility = if (isEmpty) View.VISIBLE else View.GONE
        })
    }
}