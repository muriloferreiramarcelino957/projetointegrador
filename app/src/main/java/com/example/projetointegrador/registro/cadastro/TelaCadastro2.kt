package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FragmentTelaCadastro2Binding
import com.example.projetointegrador.databinding.TelaCadastroBinding


class TelaCadastro2 : Fragment() {

    private var _binding: FragmentTelaCadastro2Binding? = null
    private val binding get() = _binding!!
    private val opcoesUF = arrayListOf("Selecione um estado", "1", "2", "3")
    private val opcoesTipoLogradouro = arrayListOf("Selecione um tipo de logradouro", "Avenida", "Rua", "Alameda")
    private var opcaoSelecionadaUF: String = ""
    private var opcaoSelecionadaTipoLogradouro: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTelaCadastro2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        configurarSpinner(binding.spinnerUF, opcoesUF){uf ->
            opcaoSelecionadaUF = uf
        }
        configurarSpinner(binding.spinnerTipoLogradouro, opcoesTipoLogradouro){tipoLogradouro ->
            opcaoSelecionadaTipoLogradouro = tipoLogradouro
        }
    }

    private fun configurarSpinner(spinner: android.widget.Spinner, itens: List<String>,onSelected: (String) -> Unit){
    val adapter = ArrayAdapter(binding.root.context,android.R.layout.simple_spinner_item,itens)

    spinner.adapter = adapter
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?,view: View?,position: Int,id: Long){
            onSelected(if (position > 0) itens[position] else "")
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {
            onSelected("")
        }
    }


}
    private fun initListeners(){
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}