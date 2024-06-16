package rma.projekt.cookbook.ui.recipedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import rma.projekt.cookbook.databinding.FragmentRecipeDetailBinding
import rma.projekt.cookbook.ui.gallery.Recipe

class RecipeDetailFragment : Fragment() {
    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recipeId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firestore = FirebaseFirestore.getInstance()

        // Get the recipe ID passed from the previous fragment
        recipeId = arguments?.getString("recipeId") ?: ""

        // Fetch recipe details from Firebase
        fetchRecipeDetails(recipeId)

        binding.buttonBack.setOnClickListener {
            // return to the previous fragment
            parentFragmentManager.popBackStack()
        }

        return root
    }

    private fun fetchRecipeDetails(recipeId: String) {
        // Assuming 'recipes' is your collection name
        val recipeRef = firestore.collection("recipes").document(recipeId)
        recipeRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val recipe = document.toObject(Recipe::class.java)
                    recipe?.let { displayRecipeDetails(it) }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        // Bind data to views
        binding.textViewRecipeTitle.text = recipe.title
        binding.textViewRecipeDescription.text = recipe.description
        // Load image using Glide
        Glide.with(requireContext())
            .load(recipe.imageUrl)
            .into(binding.imageViewRecipe)

        // Display rating
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserRating = recipe.ratings[currentUser?.uid] ?: 0f
        binding.ratingBarRecipe.rating = currentUserRating
        binding.ratingBarRecipe.isEnabled = true // Enable rating bar

        // Set up rating bar listener
        binding.ratingBarRecipe.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                // Update rating in Firestore
                updateRatingInFirestore(recipeId, rating)
            }
        }
    }

    // Function to update the rating in Firestore
    private fun updateRatingInFirestore(recipeId: String, rating: Float) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Assuming 'recipes' is your collection name
            val recipeRef = firestore.collection("recipes").document(recipeId)
            val ratingsMap = hashMapOf(currentUser.uid to rating)
            recipeRef.update("ratings", ratingsMap)
                .addOnSuccessListener {
                    // Rating updated successfully
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
