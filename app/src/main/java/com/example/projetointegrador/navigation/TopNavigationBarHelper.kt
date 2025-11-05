package com.example.projetointegrador.navigation

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R

object TopNavigationBarHelper {
    
    fun setupNavigationBar(view: View, fragment: androidx.fragment.app.Fragment) {
        val navController = fragment.findNavController()
        
        view.findViewById<View>(R.id.ic_menu)?.setOnClickListener {
            // Volta para a tela anterior ou abre drawer menu
            if (!navController.navigateUp()) {
                // Se não há tela anterior, vai para home
                navController.navigate(R.id.fragmentTelaPrincipal)
            }
        }
        
        view.findViewById<View>(R.id.ic_search)?.setOnClickListener {
            navController.navigate(R.id.telaBuscaFragment)
        }
        
        view.findViewById<View>(R.id.ic_logo)?.setOnClickListener {
            navController.navigate(R.id.fragmentTelaPrincipal)
        }
        
        view.findViewById<View>(R.id.ic_notification)?.setOnClickListener {
            navController.navigate(R.id.telaNotificacaoFragment)
        }
        
        view.findViewById<View>(R.id.ic_settings)?.setOnClickListener {
            navController.navigate(R.id.telaConfiguracoesFragment)
        }
        
        view.findViewById<View>(R.id.ic_person)?.setOnClickListener {
            navController.navigate(R.id.telaPerfilFragment)
        }
    }
}

