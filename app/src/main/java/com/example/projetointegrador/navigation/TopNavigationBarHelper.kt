package com.example.projetointegrador.navigation

import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R

object TopNavigationBarHelper {

    fun setupNavigationBar(root: View, fragment: Fragment) {

        val navController = fragment.findNavController()

        val btnMenu = root.findViewById<ImageView>(R.id.ic_menu)
        val btnSearch = root.findViewById<ImageView>(R.id.ic_search)
        val btnNotification = root.findViewById<ImageView>(R.id.ic_notification)
        val btnSettings = root.findViewById<ImageView>(R.id.ic_settings)
        val btnProfile = root.findViewById<ImageView>(R.id.ic_person)
        val btnLogo = root.findViewById<ImageView>(R.id.ic_logo)   //  ← AQUI

        // MENU
        btnMenu?.setOnClickListener {
            // Drawer futuramente
        }

        // BUSCA
        btnSearch?.setOnClickListener {
            navController.navigate(R.id.telaBuscaFragment)
        }

        // NOTIFICAÇÕES
        btnNotification?.setOnClickListener {
            navController.navigate(R.id.telaNotificacaoFragment)
        }

        // CONFIGURAÇÕES
        btnSettings?.setOnClickListener {
            navController.navigate(R.id.telaConfiguracoesFragment)
        }

        // PERFIL DO USUÁRIO
        btnProfile?.setOnClickListener {
            navController.navigate(R.id.telaPerfilUsuarioFragment)
        }

        // LOGO → VOLTAR PARA A TELA PRINCIPAL
        btnLogo?.setOnClickListener {
            navController.navigate(R.id.fragmentTelaPrincipal)
        }
    }
}
