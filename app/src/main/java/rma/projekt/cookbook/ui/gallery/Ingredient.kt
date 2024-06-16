package rma.projekt.cookbook.ui.gallery

data class Ingredient(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
) {
    constructor() : this("", 0.0, "")
}