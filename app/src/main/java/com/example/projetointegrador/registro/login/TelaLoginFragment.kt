package com.example.projetointegrador.registro.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaLoginBinding

class TelaLoginFragment : Fragment() {

    private var _binding: TelaLoginBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // CORREÇÃO 3: Chame o método inflate na classe de Binding correta.
        _binding = TelaLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_telaLoginFragment_to_recuperacao1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}