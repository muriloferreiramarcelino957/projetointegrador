package com.example.projetointegrador

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.databinding.FragmentTelaPrincipalBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.database.FirebaseDatabase
import com.projetointegrador.app.ui.PrestadorAdapter

class TelaPrincipalFragment : Fragment() {

    private var _binding: FragmentTelaPrincipalBinding? = null
    private val binding get() = _binding!!

    private lateinit var prestadorAdapter: PrestadorAdapter
    private val listaCompleta = mutableListOf<PrestadorDisplay>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTelaPrincipalBinding.inflate(inflater, container, false)

        TopNavigationBarHelper.setupNavigationBar(binding.root, this)

        setupRecyclerView()
        carregarPrestadores()

        binding.btnFaxina.setOnClickListener { abrirBuscaComFiltro("1") }
        binding.btnEletrico.setOnClickListener { abrirBuscaComFiltro("2") }
        binding.btnHidraulica.setOnClickListener { abrirBuscaComFiltro("3") }
        binding.btnJardinagem.setOnClickListener { abrirBuscaComFiltro("4") }

        return binding.root
    }

    private fun setupRecyclerView() {
        prestadorAdapter = PrestadorAdapter { prestador ->
            findNavController().navigate(
                R.id.telaPerfilFragment,
                bundleOf("uidPrestador" to prestador.uid)
            )
        }

        binding.rvPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrestadores.adapter = prestadorAdapter
    }

    private fun carregarPrestadores() {
        val db = FirebaseDatabase.getInstance().reference

        db.child("prestadores").get().addOnSuccessListener { snapPrestadores ->

            listaCompleta.clear()

            db.child("usuarios").get().addOnSuccessListener { snapUsuarios ->

                snapPrestadores.children.forEach { snap ->

                    val uid = snap.key ?: return@forEach
                    val userSnap = snapUsuarios.child(uid)

                    val nome = userSnap.child("nome").value?.toString() ?: ""
                    val cidade = userSnap.child("cidade").value?.toString() ?: ""

                    val info = InfoPrestador(
                        notaMedia = snap.child("info_prestador/notaMedia").getValue(Double::class.java) ?: 0.0,
                        nivel_cadastro = snap.child("info_prestador/nivel_cadastro").value?.toString() ?: "",
                        quantidade_de_servicos = snap.child("info_prestador/quantidade_de_servicos")
                            .getValue(Int::class.java) ?: 0
                    )

                    val servicosIds = snap.child("servicos_oferecidos")
                        .children.mapNotNull { it.key }

                    listaCompleta.add(
                        PrestadorDisplay(
                            uid = uid,
                            nome = nome,
                            cidade = cidade,
                            info_prestador = info,
                            servicos = servicosIds
                        )
                    )
                }

                val top3 = listaCompleta.sortedByDescending {
                    it.info_prestador.quantidade_de_servicos
                }.take(3)

                prestadorAdapter.atualizarLista(top3)
            }
        }
    }

    private fun abrirBuscaComFiltro(servicoId: String) {
        val action = TelaPrincipalFragmentDirections
            .actionFragmentTelaPrincipalToTelaBuscaFragment(servicoId)

        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
