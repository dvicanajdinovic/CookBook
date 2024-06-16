package rma.projekt.cookbook.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import rma.projekt.cookbook.ui.gallery.Ingredient
import java.util.*

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private var selectedImageUri: Uri? = null

    private lateinit var btnExpand: Button
    private lateinit var btnDelete: Button

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

        // Initialize ingredient list and buttons
        expandIngredientsList(binding.root)

        btnExpand = binding.root.findViewById(R.id.btnExpandList)
        btnDelete = binding.root.findViewById(R.id.btnDeleteLast)

        btnExpand.setOnClickListener {
            expandIngredientsList(binding.root)
        }

        btnDelete.setOnClickListener {
            deleteLastIngredient(binding.root)
        }

        // Setup Spinner
        val unitSpinner: Spinner = binding.root.findViewById(R.id.unitPicker)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.unit_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            unitSpinner.adapter = adapter
        }

        // Handle Spinner selection
        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedUnit = parent.getItemAtPosition(position).toString()
                Toast.makeText(requireContext(), "Selected: $selectedUnit", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
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
                // Get ingredients list
                val ingredientsList = getIngredientsList()
                // Save recipe to Firestore with ingredients
                saveRecipeToFirestore(title, description, downloadUri.toString(), ingredientsList)
            } else {
                Toast.makeText(requireContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getIngredientsList(): List<Ingredient> {
        val parentLayout = binding.root.findViewById<LinearLayout>(R.id.ingredientsList)
        val ingredientsList = mutableListOf<Ingredient>()

        for (i in 0 until parentLayout.childCount - 1) { // Exclude the last child button
            val child = parentLayout.getChildAt(i)
            val editIngredientName = child.findViewById<EditText>(R.id.ingredientName)
            val editIngredientQuantity = child.findViewById<EditText>(R.id.ingredientQuantity)
            val unitSpinner = child.findViewById<Spinner>(R.id.unitPicker)

            val name = editIngredientName.text.toString().trim()
            val quantity = editIngredientQuantity.text.toString().trim().toDoubleOrNull() ?: 0.0
            val unit = unitSpinner.selectedItem.toString()

            val ingredient = Ingredient(name, quantity, unit)
            ingredientsList.add(ingredient)
        }

        return ingredientsList
    }


    private fun saveRecipeToFirestore(title: String, description: String, imageUrl: String, ingredients: List<Ingredient>) {
        val recipe = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "userId" to auth.currentUser?.uid,
            "score" to listOf<Int>(), // Initializing an empty list of integers
            "ingredients" to ingredients.map { ingredient ->
                hashMapOf(
                    "name" to ingredient.name,
                    "quantity" to ingredient.quantity,
                    "unit" to ingredient.unit
                )
            }
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
        val child = LayoutInflater.from(requireContext()).inflate(R.layout.add_ingredient, parentLayout, false)

        val btnIncrease = child.findViewById<Button>(R.id.increase)
        val btnDecrease = child.findViewById<Button>(R.id.decrease)
        val editIngredientQuantity = child.findViewById<EditText>(R.id.ingredientQuantity)
        val unitSpinner = child.findViewById<Spinner>(R.id.unitPicker)

        btnIncrease.setOnClickListener {
            val count = editIngredientQuantity.text.toString().toInt()
            editIngredientQuantity.setText((count + 1).toString())
        }

        btnDecrease.setOnClickListener {
            val count = editIngredientQuantity.text.toString().toInt()
            if (count > 1) {
                editIngredientQuantity.setText((count - 1).toString())
            }
        }

        // Setup both quantity and unit spinner
        setupSpinner(unitSpinner)

        parentLayout.addView(child, parentLayout.childCount - 1) // Add new view above the last child
    }

    private fun setupSpinner(unitSpinner: Spinner) {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.unit_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            unitSpinner.adapter = adapter
        }

        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedUnit = parent.getItemAtPosition(position).toString()
                Toast.makeText(requireContext(), "Selected: $selectedUnit", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun deleteLastIngredient(rootView: View) {
        val parentLayout = rootView.findViewById<LinearLayout>(R.id.ingredientsList)
        val childCount = parentLayout.childCount

        // Ensure there is at least one ingredient to delete and the last child is not the "Dodajte još sastojaka" button
        if (childCount > 1) {
            parentLayout.removeViewAt(childCount - 2) // Remove the last ingredient view
        } else {
            Toast.makeText(requireContext(), "Nema više sastojaka za izbrisati.", Toast.LENGTH_SHORT).show()
        }
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
