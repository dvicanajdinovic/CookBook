package rma.projekt.cookbook.ui.gallery

data class Recipe(
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ratings: HashMap<String, Float> = hashMapOf(), // Changed to ratings
    val userId: String = "",
    var documentId: String = "",
    var favorite: Boolean = false // Added favorite
) {
    constructor() : this("", "", "", hashMapOf(), "", "", false)
}
