package rma.projekt.cookbook.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import rma.projekt.cookbook.databinding.FragmentFavoritesBinding
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import rma.projekt.cookbook.R
import rma.projekt.cookbook.ui.gallery.Recipe

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root = binding.root

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        favoritesAdapter = FavoritesAdapter(
            mutableListOf(), // Initially, show an empty list
            { recipe, rating ->
                updateRatingInDatabase(recipe, rating)
            },
            { recipe ->
                updateFavoriteInDatabase(recipe)
            }
        )

        recyclerView.adapter = favoritesAdapter

        eventChangeListener()
        return root
    }

    private fun eventChangeListener() {
        val currentUserId = currentUser?.uid ?: ""

        // Listen for changes in recipes collection where favorite is true
        db.collection("recipes")
            .whereNotEqualTo("userId", currentUserId)
            .whereEqualTo("favorite", true)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val favoriteRecipes = mutableListOf<Recipe>()
                for (dc in value!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val recipe = dc.document.toObject(Recipe::class.java)
                        recipe.documentId = dc.document.id // Set the document ID
                        favoriteRecipes.add(recipe)
                    }
                }

                favoritesAdapter.updateList(favoriteRecipes) // Update adapter list
            }
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



            }.addOnFailureListener {
                // Failed to update rating
                Toast.makeText(requireContext(), "Failed to update rating.", Toast.LENGTH_SHORT).show()

            }

    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    private fun updateFavoriteInDatabase(recipe: Recipe) {
        db.collection("recipes").document(recipe.documentId)
            .update("favorite", recipe.favorite)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Favorite status updated.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update favorite status.", Toast.LENGTH_SHORT).show()
            }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
