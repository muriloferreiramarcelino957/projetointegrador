package com.example.projetointegrador

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.projetointegrador.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController   // ← FALTAVA ISSO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        initNavigation()
        initMenu()
    }

    // ---------------------------------------------
    // INICIALIZAÇÃO DO NAVIGATION COMPONENT
    // ---------------------------------------------
    private fun initNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }

    // ---------------------------------------------
    // CONFIGURAÇÃO DO MENU LATERAL
    // ---------------------------------------------
    private fun initMenu() {

        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        // pega o header do menu lateral
        val header = navigationView.getHeaderView(0)

        val btnHome = header.findViewById<LinearLayout>(R.id.btnHome)
        val btnSair = header.findViewById<LinearLayout>(R.id.btnSair)
        val btnVoltar = header.findViewById<ImageView>(R.id.btnVoltar)

        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)

        // botão voltar fecha o drawer
        btnVoltar.setOnClickListener {
            drawer.closeDrawers()
        }

        // página inicial
        btnHome.setOnClickListener {
            navController.navigate(R.id.fragmentTelaPrincipal)
            drawer.closeDrawers()
        }

        // sair
        btnSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(R.id.telaInicialFragment)
            drawer.closeDrawers()
        }
    }
}
