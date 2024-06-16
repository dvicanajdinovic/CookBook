package rma.projekt.cookbook.ui.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import rma.projekt.cookbook.R
import rma.projekt.cookbook.ui.gallery.Recipe

class PostsAdapter(private val recipeList: List<Recipe>, private val onRatingChanged: (recipe: Recipe, rating: Float) -> Unit) :
    RecyclerView.Adapter<PostsAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.itemTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.itemDescription)
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_posts, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.titleTextView.text = recipe.title
        holder.descriptionTextView.text = recipe.description

        // Load image using Glide
        Glide.with(holder.itemView)
            .load(recipe.imageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

}
