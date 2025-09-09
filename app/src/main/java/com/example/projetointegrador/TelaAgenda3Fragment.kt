package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TelaAgenda3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tela_agenda3, container, false)

        // Botão de voltar (seta)
        val btnVoltar: ImageView = view.findViewById(R.id.btn_arrow_back)
        btnVoltar.setOnClickListener {
            findNavController().popBackStack() // Volta para o fragmento anterior
        }

        // Botão de concluir
        val btnConcluir: Button = view.findViewById(R.id.btnConcluir)
        btnConcluir.setOnClickListener {
            mostrarDialogoConcluido()
        }

        return view
    }

    private fun mostrarDialogoConcluido() {
        // Cria uma visualização personalizada do diálogo com a imagem de sucesso
        val dialogView = layoutInflater.inflate(R.layout.dialog_sucesso, null)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .show()
    }
}
