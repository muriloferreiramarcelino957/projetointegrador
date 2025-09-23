package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.FragmentTelaAgenda2Binding

class TelaAgenda2Fragment : Fragment() {

    private var _binding: FragmentTelaAgenda2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaAgenda2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        // Botão de voltar
        binding.btnArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setas de "ver mais" dos agendamentos
        val arrowButtons = listOf(
            binding.arrowForward,
            binding.arrowForward2,
            binding.arrowForward3
        )

        for (arrow in arrowButtons) {
            arrow.setOnClickListener {
                findNavController().navigate(R.id.action_telaAgenda2Fragment_to_telaAgenda3Fragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
