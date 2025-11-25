package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaTipoDeServico3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TipoDeServico3Fragment : Fragment() {

    private var _binding: TelaTipoDeServico3Binding? = null
    private val binding get() = _binding!!

    private val uid by lazy { FirebaseAuth.getInstance().uid!! }
    private val prestadorRef by lazy {
        FirebaseDatabase.getInstance().reference.child("prestadores").child(uid)
    }

    /** Modelos */
    data class ServicoTemp(val id: String, val valor: String)
    data class TipoServico(val id: String, val nome: String)

    /** Lista temporária compartilhada */
    companion object {
        val listaServicosTemp = mutableListOf<ServicoTemp>()
    }

    /** Lista carregada do Firebase */
    private val servicosFirebase = mutableListOf<TipoServico>()

    /** Contador */
    private val servicosJaPreenchidos get() = listaServicosTemp.size

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaTipoDeServico3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        configurarBotoes()
        configurarAutoComplete()
        carregarTiposServico()
    }

    // -------------------------------------------------------------------------
    //  CONFIGURAÇÕES DE UI
    // -------------------------------------------------------------------------

    private fun configurarBotoes() {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.btnAdicionar.setOnClickListener {
            if (!salvarTemporariamente()) return@setOnClickListener
            findNavController().navigate(R.id.tipoDeServico3Fragment)
        }

        binding.btnOk.setOnClickListener {
            if (!salvarTemporariamente()) return@setOnClickListener
            enviarTodosParaFirebase()
        }

        atualizarVisibilidadeAdicionar()
    }

    private fun configurarAutoComplete() {
        binding.autoServico.keyListener = null
        binding.autoServico.setOnClickListener { binding.autoServico.showDropDown() }
    }

    private fun atualizarVisibilidadeAdicionar() {
        binding.btnAdicionar.visibility =
            if (servicosJaPreenchidos >= 2) View.GONE else View.VISIBLE
    }

    // -------------------------------------------------------------------------
    //  FIREBASE
    // -------------------------------------------------------------------------

    private fun carregarTiposServico() {
        FirebaseDatabase.getInstance().reference.child("tipos_de_servico")
            .get()
            .addOnSuccessListener { snap ->
                servicosFirebase.clear()

                for (tipo in snap.children) {
                    val id = tipo.key ?: continue
                    val nome = tipo.child("dscr_servico").getValue(String::class.java) ?: continue
                    servicosFirebase.add(TipoServico(id, nome))
                }

                atualizarAdapter()
            }
    }

    private fun atualizarAdapter() {
        val nomesDisponiveis = servicosFirebase
            .filterNot { tipo -> listaServicosTemp.any { it.id == tipo.id } }
            .map { it.nome }

        binding.autoServico.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nomesDisponiveis)
        )
    }

    // -------------------------------------------------------------------------
    //  VALIDAÇÃO + SALVAMENTO LOCAL
    // -------------------------------------------------------------------------

    private fun salvarTemporariamente(): Boolean {
        val nome = binding.autoServico.text.toString().trim()
        val valor = binding.editValor.text.toString().trim()

        if (nome.isEmpty()) {
            toast("Selecione um serviço")
            return false
        }
        if (valor.isEmpty()) {
            toast("Digite o valor")
            return false
        }

        val tipo = servicosFirebase.firstOrNull { it.nome == nome }
        if (tipo == null) {
            toast("Serviço inválido")
            return false
        }

        listaServicosTemp.add(ServicoTemp(tipo.id, valor))
        return true
    }

    // -------------------------------------------------------------------------
    //  ENVIO AO FIREBASE (ALTERADO COMO VOCE PEDIU)
    // -------------------------------------------------------------------------

    private fun enviarTodosParaFirebase() {

        // Mapa de serviços: "1" -> "300", "2" -> "100"
        val servicosMap = listaServicosTemp.associate { it.id to it.valor }

        // Quantidade de serviços começa sempre em ZERO
        val quantidadeInicial = 0

        // Descrição padrão
        val descricaoPadrao = "Olá, sou um prestador da AllService!"

        val updates = mapOf(
            "servicos_oferecidos" to servicosMap,
            "nivel_cadastro" to "bronze",
            "quantidade_de_servicos" to quantidadeInicial,
            "info_prestador/descricao" to descricaoPadrao
        )

        prestadorRef.updateChildren(updates)
            .addOnSuccessListener {
                toast("Serviços salvos!")
                listaServicosTemp.clear()
                findNavController().navigate(R.id.navigation)
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
