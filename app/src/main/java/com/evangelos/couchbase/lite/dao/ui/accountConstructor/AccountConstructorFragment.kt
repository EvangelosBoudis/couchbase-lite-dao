package com.evangelos.couchbase.lite.dao.ui.accountConstructor

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.evangelos.couchbase.lite.dao.R
import com.evangelos.couchbase.lite.dao.databinding.FragmentAccountDetailsBinding
import com.evangelos.couchbase.lite.dao.presentation.AccountConstructorViewModel
import com.evangelos.couchbase.lite.dao.presentation.ViewModelFactory
import com.evangelos.couchbase.lite.dao.util.viewModels

class AccountConstructorFragment : Fragment(R.layout.fragment_account_details) {

    private val viewModel: AccountConstructorViewModel by viewModels(::ViewModelFactory)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAccountDetailsBinding.bind(view)
        binding.apply {
            saveFab.setOnClickListener {
                viewModel.saveAccount(
                    nameField.text?.toString(),
                    emailField.text?.toString(),
                    usernameField.text?.toString(),
                    passwordField.text?.toString()
                ).observe(viewLifecycleOwner) { result ->
                    result.fold(onSuccess = {
                        requireActivity().onBackPressed()
                    }, onFailure = {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }

}