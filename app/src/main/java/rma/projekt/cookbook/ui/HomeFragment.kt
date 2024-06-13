package rma.projekt.cookbook.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import rma.projekt.cookbook.R
import rma.projekt.cookbook.databinding.FragmentHomeBinding
import rma.projekt.cookbook.ui.board.TableView

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        setTable()
        chooseContent()
    }

    private fun setTable() {
        val adapter = TableView(requireActivity())
        binding.pager.adapter = adapter

        adapter.addFragment(NewsFragment(), R.string.news)
        adapter.addFragment(FriendsFragment(), R.string.friends)
        //adapter.addFragment(MyRecipesFragment(), R.string.recipes)

        binding.pager.offscreenPageLimit = adapter.itemCount

        TabLayoutMediator(binding.tabs, binding.pager) {
            tab, position -> tab.text = getString(adapter.getTitle(position))
        }.attach()
    }

    private fun chooseContent() {
        binding.ibLogout.setOnClickListener { logout() }
    }

    private fun logout() {
        auth.signOut()
        findNavController().navigate(R.id.action_introFragment_to_authentication)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}