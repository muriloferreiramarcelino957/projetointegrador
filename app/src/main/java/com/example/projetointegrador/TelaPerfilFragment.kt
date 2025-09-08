package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class TelaPerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tela_perfil, container, false)

        // Botão principal
        val btnAgenda = view.findViewById<Button>(R.id.btnAgenda)

        // Ícones do topo
        val btnMenu = view.findViewById<ImageView>(R.id.ic_menu)
        val btnSearch = view.findViewById<ImageView>(R.id.ic_search)
        val btnNotification = view.findViewById<ImageView>(R.id.ic_notification)
        val btnSettings = view.findViewById<ImageView>(R.id.ic_settings)
        val btnPerfil = view.findViewById<ImageView>(R.id.ic_person)

        // Clique no botão "Minha Agenda"
        btnAgenda.setOnClickListener {
            findNavController().navigate(R.id.action_telaPerfilFragment_to_telaAgendaFragment)
        }

        btnMenu.setOnClickListener {
            Toast.makeText(requireContext(), "Abrindo menu...", Toast.LENGTH_SHORT).show()
        }

        btnSearch.setOnClickListener {
            Toast.makeText(requireContext(), "Abrindo busca...", Toast.LENGTH_SHORT).show()
        }

        btnNotification.setOnClickListener {
            findNavController().navigate(R.id.action_telaPerfilFragment_to_telaNotificacaoFragment)
        }

        btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_telaPerfilFragment_to_telaConfiguracoesFragment)
        }

        btnPerfil.setOnClickListener {
            val navController = findNavController()
            val currentDestination = navController.currentDestination?.id

            if (currentDestination == R.id.telaPerfilFragment) {
                // Já está no Perfil → apenas manter o botão "ativo"
                btnPerfil.isSelected = true
            } else {
                // Se não estiver, navega para o Perfil
                navController.navigate(R.id.telaPerfilFragment)
            }
        }

        return view
    }
}
