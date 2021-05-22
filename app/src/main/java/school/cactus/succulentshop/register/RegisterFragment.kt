package school.cactus.succulentshop.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import school.cactus.succulentshop.R
import school.cactus.succulentshop.api.api
import school.cactus.succulentshop.api.register.RegisterErrorResponse
import school.cactus.succulentshop.api.register.RegisterRequest
import school.cactus.succulentshop.api.register.RegisterResponse
import school.cactus.succulentshop.auth.JwtStore
import school.cactus.succulentshop.databinding.FragmentRegisterBinding
import school.cactus.succulentshop.register.validation.EmailValidator
import school.cactus.succulentshop.register.validation.PasswordValidator
import school.cactus.succulentshop.register.validation.UsernameValidator


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    private val usernameValidator = UsernameValidator()

    private val emailValidator = EmailValidator()

    private val passwordValidator = PasswordValidator()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            signUpButton.setOnClickListener {
                if (passwordInputLayout.isValid() and usernameInputLayout.isValid() and
                    emailInputLayout.isValid()
                ) {
                    sendSignupRequest()
                }
            }

            alreadyHaveAccountButton.setOnClickListener {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        requireActivity().title = getString(R.string.sign_up)
    }

    private fun sendSignupRequest() {
        val email = binding.emailInputLayout.editText!!.text.toString()
        val username = binding.usernameInputLayout.editText!!.text.toString()
        val password = binding.passwordInputLayout.editText!!.text.toString()

        val request = RegisterRequest(email, password, username)

        api.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                when (response.code()) {
                    in 200..299 -> onSignupSuccess(response.body()!!)
                    in 400..499 -> onClientError(response.errorBody()!!)
                    else -> onUnexpectedError()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Snackbar.make(
                    binding.root, R.string.check_your_connection,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.retry) {
                        sendSignupRequest()
                    }
                    .show()
            }
        })
    }

    private fun onSignupSuccess(response: RegisterResponse) {
        JwtStore(requireContext()).saveJwt(response.jwt)
        if (findNavController().currentDestination?.id == R.id.registerFragment) {
            findNavController().navigate(R.id.registerSuccessful)
        }
    }

    private fun onUnexpectedError() {
        Snackbar.make(
            binding.root, R.string.unexpected_error_occurred,
            LENGTH_LONG
        ).show()
    }

    private fun onClientError(errorBody: ResponseBody?) {
        try {
            val gson = GsonBuilder().create()
            val registerErrorResponse =
                gson.fromJson(errorBody!!.string(), RegisterErrorResponse::class.java)
            val message = registerErrorResponse.message[0].messages[0].message
            Snackbar.make(binding.root, message, LENGTH_LONG).show()
        } catch (ex: JsonSyntaxException) {
            onUnexpectedError()
        }
    }

    private fun TextInputLayout.isValid(): Boolean {
        val errorMessage = validator().validate(editText!!.text.toString())
        error = errorMessage?.resolveAsString()
        isErrorEnabled = errorMessage != null
        return errorMessage == null
    }

    private fun Int.resolveAsString() = getString(this)

    private fun TextInputLayout.validator() = when (this) {
        binding.usernameInputLayout -> usernameValidator
        binding.emailInputLayout -> emailValidator
        binding.passwordInputLayout -> passwordValidator
        else -> throw IllegalArgumentException("Cannot find any validator for the given TextInputLayout")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
