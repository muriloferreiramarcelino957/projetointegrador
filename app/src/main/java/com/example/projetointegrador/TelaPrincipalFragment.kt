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
        buscarPrestadores()
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
    private fun buscarPrestadores() {
        val db = FirebaseDatabase.getInstance().reference.child("prestadores")

        db.get().addOnSuccessListener { prestadoresSnapshot ->
            if (!prestadoresSnapshot.exists()) {
                prestadorAdapter.atualizarLista(emptyList())
                return@addOnSuccessListener
            }

            val prestadores = mutableListOf<PrestadorDisplay>()
            val totalPrestadores = prestadoresSnapshot.childrenCount.toInt()
            if (totalPrestadores == 0) {
                prestadorAdapter.atualizarLista(emptyList())
                return@addOnSuccessListener
            }

            // Processa cada prestador
            prestadoresSnapshot.children.forEach { prestadorSnapshot ->
                val uid = prestadorSnapshot.key ?: return@forEach

                FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)
                    .get().addOnSuccessListener { userSnapshot ->
                        val user = userSnapshot.getValue(User::class.java) ?: User()
                        val prestadorInfo = prestadorSnapshot.child("info_prestador")
                            .getValue(Prestador::class.java) ?: Prestador()
                        val tiposServico = prestadorSnapshot.child("info_serviços")
                            .getValue(TiposServico::class.java) ?: TiposServico()

                        prestadores.add(PrestadorDisplay(user, prestadorInfo, tiposServico))

                        // Atualiza RecyclerView quando todos os prestadores forem processados
                        if (prestadores.size == totalPrestadores) {
                            atualizarTopPrestadores(prestadores)
                        }
                    }
                    .addOnFailureListener {
                        // Mesmo que dê erro, continua contando
                        if (prestadores.size == totalPrestadores) {
                            atualizarTopPrestadores(prestadores)
                        }
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                requireContext(),
                "Erro ao carregar prestadores: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
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
