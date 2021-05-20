package school.cactus.succulentshop.api.product

data class RelatedProducts(
    val id: Long,
    val publishedAt: String,
    val createdAt: String,
    val updatedAt: String,
    val products: List<Product>
)
