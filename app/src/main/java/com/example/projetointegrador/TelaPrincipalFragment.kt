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
import com.example.projetointegrador.registro.cadastro.InfoPrestador
import com.example.projetointegrador.registro.cadastro.Prestador
import com.example.projetointegrador.registro.cadastro.User
import com.google.firebase.database.DataSnapshot
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

        return binding.root
    }

    private fun setupRecyclerView() {
        prestadorAdapter = PrestadorAdapter { prestadorDisplay ->
            findNavController().navigate(
                R.id.telaPerfilFragment,
                bundleOf("uidPrestador" to prestadorDisplay.uid)
            )
        }

        binding.rvPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrestadores.adapter = prestadorAdapter
    }

    // ============================================================
    // CARREGAR PRESTADORES DO FIREBASE
    // ============================================================
    private fun carregarPrestadores() {
        val db = FirebaseDatabase.getInstance().reference

        db.child("prestadores").get()
            .addOnSuccessListener { snapPrestadores ->

                listaCompleta.clear()
                val total = snapPrestadores.childrenCount.toInt()
                var carregados = 0

                snapPrestadores.children.forEach { prestadorSnap ->

                    val uid = prestadorSnap.key ?: run {
                        carregados++
                        return@forEach
                    }

                    // Carrega dados do usuário
                    db.child("usuarios").child(uid).get()
                        .addOnSuccessListener { userSnap ->

                            val user = userFromSnapshot(userSnap)

                            val dataCadastro = prestadorSnap.child("data_cadastro").value.asString()
                            val ultimoAcesso = prestadorSnap.child("ultimo_acesso").value.asString()

                            val prestadorInfo = prestadorFromSnapshot(prestadorSnap)

                            val servicos = prestadorSnap.child("servicos_oferecidos")
                                .children
                                .associate { it.key.orEmpty() to it.value.asString() }

                            listaCompleta.add(
                                PrestadorDisplay(
                                    uid = uid,
                                    user = user,
                                    prestador = prestadorInfo,
                                    servicos = servicos,
                                    dataCadastro = dataCadastro,
                                    ultimoAcesso = ultimoAcesso
                                )
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e("FIREBASE_ERROR", "Erro ao ler usuarios/$uid", e)
                        }
                        .addOnCompleteListener {
                            carregados++

                            // Quando carregar todos, exibe top 3
                            if (carregados >= total) {

                                val top3 =
                                    listaCompleta.sortedByDescending {
                                        it.prestador.info_prestador?.quantidade_de_servicos ?: 0
                                    }.take(3)

                                prestadorAdapter.atualizarLista(top3)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_ERROR", "Erro ao ler prestadores", e)
            }
    }

    // ============================================================
    // CONVERSORES DE SNAPSHOT -> MODEL
    // ============================================================
    private fun userFromSnapshot(snap: DataSnapshot): User {

        val raw = snap.value as? Map<*, *> ?: emptyMap<Any, Any?>()

        return User(
            nome = raw["nome"].asString(),
            email = raw["email"].asString(),
            senha = raw["senha"].asString(),
            dataNascimento = raw["dataNascimento"].asString(),
            cpf = raw["cpf"].asString(),
            cep = raw["cep"].asString(),
            logradouro = raw["logradouro"].asString(),
            numero = raw["numero"].asString(),
            bairro = raw["bairro"].asString(),
            cidade = raw["cidade"].asString(),
            estado = raw["estado"].asString(),
            prestador = raw["prestador"].asBoolean()
        )
    }

    private fun prestadorFromSnapshot(prestadorSnap: DataSnapshot): Prestador {

        val raw = prestadorSnap.value as? Map<*, *> ?: emptyMap<Any, Any?>()
        val infoRaw = raw["info_prestador"] as? Map<*, *> ?: emptyMap<Any, Any?>()

        val info = InfoPrestador(
            descricao = infoRaw["descricao"].asString(),
            notaMedia = infoRaw["notaMedia"].asDouble() ?: 0.0,
            quantidade_de_servicos = infoRaw["quantidade_de_servicos"].asLong()?.toInt() ?: 0
        )

        return Prestador(
            nivel_cadastro = raw["nivel_cadastro"].asString(),
            ultimo_acesso = raw["ultimo_acesso"].asString(),
            data_cadastro = raw["data_cadastro"].asString(),

            // ***** IMPORTANTE ***** LISTA, NÃO MAP
            servicos_oferecidos = (raw["servicos_oferecidos"] as? List<*>)?.map { it.asString() },

            disponibilidade = raw["disponibilidade"] as? Map<String, Any?>,
            info_prestador = info
        )
    }


    // ============================================================
    // EXTENSÕES PARA LEITURA SEGURA
    // ============================================================
    private fun Any?.asString(): String = when (this) {
        null -> ""
        is String -> this
        is Number -> this.toString()
        is Boolean -> this.toString()
        else -> this.toString()
    }

    private fun Any?.asBoolean(): Boolean = when (this) {
        is Boolean -> this
        is Number -> this.toInt() != 0
        is String -> this.equals("true", true) || this == "1"
        else -> false
    }

    private fun Any?.asLong(): Long? = when (this) {
        is Number -> this.toLong()
        is String -> this.toLongOrNull()
        else -> null
    }

    private fun Any?.asDouble(): Double? = when (this) {
        is Number -> this.toDouble()
        is String -> this.toDoubleOrNull()
        else -> null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
