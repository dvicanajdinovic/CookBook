package rma.projekt.cookbook.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import rma.projekt.cookbook.databinding.FragmentGalleryBinding
import GalleryAdapter
import androidx.navigation.fragment.findNavController
import rma.projekt.cookbook.R

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeArrayList: ArrayList<Recipe>
    private lateinit var filteredRecipeArrayList: ArrayList<Recipe>
    private lateinit var galleryAdapter: GalleryAdapter
    private var db = FirebaseFirestore.getInstance()
    private lateinit var categorySpinner: Spinner
    private lateinit var subcategorySpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root = binding.root

        categorySpinner = binding.categorySpinner
        subcategorySpinner = binding.subcategorySpinner
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        recipeArrayList = arrayListOf()
        filteredRecipeArrayList = arrayListOf()
        galleryAdapter = GalleryAdapter(filteredRecipeArrayList, { recipe, rating ->
            updateRatingInDatabase(recipe, rating)
        }, { recipe ->
            updateFavoriteInDatabase(recipe)
        })

        recyclerView.adapter = galleryAdapter

        setupCategorySpinner()
        eventChangeListener()

        return root
    }

    private fun setupCategorySpinner() {
        val categories = listOf("All", "Yoyo", "Category2", "Category3") // Replace with actual categories
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                setupSubcategorySpinner(categories[position])
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun setupSubcategorySpinner(category: String) {
        val subcategories = when (category) {
            "Yoyo" -> listOf("All", "Yohaha", "Subcategory1.2")
            "Category2" -> listOf("All", "Subcategory2.1", "Subcategory2.2")
            "Category3" -> listOf("All", "Subcategory3.1", "Subcategory3.2")
            else -> listOf("All")
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subcategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subcategorySpinner.adapter = adapter

        subcategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun filterRecipes() {
        val selectedCategory = categorySpinner.selectedItem.toString()
        val selectedSubcategory = subcategorySpinner.selectedItem.toString()

        filteredRecipeArrayList.clear()
        filteredRecipeArrayList.addAll(recipeArrayList.filter { recipe ->
            (selectedCategory == "All" || recipe.title == selectedCategory) &&
                    (selectedSubcategory == "All" || recipe.description == selectedSubcategory)
        })
        galleryAdapter.notifyDataSetChanged()
    }

    private fun eventChangeListener() {
        val currentUserId = getCurrentUserId()

        db.collection("recipes")
            .whereNotEqualTo("userId", currentUserId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                recipeArrayList.clear()
                for (dc in value!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val recipe = dc.document.toObject(Recipe::class.java)
                        recipe.documentId = dc.document.id // Set the document ID
                        recipeArrayList.add(recipe)
                    }
                }
                filterRecipes() // Filter the recipes after fetching
            }
    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    private fun updateRatingInDatabase(recipe: Recipe, rating: Float) {
        val userId = getCurrentUserId() // Get the current user ID

        if (recipe.ratings.containsKey(userId)) {
            // If the user has already rated, update their rating
            recipe.ratings[userId] = rating
        } else {
            // If the user hasn't rated, add their rating
            recipe.ratings[userId] = rating
        }

        // Calculate the average rating
        val averageRating = recipe.ratings.values.average()

        // Update the recipe document in Firestore with the new ratings
        db.collection("recipes").document(recipe.documentId)
            .update(
                mapOf(
                    "ratings" to recipe.ratings, // Update ratings HashMap
                    "averageRating" to averageRating
                )
            ).addOnSuccessListener {
                // Rating successfully updated
                Toast.makeText(requireContext(), "Rating updated successfully.", Toast.LENGTH_SHORT).show()
                // Refresh the fragment
                findNavController().navigate(R.id.action_Home_self)

            }.addOnFailureListener {
                // Failed to update rating
                Toast.makeText(requireContext(), "Failed to update rating.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_Home_self)
            }
    }

    private fun updateFavoriteInDatabase(recipe: Recipe) {
        db.collection("recipes").document(recipe.documentId)
            .update("favorite", recipe.favorite)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Favorite status updated.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_Home_self)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update favorite status.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_Home_self)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
