package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.FragmentTelaPerfilBinding
import com.google.firebase.Firebase

class TelaPerfilFragment : Fragment() {

    private var _binding: FragmentTelaPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        Firebase
    }

    private fun initListeners() {
        val navController = findNavController()

        // Botão principal
        binding.btnAgenda.setOnClickListener {
            navController.navigate(R.id.action_telaPerfilFragment_to_telaAgendaFragment)
        }

        // Ícones do topo
        binding.icMenu.setOnClickListener {
            Toast.makeText(requireContext(), "Abrindo menu...", Toast.LENGTH_SHORT).show()
        }

        binding.icSearch.setOnClickListener {
            Toast.makeText(requireContext(), "Abrindo busca...", Toast.LENGTH_SHORT).show()
        }

        binding.icNotification.setOnClickListener {
            navController.navigate(R.id.action_telaPerfilFragment_to_telaNotificacaoFragment)
        }

        binding.icSettings.setOnClickListener {
            navController.navigate(R.id.action_telaPerfilFragment_to_telaConfiguracoesFragment)
        }

        binding.icPerson.setOnClickListener {
            val currentDestination = navController.currentDestination?.id
            if (currentDestination == R.id.telaPerfilFragment) {
                // Já está no Perfil → apenas manter o botão "ativo"
                binding.icPerson.isSelected = true
            } else {
                navController.navigate(R.id.telaPerfilFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}