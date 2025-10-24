package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FlagPrestadorBinding
import com.example.projetointegrador.databinding.TelaDeCadastro2Binding


class TelaCadastro2 : Fragment() {

    private var _binding: TelaDeCadastro2Binding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TelaDeCadastro2Binding.inflate(inflater, container, false)
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
        binding.btnCadastrar.setOnClickListener {
            val flag = FlagPrestadorBinding.inflate(layoutInflater)
            val overlay = flag.root
            overlay.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val root = binding.root
            root.addView(overlay)

            flag.btnSim.setOnClickListener {
                findNavController().navigate(R.id.action_telaCadastro2_to_tipoDeServico1Fragment)
            }
            flag.btnNao.setOnClickListener {
                Toast.makeText(requireContext(), "Ir para tela principal", Toast.LENGTH_SHORT).show()
                root.removeView(overlay)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}