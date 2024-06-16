package rma.projekt.cookbook.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import rma.projekt.cookbook.R
import rma.projekt.cookbook.databinding.FragmentAddRecipeBinding
import java.util.UUID


class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private var selectedImageUri: Uri? = null

    private lateinit var btn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage
        firestore = FirebaseFirestore.getInstance()

        binding.btnOdustani.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.btnImportImage.setOnClickListener {
            openGallery()
        }

        binding.btnObjavi.setOnClickListener {
            uploadRecipe()
        }

        btn = binding.root.findViewById(R.id.btnExpandList)
        binding.btnExpandList.setOnClickListener {
            expandIngredientsList(binding.root)
        }
    }

    private fun uploadRecipe() {
        val title = binding.editNaslov.text.toString().trim()
        val description = binding.editOpis.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload image to Firebase Storage
        val imageRef = storage.reference.child("recipe_images/${UUID.randomUUID()}.jpg")
        val uploadTask = imageRef.putFile(selectedImageUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                saveRecipeToFirestore(title, description, downloadUri.toString())
            } else {
                Toast.makeText(requireContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRecipeToFirestore(title: String, description: String, imageUrl: String) {
        val recipe = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "userId" to auth.currentUser?.uid,
            "score" to listOf<Int>() // Initializing an empty list of integers
        )

        firestore.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {
                // Recipe successfully added
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save recipe.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Jeste li sigurni da se želite vratiti?")
            .setPositiveButton("Da") { _, _ ->
                goBack()
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun expandIngredientsList(rootView: View) {
        val parentLayout = rootView.findViewById<LinearLayout>(R.id.ingredientsList)
        val child = LinearLayout(context)
        parentLayout.addView(child)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                // Load the image using Glide
                Glide.with(this)
                    .load(it)
                    .into(binding.imageView) // Assuming you have an ImageView with this id in your layout
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun goBack() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
