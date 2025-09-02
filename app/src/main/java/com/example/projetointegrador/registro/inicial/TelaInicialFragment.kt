package com.example.projetointegrador.registro.inicial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaInicialBinding

class TelaInicialFragment : Fragment() {

    private var _binding: TelaInicialBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaInicialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }
    private fun initListeners(){
        binding.registerButton.setOnClickListener{
            findNavController().navigate(R.id.action_telaInicialFragment_to_telaCadastroFragment)
        }
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_telaInicialFragment_to_telaLoginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}