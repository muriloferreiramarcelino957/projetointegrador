package com.projetointegrador.app.ui

// ... imports ...
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.PrestadorDisplay // Esta classe precisa ser atualizada
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
            val action = FragmentTelaPrincipalDirections
                .actionFragmentTelaPrincipalToTelaPerfilFragment(prestadorDisplay.prestador.uid)
            findNavController().navigate(action)
        }

        binding.rvPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrestadores.adapter = prestadorAdapter
    }

    // ================================
    // BUSCA TOP 3 PRESTADORES (ou todos se houver menos de 3)
    // ================================

    private fun carregarPrestadores() {
        val ref = FirebaseDatabase.getInstance().reference.child("prestadores")

        // 1. Ordena pela notaMedia (crescente) e pega os 3 últimos, que são os melhores.
        ref.orderByChild("info_prestador/notaMedia").limitToLast(3)
            .get()
            .addOnSuccessListener { snap ->

                if (!snap.exists()) {
                    prestadorAdapter.atualizarLista(emptyList())
                    return@addOnSuccessListener
                }

                // 2. Mapeia e inverte a ordem para que o melhor (maior nota) venha primeiro.
                val ranking = snap.children.mapNotNull { node ->
                    val uid = node.key ?: return@mapNotNull null
                    val nota = node.child("info_prestador/notaMedia")
                        .getValue(Double::class.java) ?: 0.0
                    uid to nota
                }.sortedByDescending { it.second } // Ordem: Nota mais alta primeiro

                carregarPrestadoresCompletos(ranking.map { it.first })
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao carregar", Toast.LENGTH_SHORT).show()
            }
    }

    // ================================
    // CARREGA TODOS OS DADOS DO PRESTADOR
    // ================================

    private fun carregarPrestadoresCompletos(ordemUIDs: List<String>) {
        val db = FirebaseDatabase.getInstance().reference

        val resultado = mutableListOf<PrestadorDisplay>()
        var carregados = 0

        ordemUIDs.forEach { uid ->

            val refPrestador = db.child("prestadores").child(uid)
            val refUser = db.child("usuarios").child(uid)

            refPrestador.get().addOnSuccessListener { prestadorSnap ->

                // === BUSCA DATA DE CADASTRO E ULTIMO ACESSO DO NÓ PRINCIPAL ===
                val dataCadastro = prestadorSnap.child("data_cadastro")
                    .getValue(String::class.java)
                val ultimoAcesso = prestadorSnap.child("ultimo_acesso")
                    .getValue(String::class.java)

                refUser.get().addOnSuccessListener { userSnap ->

                    // ==== PRESTADOR (Mantido inalterado) ====
                    val prestadorInfo =
                        prestadorSnap.child("info_prestador").getValue(Prestador::class.java)
                            ?: Prestador().also { it.uid = uid }
                    prestadorInfo.uid = uid

                    // ==== SERVICOS OFERECIDOS ====
                    val servicosMap = prestadorSnap.child("servicos_oferecidos")
                        .children
                        .associate { servico ->
                            servico.key!! to servico.value.toString()
                        }

                    // ==== USER ====
                    val user = userSnap.getValue(User::class.java) ?: User()

                    resultado.add(
                        PrestadorDisplay(
                            user = user,
                            prestador = prestadorInfo,
                            servicos = servicosMap,
                            // === NOVIDADE: PASSANDO DADOS EXTRAS AQUI ===
                            dataCadastro = dataCadastro, // Você deve atualizar a classe PrestadorDisplay
                            ultimoAcesso = ultimoAcesso // Você deve atualizar a classe PrestadorDisplay
                        )
                    )

                }.addOnCompleteListener {
                    carregados++

                    if (carregados == ordemUIDs.size) {

                        // Garante que a lista final seja exibida na ordem de nota decrescente
                        val ordenado = resultado.sortedBy {
                            ordemUIDs.indexOf(it.prestador.uid)
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