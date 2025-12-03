package com.projetointegrador.app.ui

import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.example.projetointegrador.R
import com.google.firebase.auth.FirebaseAuth

object MenuLateralHelper {

    fun setupMenu(
        drawerLayout: DrawerLayout,
        navigationView: NavigationView,
        fragment: Fragment
    ) {

        val header = navigationView.getHeaderView(0)
        val nav = fragment.findNavController()

        header.findViewById<View>(R.id.btnVoltar).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            nav.navigateUp()
        }

        header.findViewById<View>(R.id.btnHome).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            nav.navigate(R.id.fragmentTelaPrincipal)
        }

        header.findViewById<View>(R.id.btnSair).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)

            FirebaseAuth.getInstance().signOut()

            nav.navigate(R.id.telaInicialFragment)
        }
    }
}
