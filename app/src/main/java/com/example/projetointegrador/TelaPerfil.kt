package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.projetointegrador.databinding.TelaPerfilBinding

class ProfileFragment : Fragment() {

    private var _binding: TelaPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aqui você pode adicionar listeners para botões quando necessário
        // Por exemplo:
        // binding.btnAgenda.setOnClickListener {
        //     // Ação do botão
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}