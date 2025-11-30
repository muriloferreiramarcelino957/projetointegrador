package com.projetointegrador.app.ui

import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.example.projetointegrador.R

object MenuLateralHelper {

    fun setupMenu(
        drawerLayout: DrawerLayout,
        navigationView: NavigationView,
        fragment: Fragment
    ) {

        val header = navigationView.getHeaderView(0)

        // SETA DE VOLTAR
        header.findViewById<View>(R.id.btnVoltar).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            fragment.findNavController().navigateUp()
        }

        // HOME
        header.findViewById<View>(R.id.btnHome).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            fragment.findNavController().navigate(R.id.fragmentTelaPrincipal)
        }

        // AGENDAMENTOS
        header.findViewById<View>(R.id.btnAgendamentos).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            fragment.findNavController().navigate(R.id.telaAgendaFragment)
        }

        // SAIR
        header.findViewById<View>(R.id.btnSair).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            fragment.findNavController().navigate(R.id.telaInicialFragment)
        }
    }
}
