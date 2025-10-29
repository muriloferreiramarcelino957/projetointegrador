package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeEditarPerfilBinding

class TelaEditarPerfil : Fragment() {

    private var _binding: TelaDeEditarPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeEditarPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        // Botão de voltar
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Botão salvar alterações
        binding.btnSalvar.setOnClickListener {
            val nome = binding.etNome.text.toString().trim()
            val descricao = binding.etDescricao.text.toString().trim()

            if (nome.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                // Aqui você pode salvar no banco, API ou localmente
                Toast.makeText(requireContext(), "Edições salvas!", Toast.LENGTH_SHORT).show()

                // Voltar para a tela anterior
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}