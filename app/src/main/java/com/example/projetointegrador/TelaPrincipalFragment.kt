package com.projetointegrador.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.databinding.FragmentTelaPrincipalBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.example.projetointegrador.registro.cadastro.TiposServico
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
        buscarPrestadoresTop3()
        return binding.root
    }

    // Configura barra de navegação superior
    private fun setupNavigationBar() {
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
    }

    // Configura RecyclerView
    private fun setupRecyclerView() {
        prestadorAdapter = PrestadorAdapter()
        binding.rvPrestadores.apply {
            adapter = prestadorAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // Busca prestadores do Firebase e atualiza a lista
    private fun buscarPrestadoresTop3() {
        val refPrestadores = FirebaseDatabase.getInstance().reference.child("prestadores")


        refPrestadores.orderByChild("info_prestador/notaMedia").limitToLast(3).get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    prestadorAdapter.atualizarLista(emptyList())
                    return@addOnSuccessListener
                }

                val filhosOrdenadosDesc = snap.children.toList().sortedByDescending {
                    it.child("info_prestador/notaMedia").getValue(Double::class.java) ?: 0.0
                }

                val resultado = mutableListOf<PrestadorDisplay>()
                var processados = 0
                val total = filhosOrdenadosDesc.size

                filhosOrdenadosDesc.forEach { prestadorNode ->
                    val uidDaChave = prestadorNode.key

                    val uid = uidDaChave ?: prestadorNode
                        .child("info_prestador/uid").getValue(String::class.java)

                    if (uid.isNullOrEmpty()) {
                        processados++
                        if (processados == total) prestadorAdapter.atualizarLista(resultado)
                        return@forEach
                    }

                    // mapeia os blocos
                    val prestadorInfo = prestadorNode.child("info_prestador")
                        .getValue(Prestador::class.java) ?: Prestador()

                    val tiposServico = prestadorNode.child("info_servicos")
                        .getValue(TiposServico::class.java)

                    // busca dados do usuário
                    FirebaseDatabase.getInstance().reference
                        .child("usuarios").child(uid)
                        .get()
                        .addOnSuccessListener { userSnap ->
                            val user = userSnap.getValue(User::class.java) ?: User()
                            resultado.add(PrestadorDisplay(user, prestadorInfo, tiposServico))
                        }
                        .addOnCompleteListener {
                            processados++
                            if (processados == total) {
                                prestadorAdapter.atualizarLista(resultado)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Atualiza os top 3 prestadores por nota
    private fun atualizarTopPrestadores(prestadores: List<PrestadorDisplay>) {
        val top3Prestadores = prestadores
            .sortedByDescending { it.prestador.notaMedia ?: 0.0 }
            .take(3)
        prestadorAdapter.atualizarLista(top3Prestadores)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
