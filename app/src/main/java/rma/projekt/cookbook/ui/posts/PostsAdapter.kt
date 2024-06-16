package rma.projekt.cookbook.ui.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import rma.projekt.cookbook.R
import rma.projekt.cookbook.ui.gallery.Recipe

class PostsAdapter(private val recipeList: List<Recipe>, private val onRatingChanged: (recipe: Recipe, rating: Float) -> Unit) :
    RecyclerView.Adapter<PostsAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.itemTitle)
       // val descriptionTextView: TextView = itemView.findViewById(R.id.itemDescription)
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        val itemRating: TextView = itemView.findViewById(R.id.itemRating)
        val postDelete: FloatingActionButton = itemView.findViewById(R.id.floatingActionButton2)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_posts, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.titleTextView.text = recipe.title
       // holder.descriptionTextView.text = recipe.description
        holder.itemRating.text = String.format("%.1f / 5", getAverageRating(recipe))

        // Load image using Glide
        Glide.with(holder.itemView)
            .load(recipe.imageUrl)
            .into(holder.imageView)

        holder.postDelete.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, recipe)
        }
    }

    private fun showDeleteConfirmationDialog(context: android.content.Context, recipe: Recipe) {
        AlertDialog.Builder(context)
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                deleteRecipe(recipe)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteRecipe(recipe: Recipe) {
        // Implement the deletion logic here
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val recipeRef = db.collection("recipes").document(recipe.documentId)
        recipeRef.delete()
    }

    private fun getAverageRating(recipe: Recipe): Float {
        val ratings = recipe.ratings.values // Get all the ratings
        return if (ratings.isNotEmpty()) {
            val totalRating = ratings.sum() // Sum of all ratings
            totalRating.toFloat() / ratings.size // Calculate average
        } else {
            0f // No ratings yet
        }
    }


    override fun getItemCount(): Int {
        return recipeList.size
    }

}
