package rma.projekt.cookbook.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import rma.projekt.cookbook.R
import rma.projekt.cookbook.databinding.FragmentRecoverAccountBinding

class RecoverAccountFragment : Fragment() {

    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecoverAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        setListener()
    }

    private fun setListener() {
        binding.buttonRecover.setOnClickListener { validateUser() }
    }

    private fun validateUser() {
        val email = binding.editEmailRecover.text.toString().trim()

        if (email.isNotEmpty()) {
            binding.progressBar.isVisible = true
            recoverAccount(email)
        } else {
            Toast.makeText(requireContext(), "Unesi e-mail adresu.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recoverAccount(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Slijedite poveznicu.", Toast.LENGTH_SHORT)
                }
                binding.progressBar.isVisible = false
            }
    }
}