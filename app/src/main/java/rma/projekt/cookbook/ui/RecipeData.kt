package rma.projekt.cookbook.ui

data class RecipeData(
    val title: String? = null,
    val description: String? = null,
    val imageUrls: String? = null,
    val score: List<Int> = listOf(),// Property for image URLs,
)
