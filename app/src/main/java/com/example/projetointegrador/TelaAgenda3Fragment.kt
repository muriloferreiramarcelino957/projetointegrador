package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeAgenda3Binding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TelaAgenda3Fragment : Fragment() {

    private var _binding: TelaDeAgenda3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgenda3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        // Botão de voltar (seta)
        binding.btnArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Botão de concluir
        binding.btnConcluir.setOnClickListener {
            mostrarDialogoConcluido()
        }
    }

    private fun mostrarDialogoConcluido() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sucesso, null)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}