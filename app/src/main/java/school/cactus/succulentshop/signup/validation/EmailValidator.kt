package school.cactus.succulentshop.signup.validation

import school.cactus.succulentshop.R
import school.cactus.succulentshop.validation.Validator


class EmailValidator : Validator {
    private val USERNAME_REGEX by lazy { "[a-z0-9_]{3,19}" }

    override fun validate(field: String): Int? {
        val checkEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(field.trim()).matches()

        return when {
            field.isEmpty() -> R.string.this_field_is_required
            !(checkEmail) -> R.string.email_invanlid
            else -> null
        }
    }
}
