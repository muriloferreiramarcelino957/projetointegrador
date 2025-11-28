package com.projetointegrador.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.databinding.FragmentTelaPrincipalBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.example.projetointegrador.registro.cadastro.Prestador
import com.example.projetointegrador.registro.cadastro.User
import com.google.firebase.database.FirebaseDatabase

class FragmentTelaPrincipal : Fragment() {

    private var _binding: FragmentTelaPrincipalBinding? = null
    private val binding get() = _binding!!

    private lateinit var prestadorAdapter: PrestadorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaPrincipalBinding.inflate(inflater, container, false)
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
            // 👉 AGORA USA O UID DO PrestadorDisplay
            val action = FragmentTelaPrincipalDirections
                .actionFragmentTelaPrincipalToTelaPerfilFragment(prestadorDisplay.uid)
            findNavController().navigate(action)
        }

        binding.rvPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrestadores.adapter = prestadorAdapter
    }

    // ================================
    // CARREGA PRESTADORES (simples)
    // ================================

    private fun carregarPrestadores() {
        val db = FirebaseDatabase.getInstance().reference

        db.child("prestadores").get().addOnSuccessListener { snapPrestadores ->

            val lista = mutableListOf<PrestadorDisplay>()

            snapPrestadores.children.forEach { prestadorSnap ->

                val uid = prestadorSnap.key ?: return@forEach

                db.child("usuarios").child(uid).get().addOnSuccessListener { userSnap ->

                    val user = userSnap.getValue(User::class.java) ?: User()

                    // -----------------------------
                    // CAMPOS COM PADRÃO DEFINITIVO
                    // -----------------------------
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

                    // -------- SERVIÇOS ----------
                    val servicos = prestadorSnap.child("servicos_oferecidos")
                        .children.associate {
                            it.key!! to it.value.toString()
                        }

                    // Garantir que nota não seja null
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

                    // Já carregou tudo → mostrar top 3
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
    // CARREGA PRESTADORES EM ORDEM (optional)
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
                            uid = uid,                         // 👈 UID aqui também
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
                        // Ordena pela ordem de UIDs passada
                        val ordenado = resultado.sortedBy { pd ->
                            ordemUIDs.indexOf(pd.uid)        // 👈 USA pd.uid
                        }
                        prestadorAdapter.atualizarLista(ordenado)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
