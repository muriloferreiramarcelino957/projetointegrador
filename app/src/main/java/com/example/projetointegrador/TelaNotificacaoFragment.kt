package com.example.projetointegrador

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.adapters.NotificacaoAdapter
import com.example.projetointegrador.databinding.TelaDeNotificacoesBinding
import com.example.projetointegrador.model.Notificacao
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TelaNotificacaoFragment : Fragment() {

    private var _binding: TelaDeNotificacoesBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseReference
    private lateinit var adapter: NotificacaoAdapter
    private val uid by lazy { FirebaseAuth.getInstance().uid ?: "" }
    private var listener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeNotificacoesBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance().reference
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        TopNavigationBarHelper.setupNavigationBar(binding.root, this)

        adapter = NotificacaoAdapter(
            onAceitar = { aceitar(it) },
            onRecusar = { recusar(it) },
            onMarcarLido = { marcarComoLido(it) },
            onAvaliar = { id, nota -> avaliar(id, nota) }
        )

        binding.recyclerNotificacoes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNotificacoes.adapter = adapter

        binding.btnMarcarComoLidas.setOnClickListener { marcarTodasLidas() }
        binding.btnVoltar.setOnClickListener { requireActivity().onBackPressed() }

        carregarNotificacoes()
    }

    private fun carregarNotificacoes() {

        val ref = db.child("notificacoes").child(uid)

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (_binding == null) return

                val lista = snapshot.children.mapNotNull { snap ->
                    snap.getValue(Notificacao::class.java)?.apply {
                        id = snap.key ?: ""
                    }
                }

                adapter.atualizar(lista)
                binding.layoutVazio.visibility =
                    if (lista.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener!!)
    }

    private fun marcarComoLido(id: String) {
        db.child("notificacoes").child(uid).child(id).removeValue()
    }

    private fun marcarTodasLidas() {
        db.child("notificacoes").child(uid).removeValue()
    }

    // ============================================================
    // ACEITAR SOLICITAÇÃO
    // ============================================================
    private fun aceitar(idNotif: String) {

        db.child("notificacoes").child(uid).child(idNotif)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snap: DataSnapshot) {

                    val agendamentoId = snap.child("agendamento_id").value?.toString() ?: ""
                    val clienteId = snap.child("usuario_id").value?.toString() ?: ""

                    if (agendamentoId.isBlank() || clienteId.isBlank()) return

                    // Marca prestação como agendada
                    db.child("prestacoes").child(agendamentoId)
                        .child("status").setValue("agendado")

                    criarNotificacaoParaCliente(clienteId, agendamentoId)

                    db.child("notificacoes").child(uid).child(idNotif).removeValue()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun criarNotificacaoParaCliente(clienteId: String, agendamentoId: String) {

        val ref = db.child("notificacoes").child(clienteId).push()
        val id = ref.key ?: return

        val agora = System.currentTimeMillis()
        val data = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(agora))
        val hora = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(agora))

        val dados = mapOf(
            "id" to id,
            "tipo" to "confirmado",
            "mensagem" to "Seu agendamento foi aceito pelo prestador!",
            "agendamento_id" to agendamentoId,
            "prestador_id" to uid,
            "data" to data,
            "hora" to hora,
            "data_hora" to agora,
            "lido" to false
        )

        ref.setValue(dados)
    }

    // ============================================================
    // RECUSAR SOLICITAÇÃO
    // ============================================================
    private fun recusar(idNotif: String) {

        db.child("notificacoes").child(uid).child(idNotif)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snap: DataSnapshot) {

                    val agendamentoId = snap.child("agendamento_id").value?.toString() ?: ""
                    val clienteId = snap.child("usuario_id").value?.toString() ?: ""

                    if (agendamentoId.isBlank() || clienteId.isBlank()) return

                    // Apaga prestação
                    db.child("prestacoes").child(agendamentoId).removeValue()

                    criarNotificacaoDeRecusa(clienteId, agendamentoId)

                    db.child("notificacoes").child(uid).child(idNotif).removeValue()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun criarNotificacaoDeRecusa(clienteId: String, agendamentoId: String) {

        val ref = db.child("notificacoes").child(clienteId).push()
        val id = ref.key ?: return

        val dados = mapOf(
            "id" to id,
            "tipo" to "recusado",
            "mensagem" to "O prestador recusou sua solicitação.",
            "agendamento_id" to agendamentoId,
            "usuario_id" to clienteId,
            "prestador_id" to uid,
            "lido" to false
        )

        ref.setValue(dados)
    }

    // ============================================================
    // AVALIAÇÃO
    // ============================================================
    private fun avaliar(id: String, nota: Int) {

        db.child("notificacoes").child(uid).child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {

                    val prestadorId = snap.child("prestador_id").value?.toString()

                    println("DEBUG prestador_id recebido = $prestadorId")

                    if (prestadorId.isNullOrBlank()) {
                        println("ERRO: prestador_id ausente! Avaliação não pode ser salva.")
                        return
                    }

                    salvarAvaliacao(prestadorId, nota)

                    // Agora sim remove
                    db.child("notificacoes").child(uid).child(id).removeValue()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }



    private fun salvarAvaliacao(prestadorId: String, nota: Int) {

        val ref = db.child("prestadores").child(prestadorId).child("info_prestador")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snap: DataSnapshot) {

                val atualMedia = snap.child("notaMedia").getValue(Double::class.java) ?: 0.0
                val atualQtd = snap.child("quantidade_de_servicos").getValue(Int::class.java) ?: 0

                val novaQtd = atualQtd + 1
                val novaMedia = ((atualMedia * atualQtd) + nota.toDouble()) / novaQtd

                val updates = mapOf(
                    "quantidade_de_servicos" to novaQtd,
                    "notaMedia" to novaMedia // SEMPRE Double
                )

                ref.updateChildren(updates)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun arredondarMeiaNota(v: Double) = (Math.round(v * 2) / 2.0)

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.let { db.child("notificacoes").child(uid).removeEventListener(it) }
        listener = null
        _binding = null
    }
}
