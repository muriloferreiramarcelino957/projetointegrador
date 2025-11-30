package com.projetointegrador.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.projetointegrador.databinding.DialogFiltrosBinding
import com.projetointegrador.app.model.FiltrosModel

class FiltroBottomSheet(
    private val onAplicarFiltros: (FiltrosModel) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogFiltrosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFiltrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarSpinnerNivel()
        configurarClickAplicar()
    }

    private fun configurarSpinnerNivel() {

        val niveis = listOf("Todos", "Bronze", "Prata", "Ouro")

        binding.spNivel.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            niveis
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun configurarClickAplicar() {

        binding.btnAplicar.setOnClickListener {

            val filtros = FiltrosModel(
                nivelCadastro = binding.spNivel.selectedItem.toString(),
                servico = binding.etServico.text.toString(),
                avaliacaoMinima = binding.ratingMin.rating.toString(),
                localizacao = binding.tvLocalizacao.text.toString()
            )

            onAplicarFiltros(filtros)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
