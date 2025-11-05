package com.seuprojeto.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.projetointegrador.databinding.FragmentTelaBuscaBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper


class TelaBuscaFragment : Fragment() {

    private var _binding: FragmentTelaBuscaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaBuscaBinding.inflate(inflater, container, false)
        setupNavigationBar()
        return binding.root
    }
    
    private fun setupNavigationBar() {
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
