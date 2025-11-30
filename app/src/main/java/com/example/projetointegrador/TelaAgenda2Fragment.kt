package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeAgenda2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TelaAgenda2Fragment : Fragment() {

    private var _binding: TelaDeAgenda2Binding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    data class Agendamento(
        val id: String,
        val data: String,
        val hora: String,
        val tipoServico: String,
        val statusCode: String,
        val local: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgenda2Binding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        carregarProximoAgendamentoDoUsuarioLogado()
    }

    // =====================================================================
    // BUSCA O PRÓXIMO AGENDAMENTO QUE ENVOLVE O USUÁRIO LOGADO
    // - como cliente: usuario_id == uid
    // - como prestador: prestador == uid
    // =====================================================================
    private fun carregarProximoAgendamentoDoUsuarioLogado() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        val prestacoesRef = database.child("prestacoes")

        // Vamos buscar 2 listas e unir (Realtime Database não tem OR query)
        val resultados = LinkedHashMap<String, Agendamento>()
        var pendentes = 2

        fun finalizarSePronto() {
            pendentes--
            if (pendentes > 0) return

            if (resultados.isEmpty()) {
                mostrarSemAgendamento()
                return
            }

            val proximo = escolherAgendamentoMaisProximo(resultados.values.toList())
            if (proximo == null) {
                // se não houver futuro, pega o primeiro (ou o mais recente, se quiser)
                val fallback = resultados.values.first()
                mostrarAgendamento(fallback)
            } else {
                mostrarAgendamento(proximo)
            }
        }

        // 1) COMO CLIENTE (chave REAL do seu banco: usuario_id)
        prestacoesRef.orderByChild("usuario_id")
            .equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        val ag = parseAgendamento(child) ?: return@forEach
                        resultados[ag.id] = ag
                    }
                    finalizarSePronto()
                }

                override fun onCancelled(error: DatabaseError) {
                    finalizarSePronto()
                }
            })

        // 2) COMO PRESTADOR (chave REAL do seu banco: prestador)
        // ❗ Se você quiser APENAS os agendamentos onde ele é cliente,
        // apague este bloco inteiro e troque pendentes=2 para pendentes=1 acima.
        prestacoesRef.orderByChild("prestador")
            .equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        val ag = parseAgendamento(child) ?: return@forEach
                        resultados[ag.id] = ag
                    }
                    finalizarSePronto()
                }

                override fun onCancelled(error: DatabaseError) {
                    finalizarSePronto()
                }
            })
    }

    private fun parseAgendamento(snap: DataSnapshot): Agendamento? {
        val id = snap.key ?: return null

        // Chaves do seu banco (snake_case) + fallback para camelCase
        val data = snap.child("data").getStringOrEmpty()
        val hora = snap.child("hora").getStringOrEmpty()

        val tipoServico =
            snap.child("tipo_servico").getStringOrEmpty()
                .ifBlank { snap.child("tipoServico").getStringOrEmpty() }
                .ifBlank { "Serviço" }

        val statusCode =
            snap.child("status").getStringOrEmpty()
                .ifBlank { "aguardando_confirmacao" }

        val local =
            snap.child("local").getStringOrEmpty()
                .ifBlank { "Local informado no momento da contratação" }

        if (data.isBlank() || hora.isBlank()) return null

        return Agendamento(
            id = id,
            data = data,
            hora = hora,
            tipoServico = tipoServico,
            statusCode = statusCode,
            local = local
        )
    }

    private fun escolherAgendamentoMaisProximo(lista: List<Agendamento>): Agendamento? {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val agora = Date()

        var melhor: Agendamento? = null
        var menorDiff: Long? = null

        lista.forEach { ag ->
            val dataHora = try {
                sdf.parse("${ag.data} ${ag.hora}")
            } catch (_: Exception) {
                null
            } ?: return@forEach

            val diff = dataHora.time - agora.time
            if (diff >= 0 && (menorDiff == null || diff < menorDiff!!)) {
                menorDiff = diff
                melhor = ag
            }
        }
        return melhor
    }

    private fun mostrarAgendamento(ag: Agendamento) {
        val statusTexto = traduzirStatus(ag.statusCode)

        binding.tvDataCalendario.text = "Próximo agendamento"
        binding.eventDateTime.text = "${ag.data} • ${ag.hora}"
        binding.eventType.text = ag.tipoServico
        binding.eventLocation.text = ag.local
        binding.eventStatus.text = "Status: $statusTexto"

        binding.cardServicoSelecionado.isClickable = true
        binding.cardServicoSelecionado.setOnClickListener {
            val action =
                TelaAgenda2FragmentDirections
                    .actionTelaAgenda2FragmentToTelaAgenda3Fragment(ag.id)
            findNavController().navigate(action)
        }
    }

    private fun mostrarSemAgendamento() {
        binding.tvDataCalendario.text = "Nenhum agendamento encontrado"
        binding.eventDateTime.text = "Data e hora"
        binding.eventType.text = "Tipo de Serviço"
        binding.eventLocation.text = "Localização"
        binding.eventStatus.text = "Status: —"
        binding.cardServicoSelecionado.isClickable = false
        binding.cardServicoSelecionado.setOnClickListener(null)
    }

    private fun traduzirStatus(code: String): String {
        return when (code) {
            "aguardando_confirmacao" -> "aguardando confirmação"
            "agendado" -> "agendado"
            "em_execucao" -> "em execução"
            "finalizado" -> "finalizado"
            "cancelado" -> "cancelado"
            "recusado" -> "recusado"
            else -> code.replace("_", " ")
        }
    }

    private fun DataSnapshot.getStringOrEmpty(): String {
        val v = this.value ?: return ""
        return when (v) {
            is String -> v
            is Number -> v.toString()
            is Boolean -> v.toString()
            else -> v.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
