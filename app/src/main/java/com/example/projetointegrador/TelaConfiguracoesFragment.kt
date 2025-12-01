package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeConfiguracoesBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper

class TelaConfiguracoesFragment : Fragment() {

    private var _binding: TelaDeConfiguracoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeConfiguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
        initListeners()
    }

    private fun initListeners() {
        binding.switchTema.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_telaConfiguracoesFragment_to_telaEditarPerfil)
        }
    }
    private fun configurarMenuLateral() {
        val btnMenu = binding.topBar.root.findViewById<ImageView>(R.id.ic_menu)
        val drawerLayout = binding.root.findViewById<DrawerLayout>(R.id.drawerLayout)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
