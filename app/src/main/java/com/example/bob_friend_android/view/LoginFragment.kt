package com.example.bob_friend_android.view

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.example.bob_friend_android.App
import com.example.bob_friend_android.R
import com.example.bob_friend_android.SharedPref
import com.example.bob_friend_android.base.BaseFragment
import com.example.bob_friend_android.databinding.FragmentLoginBinding
import com.example.bob_friend_android.viewmodel.LoginViewModel

class LoginFragment(override val viewModel: LoginViewModel) : BaseFragment<FragmentLoginBinding, LoginViewModel>(
    R.layout.fragment_login
) {
    private var checked = false

    override fun init() {
        SharedPref.openSharedPrep(requireContext())

        val check = App.prefs.getBoolean("checked",false)
        if (check) {
            viewModel.validateUser()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            checked = binding.checkBoxAutoLogin.isChecked

            viewModel.login(email ,password)
        }

        binding.btnRegister.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_joinFragment)
        }

        binding.btnFindUserAccount.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_findUserAccountFragment)
        }

        binding.loginLayout.setOnClickListener {
            hideKeyboard()
        }

        observeData()
    }

    private fun hideKeyboard(){
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editTextEmail.windowToken, 0)
        imm.hideSoftInputFromWindow(binding.editTextPassword.windowToken, 0)
    }

    private fun observeData() {
        with(viewModel) {
            errorMsg.observe(viewLifecycleOwner) {
                showToast(it)
                if (it == "자동 로그인"){
                }
            }

            token.observe(viewLifecycleOwner) {
                val editor = App.prefs.edit()
                editor.putString("token", it.accessToken)
                editor.putString("refresh", it.refreshToken)
                editor.putBoolean("checked", checked)
                editor.apply()

                navController.navigate(R.id.action_loginFragment_to_homeFragment)
            }

            refreshToken.observe(viewLifecycleOwner) {
                val editor = App.prefs.edit()
                editor.putString("token", it.accessToken)
                editor.putString("refresh", it.refreshToken)
                editor.putBoolean("checked", true)
                editor.apply()

                navController.navigate(R.id.action_loginFragment_to_homeFragment)
            }

            val dialog = SetLoadingDialog(requireContext())
            progressVisible.observe(viewLifecycleOwner) {
                if (progressVisible.value!!) {
                    dialog.show()
                }
                else if (!progressVisible.value!!) {
                    dialog.dismiss()
                }
            }
        }
    }
}