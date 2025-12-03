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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.InfoPrestador
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FragmentTelaBuscaBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.database.FirebaseDatabase
import com.projetointegrador.app.model.FiltrosModel
import com.projetointegrador.app.ui.FiltroBottomSheet
import com.projetointegrador.app.ui.PrestadorAdapter

class TelaBuscaFragment : Fragment() {

    private var _binding: FragmentTelaBuscaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PrestadorAdapter
    private val args by navArgs<TelaBuscaFragmentArgs>()

    private var filtrosAtuais: FiltrosModel? = null

    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTelaBuscaBinding.inflate(inflater, container, false)
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)

        configurarRecycler()
        configurarBusca()
        configurarBotoes()
        configurarMenuLateral()

        val servicoInicial = args.tipoDeServico

        if (servicoInicial.isNotBlank()) {
            filtrosAtuais = FiltrosModel(
                nivelCadastro = "Todos",
                servico = servicoInicial,
                avaliacaoMinima = "0",
                localizacao = ""
            )
            buscarPrestadores(servicoId = servicoInicial)
        } else {
            buscarPrestadores()
        }

        return binding.root
    }

    private fun configurarRecycler() {
        adapter = PrestadorAdapter()
        binding.recyclerPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPrestadores.adapter = adapter
    }

    private fun configurarBusca() {
        binding.etBuscarPrestador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                buscarPrestadores(nome = text.toString())
            }
        })
    }

    private fun configurarBotoes() {

        binding.btnFiltros.setOnClickListener {

            val bottom = FiltroBottomSheet { filtros ->

                filtrosAtuais = filtros

                buscarPrestadores(
                    nome = binding.etBuscarPrestador.text.toString(),
                    servicoId = filtros.servico,
                    nivel = filtros.nivelCadastro,
                    avaliacaoMin = filtros.avaliacaoMinima.toDoubleOrNull(),
                    cidade = filtros.localizacao
                )
            }

            bottom.arguments = Bundle().apply {
                putString("nivel", filtrosAtuais?.nivelCadastro ?: "Todos")
                putString("servico", filtrosAtuais?.servico ?: "")
                putString("avaliacao", filtrosAtuais?.avaliacaoMinima ?: "0")
                putString("cidade", filtrosAtuais?.localizacao ?: "")
            }

            bottom.show(parentFragmentManager, "FiltroBottomSheet")
        }

        binding.btnVoltar.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun configurarMenuLateral() {
        val btnMenu = binding.topBar.root.findViewById<ImageView>(R.id.ic_menu)
        val drawerLayout = binding.root.findViewById<DrawerLayout>(R.id.drawerLayout)
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
    }

    // ==========================================================
    // BUSCA REAL — filtrando por KEY correta do serviço ("1","2","3","4")
    // ==========================================================
    private fun buscarPrestadores(
        nome: String = "",
        servicoId: String = "",
        nivel: String = "",
        avaliacaoMin: Double? = null,
        cidade: String = ""
    ) {
        db.child("prestadores").get().addOnSuccessListener { snapPrestadores ->

            val listaFinal = mutableListOf<PrestadorDisplay>()

            db.child("usuarios").get().addOnSuccessListener { snapUsuarios ->

                snapPrestadores.children.forEach { prestSnap ->

                    val uid = prestSnap.key ?: return@forEach
                    val userSnap = snapUsuarios.child(uid)

                    val nomeUser = userSnap.child("nome").value?.toString() ?: ""
                    val cidadeUser = userSnap.child("cidade").value?.toString() ?: ""

                    val info = InfoPrestador(
                        notaMedia = prestSnap.child("info_prestador/notaMedia")
                            .getValue(Double::class.java) ?: 0.0,
                        nivel_cadastro = prestSnap.child("nivel_cadastro").value?.toString() ?: "",
                        quantidade_de_servicos = prestSnap.child("info_prestador/quantidade_de_servicos")
                            .getValue(Int::class.java) ?: 0
                    )

                    // ------------- AQUI ESTÁ A CORREÇÃO FINAL -------------
                    // Pega **a KEY**, que é o ID do serviço (1,2,3,4)
                    val servicosIds = prestSnap.child("servicos_oferecidos")
                        .children.mapNotNull { it.key }

                    // ================ FILTROS =================
                    val filtroNome = nome.isBlank() || nomeUser.contains(nome, ignoreCase = true)
                    val filtroCidade = cidade.isBlank() || cidadeUser.contains(cidade, ignoreCase = true)
                    val filtroNivel = nivel.isBlank() || nivel == "Todos" ||
                            nivel.equals(info.nivel_cadastro, ignoreCase = true)
                    val filtroAvaliacao = avaliacaoMin == null || info.notaMedia >= avaliacaoMin

                    val filtroServico =
                        servicoId.isBlank() || servicosIds.contains(servicoId)

                    // ========================================
                    if (filtroNome && filtroCidade && filtroNivel && filtroAvaliacao && filtroServico) {
                        listaFinal.add(
                            PrestadorDisplay(
                                uid = uid,
                                nome = nomeUser,
                                cidade = cidadeUser,
                                info_prestador = info,
                                servicos = servicosIds
                            )
                        )
                    }
                }

                adapter.atualizarLista(listaFinal)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
