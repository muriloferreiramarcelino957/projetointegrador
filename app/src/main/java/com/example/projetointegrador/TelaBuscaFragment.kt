package com.seuprojeto.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.databinding.FragmentTelaBuscaBinding


class TelaBuscaFragment : Fragment() {

    private var _binding: FragmentTelaBuscaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaBuscaBinding.inflate(inflater, container, false)

        val listaPrestadores = listOf(
            Prestador("Seu Hélio", "Jardim da Penha, Vitória - ES", "⭐ 5", "Faxina | Hidráulica | Pintura"),
            Prestador("Maria Coelho", "Laranjeiras, Serra - ES", "⭐ 4.8", "Cuidado com idosos"),
            Prestador("Arthur Silva", "Barcelona, Serra - ES", "⭐ 4.7", "Reparador de móveis"),
            Prestador("Júlia Trindad", "República, Vitória - ES", "⭐ 4.5", "Serviços gerais"),
            Prestador("Heitor Barcelos", "Nova Almeida, Serra - ES", "⭐ 4.2", "Corte e jardinagem")
        )

        val adapter = PrestadorAdapter(listaPrestadores)
        binding.recyclerPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPrestadores.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
