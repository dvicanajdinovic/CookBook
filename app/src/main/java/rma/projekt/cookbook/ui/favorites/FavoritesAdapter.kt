package rma.projekt.cookbook.ui.favorites

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import rma.projekt.cookbook.R
import rma.projekt.cookbook.ui.gallery.Recipe

class FavoritesAdapter(
    private var originalRecipeList: List<Recipe>,
    private val onRatingChanged: (recipe: Recipe, rating: Float) -> Unit,
    private val onFavoriteChanged: (recipe: Recipe) -> Unit // Callback for favorite changes
) : RecyclerView.Adapter<FavoritesAdapter.RecipeViewHolder>() {

    // Function to update the list of recipes in the adapter


    private var filteredRecipeList: List<Recipe> = originalRecipeList.filter { it.favorite }
    // Function to update the list of recipes in the adapter
    fun updateList(newList: List<Recipe>) {
        originalRecipeList = newList // Update the original list
        filterItems()
    }
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.itemTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.itemDescription)
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val favoriteIcon: ToggleButton = itemView.findViewById(R.id.toggleButton) // Favorite icon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = filteredRecipeList[position]

        holder.titleTextView.text = recipe.title
        holder.descriptionTextView.text = recipe.description

        // Load image using Glide
        Glide.with(holder.itemView)
            .load(recipe.imageUrl)
            .into(holder.imageView)

        // Set rating
        holder.ratingBar.rating = getAverageRating(recipe)

        // Handle rating change
        holder.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            onRatingChanged(recipe, rating)
        }

        // Set favorite icon color
        val favoriteColor = ContextCompat.getColor(holder.itemView.context, R.color.green)
        holder.favoriteIcon.backgroundTintList = ColorStateList.valueOf(favoriteColor)

        // Set favorite icon state
        holder.favoriteIcon.isChecked = recipe.favorite

        // Handle favorite icon click
        holder.favoriteIcon.setOnCheckedChangeListener { _, isChecked ->
            recipe.favorite = isChecked // Update recipe favorite status
            onFavoriteChanged(recipe) // Notify the callback
            filterItems() // Update filtered list
        }
    }

    override fun getItemCount(): Int {
        return filteredRecipeList.size
    }

    // Function to calculate average rating
    private fun getAverageRating(recipe: Recipe): Float {
        val ratings = recipe.ratings.values // Get all the ratings
        return if (ratings.isNotEmpty()) {
            val totalRating = ratings.sum() // Sum of all ratings
            totalRating.toFloat() / ratings.size // Calculate average
        } else {
            0f // No ratings yet
        }
    }

    // Function to filter the list based on favorite status
    private fun filterItems() {
        filteredRecipeList = originalRecipeList.filter { it.favorite }
        notifyDataSetChanged()
    }
}