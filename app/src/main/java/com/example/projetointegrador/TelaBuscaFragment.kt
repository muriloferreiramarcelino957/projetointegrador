package com.example.projetointegrador.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FragmentTelaBuscaBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.projetointegrador.app.model.FiltrosModel
import com.projetointegrador.app.ui.FiltroBottomSheet
import com.projetointegrador.app.ui.PrestadorAdapter

class TelaBuscaFragment : Fragment() {

    private var _binding: FragmentTelaBuscaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PrestadorAdapter

    // Lista original (vinda do banco)
    private var listaCompleta = listOf<PrestadorDisplay>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaBuscaBinding.inflate(inflater, container, false)
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
        configurarRecycler()
        carregarPrestadoresDoFirebase()
        configurarBusca()
        configurarBotoes()
        configurarMenuLateral()

        return binding.root
    }

    private fun configurarRecycler() {
        adapter = PrestadorAdapter()
        binding.recyclerPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPrestadores.adapter = adapter
    }

    private fun carregarPrestadoresDoFirebase() {
        val db = FirebaseFirestore.getInstance()

        db.collection("prestadores")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(PrestadorDisplay::class.java)
                listaCompleta = lista
                adapter.atualizarLista(lista)
            }
    }

    private fun configurarBusca() {
        binding.etBuscarPrestador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(texto: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarPesquisa(texto.toString())
            }
        })
    }

    private fun aplicarPesquisa(busca: String) {
        val filtrado = listaCompleta.filter { p ->
            p.user.nome.contains(busca, ignoreCase = true) ||
                    p.servicos.keys.any { it.contains(busca, ignoreCase = true) }
        }
        adapter.atualizarLista(filtrado)
    }

    private fun configurarBotoes() {
        binding.btnFiltros.setOnClickListener {
            FiltroBottomSheet { filtros ->
                aplicarFiltros(filtros)
            }.show(parentFragmentManager, "FiltroBottomSheet")
        }

        binding.btnVoltar.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun configurarMenuLateral() {
        val btnMenu = binding.topBar.root.findViewById<ImageView>(R.id.ic_menu)
        val drawerLayout = binding.root.findViewById<DrawerLayout>(R.id.drawerLayout)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * 🟣 FILTROS APLICADOS
     */
    private fun aplicarFiltros(f: FiltrosModel) {

        val filtrados = listaCompleta.filter { p ->

            val nivelPrestador = p.prestador.nivel_cadastro ?: ""

            val nivelOk =
                f.nivelCadastro == "Todos" ||
                        nivelPrestador.equals(f.nivelCadastro, ignoreCase = true)

            val servicoOk =
                f.servico.isBlank() ||
                        p.servicos.keys.any { it.contains(f.servico, ignoreCase = true) }

            val avaliacaoOk =
                (p.prestador.info_prestador?.notaMedia ?: 0.0) >= f.avaliacaoMinima.toDouble()

            val localOk =
                f.localizacao.isBlank() ||
                        p.user.cidade.contains(f.localizacao, ignoreCase = true)

            nivelOk && servicoOk && avaliacaoOk && localOk
        }

        adapter.atualizarLista(filtrados)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
