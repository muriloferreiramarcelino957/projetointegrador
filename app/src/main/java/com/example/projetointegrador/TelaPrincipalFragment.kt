package com.projetointegrador.app.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.databinding.FragmentTelaPrincipalBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.example.projetointegrador.registro.cadastro.InfoPrestador
import com.example.projetointegrador.registro.cadastro.Prestador
import com.example.projetointegrador.registro.cadastro.User
import com.google.firebase.database.DataSnapshot
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
            // ✅ IGUALZINHO O SEU (clique volta a funcionar)
            val action = FragmentTelaPrincipalDirections
                .actionFragmentTelaPrincipalToTelaPerfilFragment(prestadorDisplay.uid)
            findNavController().navigate(action)
        }

        binding.rvPrestadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrestadores.adapter = prestadorAdapter
    }

    // ================================
    // CARREGA PRESTADORES (TOP 3)
    // ================================

    private fun carregarPrestadores() {
        val db = FirebaseDatabase.getInstance().reference

        db.child("prestadores").get().addOnSuccessListener { snapPrestadores ->

            val lista = mutableListOf<PrestadorDisplay>()
            val total = snapPrestadores.childrenCount.toInt()
            var carregados = 0

            snapPrestadores.children.forEach { prestadorSnap ->

                val uid = prestadorSnap.key ?: run {
                    carregados++
                    return@forEach
                }

                db.child("usuarios").child(uid).get()
                    .addOnSuccessListener { userSnap ->

                        // Logs úteis caso algum dado esteja estranho
                        Log.e("RAW_PRESTADOR", "UID: $uid prestador→ ${prestadorSnap.value}")
                        Log.e("RAW_USER", "UID: $uid user→ ${userSnap.value}")

                        // ✅ User seguro (não quebra em Long→String)
                        val user = userFromSnapshot(userSnap)

                        // ✅ data_cadastro / ultimo_acesso como String (se vier Long/Date, vira String)
                        val dataCadastro = prestadorSnap.child("data_cadastro").value.asString()
                        val ultimoAcesso = prestadorSnap.child("ultimo_acesso").value.asString()

                        // ✅ Prestador seguro (não quebra em mapa/tipos)
                        // Se sua notaMedia fica dentro de info_prestador ou no nó raiz, a função tenta os dois.
                        val prestadorInfo = prestadorFromSnapshot(prestadorSnap)

                        // -------- SERVIÇOS ----------
                        val servicos = prestadorSnap.child("servicos_oferecidos")
                            .children
                            .associate { it.key.orEmpty() to it.value.asString() }

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
                    }
                    .addOnFailureListener { e ->
                        Log.e("FIREBASE_ERROR", "Erro ao ler usuarios/$uid", e)
                    }
                    .addOnCompleteListener {
                        carregados++

                        // ✅ Só atualiza quando todos terminaram (igual seu antigo)
                        if (carregados >= total) {
                            val top3 = lista
                                .sortedByDescending { it.prestador.notaMedia ?: 0.0 }
                                .take(3)

                            prestadorAdapter.atualizarLista(top3)
                        }
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("FIREBASE_ERROR", "Erro ao ler prestadores", e)
        }
    }

    // ================================
    // LEITURA SEGURA (SEM CRASH)
    // ================================

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

    /**
     * Tenta ler notaMedia de onde ela estiver:
     * - raiz do prestador: prestadores/{uid}/notaMedia
     * - ou dentro de info_prestador: prestadores/{uid}/info_prestador/notaMedia
     *
     * E também traz info_prestador com campos que você tiver.
     */
    private fun prestadorFromSnapshot(prestadorSnap: DataSnapshot): Prestador {
        val raw = prestadorSnap.value as? Map<*, *> ?: emptyMap<Any, Any?>()

        val infoRaw = (raw["info_prestador"] as? Map<*, *>) ?: emptyMap<Any, Any?>()

        val notaRaiz = raw["notaMedia"].asDouble()
        val notaInfo = infoRaw["notaMedia"].asDouble()

        val info = InfoPrestador(
            descricao = infoRaw["descricao"].asString(),
            nivel_cadastro = infoRaw["nivel_cadastro"].asString(),
            quantidade_de_servicos = infoRaw["quantidade_de_servicos"].asLong()
        )

        // Mantém seus campos snake_case também
        return Prestador(
            notaMedia = notaRaiz ?: notaInfo ?: 0.0,
            data_cadastro = raw["data_cadastro"].asString(),
            ultimo_acesso = raw["ultimo_acesso"].asString(),
            info_prestador = info,
            disponibilidade = raw["disponibilidade"] as? Map<String, Any?>,
            servicos_oferecidos = raw["servicos_oferecidos"] as? Map<String, Any?>
        )
    }

    // Helpers
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
