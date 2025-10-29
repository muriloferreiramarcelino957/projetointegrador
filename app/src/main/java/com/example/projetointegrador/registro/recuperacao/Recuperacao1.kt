package com.example.projetointegrador.registro.recuperacao

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaDeRecuperacaoBinding


class Recuperacao1 : Fragment() {

    private var _binding : TelaDeRecuperacaoBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TelaDeRecuperacaoBinding.inflate(inflater, container, false)
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
            findNavController().navigate(R.id.action_recuperacao1_to_recuperacao2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}