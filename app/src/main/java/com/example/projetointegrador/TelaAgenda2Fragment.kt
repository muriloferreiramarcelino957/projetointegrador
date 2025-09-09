package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class TelaAgenda2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tela_agenda2, container, false)

        // Botão de voltar
        val btnArrowBack: ImageView = view.findViewById(R.id.btn_arrow_back)
        btnArrowBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Setas de "ver mais" dos agendamentos
        val arrowForward1: ImageView = view.findViewById(R.id.arrow_forward)
        val arrowForward2: ImageView = view.findViewById(R.id.arrow_forward2)
        val arrowForward3: ImageView = view.findViewById(R.id.arrow_forward3)

        // Clique nas setas - exemplo com Toast, você pode substituir por navegação real
        arrowForward1.setOnClickListener {
            findNavController().navigate(R.id.action_telaAgenda2Fragment_to_telaAgenda3Fragment)
        }

        arrowForward2.setOnClickListener {
            findNavController().navigate(R.id.action_telaAgenda2Fragment_to_telaAgenda3Fragment)
        }

        arrowForward3.setOnClickListener {
            findNavController().navigate(R.id.action_telaAgenda2Fragment_to_telaAgenda3Fragment)
        }

        return view
    }
}
