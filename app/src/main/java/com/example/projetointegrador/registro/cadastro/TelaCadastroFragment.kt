package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaCadastroBinding
import com.google.firebase.Firebase

class TelaCadastroFragment : Fragment() {

    private var _binding: TelaCadastroBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaCadastroBinding.inflate(inflater, container, false)
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
        binding.button.setOnClickListener {
            findNavController().navigate(R.id.telaCadastro2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}