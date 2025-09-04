package com.allservice.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaPerfilBinding
import com.google.android.material.chip.Chip

class ProfileFragment : Fragment() {

    private var _binding: TelaPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TelaPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        // Ajuste de padding para status bar se necessário
        root.setOnApplyWindowInsetsListener { v, insets ->
            v.updatePadding(top = insets.systemWindowInsetTop)
            insets
        }



        // Fotos horizontais
        photoAdapter = PhotoAdapter { /* clique numa foto */ }
        rvFotos.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
        photoAdapter.submitList(fakePhotos())

        // Botões navegação
        btnAgenda.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_agendaFragment)
        }
        btnVerTodas.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_photosFragment)
        }
        btnChat.setOnClickListener {
            // Aqui você poderia navegar para uma tela de chat
        }
    }

    private fun fakePhotos(): List<Int> =
        listOf(R.drawable.sample_work_1, R.drawable.sample_work_2, R.drawable.sample_work_3)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}