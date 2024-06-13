package rma.projekt.cookbook.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rma.projekt.cookbook.R
import rma.projekt.cookbook.databinding.FragmentFriendsBinding
import rma.projekt.cookbook.databinding.FragmentHomeBinding
import rma.projekt.cookbook.databinding.FragmentNewsBinding

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}