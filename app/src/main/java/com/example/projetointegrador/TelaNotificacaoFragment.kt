package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.FragmentTelaNotificacaoBinding

class TelaNotificacaoFragment : Fragment() {

    private var _binding: FragmentTelaNotificacaoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaNotificacaoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botão de voltar
        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }

        // Exemplo de ação no botão "Marcar todas como lidas"
        binding.btnMarcarComoLidas.setOnClickListener {
            Toast.makeText(requireContext(), "Notificações marcadas como lidas!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
