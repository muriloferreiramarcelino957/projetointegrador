package com.example.projetointegrador.navigation

import android.view.View
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R

object TopNavigationBarHelper {

    fun setupNavigationBar(topBar: View, fragment: Fragment) {

        val nav = fragment.findNavController()

        val btnMenu = topBar.findViewById<ImageView>(R.id.ic_menu)
        val btnSearch = topBar.findViewById<ImageView>(R.id.ic_search)
        val btnNotification = topBar.findViewById<ImageView>(R.id.ic_notification)
        val btnSettings = topBar.findViewById<ImageView>(R.id.ic_settings)
        val btnProfile = topBar.findViewById<ImageView>(R.id.ic_person)
        val btnLogo = topBar.findViewById<ImageView>(R.id.ic_logo)

        // MENU
        btnMenu?.setOnClickListener {
            val drawer = fragment.requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout)
            drawer.openDrawer(GravityCompat.START)
        }

        btnSearch?.setOnClickListener {
            nav.navigate(R.id.telaBuscaFragment)
        }

        btnNotification?.setOnClickListener {
            nav.navigate(R.id.telaNotificacaoFragment)
        }

        btnSettings?.setOnClickListener {
            nav.navigate(R.id.telaConfiguracoesFragment)
        }

        btnProfile?.setOnClickListener {
            nav.navigate(R.id.telaPerfilUsuarioFragment)
        }

        btnLogo?.setOnClickListener {
            nav.navigate(R.id.fragmentTelaPrincipal)
        }
    }
}
