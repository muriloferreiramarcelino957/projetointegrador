package com.example.projetointegrador

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
// CORREÇÃO 1: Importar a classe de Binding correta, gerada a partir do seu XML.
import com.example.projetointegrador.databinding.TelaLoginBinding


class TelaLoginFragment : Fragment() {

    // CORREÇÃO 2: O tipo da variável _binding deve ser a classe gerada, não a classe do Fragment.
    private var _binding: TelaLoginBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}