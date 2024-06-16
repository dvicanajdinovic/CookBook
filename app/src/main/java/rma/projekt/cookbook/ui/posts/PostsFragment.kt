package rma.projekt.cookbook.ui.posts

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
import rma.projekt.cookbook.databinding.FragmentGalleryBinding
import rma.projekt.cookbook.ui.gallery.Recipe

class PostsFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeArrayList: ArrayList<Recipe>
    private lateinit var postsAdapter: PostsAdapter
    private var db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root = binding.root

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        recipeArrayList = arrayListOf()
        postsAdapter = PostsAdapter(recipeArrayList) { recipe, rating ->
            updateRatingInDatabase(recipe, rating)
        }

        recyclerView.adapter = postsAdapter

        eventChangeListener()

        return root
    }

    private fun eventChangeListener() {
        val currentUserId = getCurrentUserId()

        db.collection("recipes")
            .whereEqualTo("userId", currentUserId)
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
                postsAdapter.notifyDataSetChanged()
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
            }.addOnFailureListener {
                // Failed to update rating
                Toast.makeText(requireContext(), "Failed to update rating.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
