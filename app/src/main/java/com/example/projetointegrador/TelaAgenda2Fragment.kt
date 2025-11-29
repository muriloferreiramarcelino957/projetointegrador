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

        carregarProximoAgendamento()
    }

    // =====================================================================
    // BUSCA O PRÓXIMO AGENDAMENTO DO USUÁRIO LOGADO
    // =====================================================================
    private fun carregarProximoAgendamento() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("prestacoes")
            .orderByChild("usuarioId")
            .equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.hasChildren()) {
                        // Não há nada agendado
                        binding.tvDataCalendario.text = "Nenhum agendamento encontrado"
                        binding.eventDateTime.text = "Data e hora"
                        binding.eventType.text = "Tipo de Serviço"
                        binding.eventLocation.text = "Localização"
                        binding.eventStatus.text = "Status: —"
                        binding.cardServicoSelecionado.isClickable = false
                        return
                    }

                    // Escolher o agendamento mais próximo da data atual
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val agora = Date()

                    var melhorSnap: DataSnapshot? = null
                    var menorDiff: Long? = null

                    snapshot.children.forEach { child ->
                        val data = child.child("data").getValue(String::class.java) ?: return@forEach
                        val hora = child.child("hora").getValue(String::class.java) ?: return@forEach

                        val dataHora = try {
                            sdf.parse("$data $hora")
                        } catch (e: Exception) {
                            null
                        } ?: return@forEach

                        val diff = dataHora.time - agora.time

                        // Se ainda não passou e é o mais próximo
                        if (diff >= 0 && (menorDiff == null || diff < menorDiff!!)) {
                            menorDiff = diff
                            melhorSnap = child
                        }
                    }

                    // Se não achou nada "futuro", pega o primeiro mesmo
                    if (melhorSnap == null) {
                        melhorSnap = snapshot.children.first()
                    }

                    val agSnap = melhorSnap!!
                    val agendamentoId = agSnap.key ?: return

                    val data = agSnap.child("data").getValue(String::class.java) ?: ""
                    val hora = agSnap.child("hora").getValue(String::class.java) ?: ""
                    val tipoServico = agSnap.child("tipoServico").getValue(String::class.java) ?: "Serviço"
                    val statusCode = agSnap.child("status").getValue(String::class.java) ?: "aguardando_confirmacao"

                    // texto humanizado do status
                    val statusTexto = traduzirStatus(statusCode)

                    // Preenche os textos
                    binding.tvDataCalendario.text = "Próximo agendamento"
                    binding.eventDateTime.text = "$data • $hora"
                    binding.eventType.text = tipoServico
                    binding.eventLocation.text = "Local informado no momento da contratação"
                    binding.eventStatus.text = "Status: $statusTexto"

                    // Clique no card → detalhes
                    binding.cardServicoSelecionado.setOnClickListener {
                        val action =
                            TelaAgenda2FragmentDirections
                                .actionTelaAgenda2FragmentToTelaAgenda3Fragment(agendamentoId)
                        findNavController().navigate(action)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao carregar agendamentos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // =====================================================================
    // MAPEIA O STATUS "code" → TEXTO BONITINHO
    // =====================================================================
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
