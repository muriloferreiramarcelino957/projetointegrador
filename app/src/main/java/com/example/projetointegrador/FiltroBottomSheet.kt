package com.projetointegrador.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.projetointegrador.databinding.DialogFiltrosBinding
import com.google.firebase.database.FirebaseDatabase
import com.projetointegrador.app.model.FiltrosModel

class FiltroBottomSheet(
    private val onAplicarFiltros: (FiltrosModel) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogFiltrosBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseDatabase.getInstance().reference

    private val mapaServicos = mutableMapOf<String, String>()
    private var selectedServiceId: String = ""

    private var filtroNivel = "Todos"
    private var filtroServico = ""
    private var filtroAvaliacao = 0.0
    private var filtroCidade = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFiltrosBinding.inflate(inflater, container, false)

        val args = arguments
        filtroNivel = args?.getString("nivel", "Todos") ?: "Todos"
        filtroServico = args?.getString("servico", "") ?: ""
        filtroAvaliacao = args?.getString("avaliacao", "0")?.toDoubleOrNull() ?: 0.0
        filtroCidade = args?.getString("cidade", "") ?: ""

        configurarSpinnerNivel()
        carregarServicos()
        carregarCidades()

        binding.ratingMin.rating = filtroAvaliacao.toFloat()
        binding.tvLocalizacao.setText(filtroCidade, false)

        return binding.root
    }

    private fun carregarServicos() {
        db.child("tipos_de_servico").get().addOnSuccessListener { snap ->

            val nomesServicos = mutableListOf("Nenhum serviço especificado")
            mapaServicos.clear()

            snap.children.forEach { tipo ->
                val id = tipo.key ?: return@forEach
                val nome = tipo.child("dscr_servico").value?.toString() ?: ""
                if (nome.isNotBlank()) {
                    mapaServicos[id] = nome
                    nomesServicos.add(nome)
                }
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                nomesServicos
            )

            binding.etServicos.setAdapter(adapter)
            binding.etServicos.isFocusable = false

            binding.etServicos.setOnItemClickListener { _, _, pos, _ ->
                if (pos == 0) {
                    selectedServiceId = ""
                } else {
                    val nome = nomesServicos[pos]
                    selectedServiceId = mapaServicos.entries.first { it.value == nome }.key
                }
            }

            if (filtroServico.isNotBlank()) {
                val nome = mapaServicos[filtroServico]
                if (!nome.isNullOrBlank()) {
                    binding.etServicos.setText(nome, false)
                    selectedServiceId = filtroServico
                }
            }

            binding.etServicos.setOnClickListener { binding.etServicos.showDropDown() }
        }
    }
    private fun carregarCidades() {
        db.child("usuarios").get().addOnSuccessListener { snap ->

            val cidades = snap.children
                .mapNotNull { it.child("cidade").value?.toString() }
                .distinct()
                .sorted()

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                cidades
            )

            binding.tvLocalizacao.setAdapter(adapter)
        }
    }

    private fun configurarSpinnerNivel() {

        val niveis = listOf("Todos", "Bronze", "Prata", "Ouro")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            niveis
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNivel.adapter = adapter

        val pos = niveis.indexOf(filtroNivel)
        if (pos >= 0) binding.spNivel.setSelection(pos)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnAplicar.setOnClickListener {

            val filtros = FiltrosModel(
                nivelCadastro = binding.spNivel.selectedItem.toString(),
                servico = selectedServiceId,
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
