package com.projetointegrador.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FragmentTelaPrincipalBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.example.projetointegrador.registro.cadastro.Prestador
import com.example.projetointegrador.registro.cadastro.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase

@Suppress("UNREACHABLE_CODE")
class FragmentTelaPrincipal : Fragment() {

    private var _binding: FragmentTelaPrincipalBinding? = null
    private val binding get() = _binding!!

    private lateinit var prestadorAdapter: PrestadorAdapter

    // 👉 ADICIONADO PARA O MENU LATERAL
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaPrincipalBinding.inflate(inflater, container, false)


        drawerLayout = binding.root.findViewById(R.id.drawerLayout)
        navigationView = binding.root.findViewById(R.id.navigationView)

        // Abrir menu ao clicar no botão do include
        binding.root.findViewById<View>(R.id.ic_menu)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Clique nos itens do menu lateral (opcional)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        setupRecyclerView()
        setupNavigationBar()
        carregarPrestadores()

        return binding.root
    }

    private fun setupNavigationBar() {
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
    }

    private fun setupRecyclerView() {
        prestadorAdapter = PrestadorAdapter { prestadorDisplay ->
            val action = FragmentTelaPrincipalDirections
                .actionFragmentTelaPrincipalToTelaPerfilFragment(prestadorDisplay.uid)
            findNavController().navigate(action)
        }

        binding.rvPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrestadores.adapter = prestadorAdapter
    }

    // ================================
    // CARREGA PRESTADORES
    // ================================

    private fun carregarPrestadores() {
        val db = FirebaseDatabase.getInstance().reference

        db.child("prestadores").get().addOnSuccessListener { snapPrestadores ->

            val lista = mutableListOf<PrestadorDisplay>()

            snapPrestadores.children.forEach { prestadorSnap ->

                val uid = prestadorSnap.key ?: return@forEach

                db.child("usuarios").child(uid).get().addOnSuccessListener { userSnap ->

                    val user = userSnap.getValue(User::class.java) ?: User()

                    val dataCadastro =
                        prestadorSnap.child("data_cadastro")
                            .getValue(String::class.java) ?: ""

                    val ultimoAcesso =
                        prestadorSnap.child("ultimo_acesso")
                            .getValue(String::class.java) ?: ""

                    val prestadorInfo =
                        prestadorSnap.child("info_prestador")
                            .getValue(Prestador::class.java)
                            ?: Prestador(notaMedia = 0.0)

                    val servicos = prestadorSnap.child("servicos_oferecidos")
                        .children.associate {
                            it.key!! to it.value.toString()
                        }

                    val prestadorCorrigido = prestadorInfo.copy(
                        notaMedia = prestadorInfo.notaMedia ?: 0.0
                    )

                    lista.add(
                        PrestadorDisplay(
                            uid = uid,
                            user = user,
                            prestador = prestadorCorrigido,
                            servicos = servicos,
                            dataCadastro = dataCadastro,
                            ultimoAcesso = ultimoAcesso
                        )
                    )

                    if (lista.size == snapPrestadores.childrenCount.toInt()) {

                        val top3 = lista
                            .sortedByDescending { it.prestador.notaMedia ?: 0.0 }
                            .take(3)

                        prestadorAdapter.atualizarLista(top3)
                    }
                }
            }
        }
    }

    // ================================
    // CARREGA PRESTADORES COMPLETOS
    // ================================

    private fun carregarPrestadoresCompletos(ordemUIDs: List<String>) {
        val db = FirebaseDatabase.getInstance().reference

        val resultado = mutableListOf<PrestadorDisplay>()
        var carregados = 0

        ordemUIDs.forEach { uid ->

            val refPrestador = db.child("prestadores").child(uid)
            val refUser = db.child("usuarios").child(uid)

            refPrestador.get().addOnSuccessListener { prestadorSnap ->

                val dataCadastro = prestadorSnap.child("data-cadastro")
                    .getValue(String::class.java)
                val ultimoAcesso = prestadorSnap.child("ultimo-acesso")
                    .getValue(String::class.java)

                refUser.get().addOnSuccessListener { userSnap ->

                    val prestadorInfo =
                        prestadorSnap.getValue(Prestador::class.java) ?: Prestador()

                    val servicosMap = prestadorSnap.child("servicos_oferecidos")
                        .children
                        .associate { servico ->
                            servico.key!! to servico.value.toString()
                        }

                    val user = userSnap.getValue(User::class.java) ?: User()

                    resultado.add(
                        PrestadorDisplay(
                            uid = uid,
                            user = user,
                            prestador = prestadorInfo,
                            servicos = servicosMap,
                            dataCadastro = dataCadastro ?: "",
                            ultimoAcesso = ultimoAcesso ?: ""
                        )
                    )

                }.addOnCompleteListener {
                    carregados++

                    if (carregados == ordemUIDs.size) {
                        val ordenado = resultado.sortedBy { pd ->
                            ordemUIDs.indexOf(pd.uid)
                        }
                        prestadorAdapter.atualizarLista(ordenado)
                    }
                }
            }
        }
    }

    private fun configurarMenuLateral() {
        val btnMenu = binding.topBar.root.findViewById<ImageView>(R.id.ic_menu)
        val drawerLayout = binding.root.findViewById<DrawerLayout>(R.id.drawerLayout)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
