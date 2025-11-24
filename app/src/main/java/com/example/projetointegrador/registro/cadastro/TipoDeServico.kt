package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaTipoDeServico1Binding

class TipoDeServico1Fragment : Fragment() {

    private var _binding: TelaTipoDeServico1Binding? = null
    private val binding: TelaTipoDeServico1Binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaTipoDeServico1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnProximo.setOnClickListener {
            val diasSelecionados = getDiasSelecionados()

            if (diasSelecionados.isEmpty()) {
                Toast.makeText(requireContext(), "Selecione ao menos um dia.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val action = TipoDeServico1FragmentDirections
                .actionTipoDeServico1FragmentToTipoDeServico2(
                    diasSelecionados.toTypedArray()
                )

            findNavController().navigate(action)
        }
    }

    private fun getDiasSelecionados(): List<String> = buildList {
        if (binding.cbSeg.isChecked) add("Segunda")
        if (binding.cbTer.isChecked) add("Terça")
        if (binding.cbQua.isChecked) add("Quarta")
        if (binding.cbQui.isChecked) add("Quinta")
        if (binding.cbSex.isChecked) add("Sexta")
        if (binding.cbSab.isChecked) add("Sábado")
        if (binding.cbDom.isChecked) add("Domingo")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
