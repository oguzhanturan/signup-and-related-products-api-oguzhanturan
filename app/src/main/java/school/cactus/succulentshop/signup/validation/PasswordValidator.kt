package school.cactus.succulentshop.signup.validation

import school.cactus.succulentshop.R
import school.cactus.succulentshop.validation.Validator
import java.util.regex.Pattern

class PasswordValidator : Validator {
    override fun validate(field: String) = when {
        field.isEmpty() -> R.string.this_field_is_required
        field.trim().length < 7 -> R.string.this_field_is_required //2 den küçük olamaz
        field.trim().length > 40 -> R.string.this_field_is_required //20 den büyük olamaz
        !isValidPasswordFormat(field.trim()) -> R.string.password_must_contains //password validator
        else -> null
    }

    private fun isValidPasswordFormat(password: String): Boolean {
        val passwordREGEX = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[!@#$%^&+=])" +   //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{7,}" +               //at least 8 characters
                    "$"
        )
        return passwordREGEX.matcher(password).matches()
    }
}