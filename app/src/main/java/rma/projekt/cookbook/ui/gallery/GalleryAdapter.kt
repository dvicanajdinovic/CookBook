import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import rma.projekt.cookbook.R
import rma.projekt.cookbook.ui.gallery.Recipe

class GalleryAdapter(
    private val recipeList: List<Recipe>,
    private val onRatingChanged: (recipe: Recipe, rating: Float) -> Unit,
    private val onFavoriteChanged: (recipe: Recipe) -> Unit // Callback for favorite changes
) : RecyclerView.Adapter<GalleryAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.itemTitle)
      //  val descriptionTextView: TextView = itemView.findViewById(R.id.itemDescription)
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val favoriteIcon: ToggleButton = itemView.findViewById(R.id.toggleButton) // Favorite icon
        val itemRating : TextView = itemView.findViewById(R.id.itemRating)
        val card : CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return RecipeViewHolder(view)

    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.titleTextView.text = recipe.title
      //  holder.descriptionTextView.text = recipe.description

        // Load image using Glide
        Glide.with(holder.itemView)
            .load(recipe.imageUrl)
            .into(holder.imageView)

        // Set rating
        holder.ratingBar.rating = recipe.ratings[FirebaseAuth.getInstance().currentUser?.uid] ?: 0f
        holder.itemRating.text = String.format("%.1f / 5", getAverageRating(recipe))
        // Handle rating change
        holder.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            onRatingChanged(recipe, rating)
            holder.itemRating.text = String.format("%.1f / 5", getAverageRating(recipe))
        }
        // Set favorite icon color
        val favoriteColor = if (recipe.favorite) {
            ContextCompat.getColor(holder.itemView.context, R.color.green)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.gray)
        }
        holder.favoriteIcon.backgroundTintList = ColorStateList.valueOf(favoriteColor)


        // Handle favorite icon click
        holder.favoriteIcon.setOnClickListener {
            recipe.favorite = !recipe.favorite // Toggle favorite status
            onFavoriteChanged(recipe) // Notify the callback
            notifyItemChanged(position) // Refresh the item to update the icon
        }

        holder.card.setOnClickListener{
            val bundle = Bundle().apply {
                putString("recipeId", recipe.documentId)
            }
            holder.card.findNavController().navigate(R.id.action_Home_to_recipeDetail, bundle)
        }

    }

    override fun getItemCount(): Int {
        return recipeList.size
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
}