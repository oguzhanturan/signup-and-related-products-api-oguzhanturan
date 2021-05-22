package school.cactus.succulentshop.api.register

data class RegisterErrorResponse(
    val statusCode: Long,
    val error: String,
    val message: List<OuterMessage>,
)

data class OuterMessage(
    val messages: List<InnerMessage>
)

data class InnerMessage(
    val id: String,
    val message: String
)
