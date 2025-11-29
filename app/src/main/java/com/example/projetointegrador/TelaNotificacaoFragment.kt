package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.adapters.NotificacaoAdapter
import com.example.projetointegrador.databinding.TelaDeNotificacoesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TelaNotificacaoFragment : Fragment() {

    private var _binding: TelaDeNotificacoesBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeNotificacoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uid = FirebaseAuth.getInstance().uid!!
        database = FirebaseDatabase.getInstance().reference

        binding.recyclerNotificacoes.layoutManager = LinearLayoutManager(requireContext())

        carregarNotificacoes()
        initListeners()
    }

    private fun initListeners() {
        binding.btnVoltar.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnMarcarComoLidas.setOnClickListener {
            database.child("notificacoes").child(uid)
                .get().addOnSuccessListener { snap ->
                    snap.children.forEach { it.ref.child("lido").setValue(true) }
                }
            Toast.makeText(requireContext(), "Notificações marcadas como lidas!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarNotificacoes() {
        database.child("notificacoes").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val lista = snapshot.children.toList()

                    if (lista.isEmpty()) {
                        binding.layoutVazio.visibility = View.VISIBLE
                        binding.recyclerNotificacoes.visibility = View.GONE
                        return
                    }

                    binding.layoutVazio.visibility = View.GONE
                    binding.recyclerNotificacoes.visibility = View.VISIBLE

                    binding.recyclerNotificacoes.adapter = NotificacaoAdapter(
                        lista,
                        onAceitar = { notifId -> aceitarSolicitacao(notifId) },
                        onRecusar = { notifId -> recusarSolicitacao(notifId) }
                    )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun aceitarSolicitacao(notifId: String) {
        val notifRef = database.child("notificacoes").child(uid).child(notifId)

        notifRef.get().addOnSuccessListener { snap ->
            val agendamentoId = snap.child("agendamento_id").value.toString()

            database.child("prestacoes").child(agendamentoId)
                .child("status")
                .setValue("agendado")

            notifRef.removeValue()
            Toast.makeText(requireContext(), "Serviço aceito!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recusarSolicitacao(notifId: String) {
        val notifRef = database.child("notificacoes").child(uid).child(notifId)

        notifRef.get().addOnSuccessListener { snap ->

            val agendamentoId = snap.child("agendamento_id").value.toString()

            val prestRef = database.child("prestacoes").child(agendamentoId)

            prestRef.child("status").setValue("recusado")

            prestRef.get().addOnSuccessListener { agSnap ->
                val usuarioId = agSnap.child("usuario_id").value.toString()

                database.child("notificacoes").child(usuarioId)
                    .push()
                    .setValue(
                        mapOf(
                            "tipo" to "recusado",
                            "mensagem" to "Seu pedido de serviço foi recusado",
                            "data_hora" to System.currentTimeMillis(),
                            "agendamento_id" to agendamentoId,
                            "lido" to false
                        )
                    )
            }

            notifRef.removeValue()

            Toast.makeText(requireContext(), "Solicitação recusada!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
