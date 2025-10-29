package com.example.projetointegrador.registro.recuperacao

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaDeRecuperacao3Binding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Recuperacao3 : Fragment() {

    private var _binding : TelaDeRecuperacao3Binding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TelaDeRecuperacao3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }
    private fun initListeners(){
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.button.setOnClickListener {
            mostrarDialogoConcluido()
        }
    }

    private fun mostrarDialogoConcluido() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_senha, null)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .show()
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuth()
        }, 3000)
    }

    private fun checkAuth(){
        findNavController().navigate(R.id.action_recuperacao3_to_telaLoginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}