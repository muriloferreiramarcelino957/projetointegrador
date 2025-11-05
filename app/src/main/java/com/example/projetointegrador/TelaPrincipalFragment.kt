package com.projetointegrador.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var rvPrestadores: RecyclerView


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
    
    private fun setupNavigationBar() {
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
    }

    private fun setupRecyclerView() {
        rvPrestadores = binding.rvPrestadores
        prestadorAdapter = PrestadorAdapter()

        rvPrestadores.apply {
            adapter = prestadorAdapter
            layoutManager = LinearLayoutManager(this@FragmentTelaPrincipal.requireContext())
        }
    }

    private fun buscarPrestadores() {
        val db = FirebaseDatabase.getInstance().reference

        db.child("prestadores")
            .get()
            .addOnSuccessListener { prestadoresSnapshot ->
                if (!prestadoresSnapshot.exists()) {
                    prestadorAdapter.atualizarLista(emptyList())
                    return@addOnSuccessListener
                }

                val prestadores = mutableListOf<PrestadorDisplay>()
                val totalPrestadores = prestadoresSnapshot.childrenCount.toInt()
                var processedCount = 0

                if (totalPrestadores == 0) {
                    prestadorAdapter.atualizarLista(emptyList())
                    return@addOnSuccessListener
                }

                for (prestadorSnapshot in prestadoresSnapshot.children) {
                    val uid = prestadorSnapshot.key ?: continue
                    
                    db.child("usuarios").child(uid)
                        .get()
                        .addOnSuccessListener { userSnapshot ->
                            val user = userSnapshot.getValue(User::class.java)
                            val prestadorInfo = prestadorSnapshot.child("info_prestador").getValue(Prestador::class.java)
                            val tiposServico = prestadorSnapshot.child("info_serviços").getValue(TiposServico::class.java)
                            
                            if (user != null && prestadorInfo != null) {
                                prestadores.add(PrestadorDisplay(user, prestadorInfo, tiposServico))
                            }
                            
                            synchronized(prestadores) {
                                processedCount++
                                if (processedCount == totalPrestadores) {
                                    updateTop3(prestadores)
                                }
                            }
                        }
                        .addOnFailureListener {
                            synchronized(prestadores) {
                                processedCount++
                                if (processedCount == totalPrestadores) {
                                    updateTop3(prestadores)
                                }
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao carregar prestadores: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateTop3(prestadores: List<PrestadorDisplay>) {
        val top3Prestadores = prestadores
            .sortedByDescending { it.prestador.notaMedia }
            .take(3)
        prestadorAdapter.atualizarLista(top3Prestadores)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
