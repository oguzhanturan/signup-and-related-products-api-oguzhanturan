package school.cactus.succulentshop.api.register

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)