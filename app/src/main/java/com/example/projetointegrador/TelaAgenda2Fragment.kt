package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeAgenda2Binding
import com.google.firebase.database.*

class TelaAgenda2Fragment : Fragment() {

    private var _binding: TelaDeAgenda2Binding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgenda2Binding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference

        // 🔹 Recebe dados da tela anterior
        val args = arguments
        val userId = args?.getString("userId") ?: ""
        val agendamentoId = args?.getString("agendamentoId") ?: ""

        if (userId.isNotEmpty() && agendamentoId.isNotEmpty()) {
            carregarAgendamento(userId, agendamentoId)
        }

        // 🔹 Botão de voltar
        binding.btnArrowBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    private fun carregarAgendamento(userId: String, agendamentoId: String) {
        // 🔹 Busca os dados do Firebase
        database.child("agendamentos").child(userId).child(agendamentoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val tipoServico = snapshot.child("tipoServico").getValue(String::class.java) ?: "Serviço"
                        val data = snapshot.child("data").getValue(String::class.java) ?: ""
                        val hora = snapshot.child("hora").getValue(String::class.java) ?: ""
                        val local = snapshot.child("local").getValue(String::class.java) ?: ""
                        val status = snapshot.child("status").getValue(String::class.java) ?: "Aguardando confirmação"
                        val contratanteNome = snapshot.child("contratante/nome").getValue(String::class.java) ?: ""
                        val contratanteAvatar = snapshot.child("contratante/avatarUrl").getValue(String::class.java) ?: ""
                        val statusOnline = snapshot.child("contratante/statusOnline").getValue(String::class.java) ?: "offline"
                        val rating = snapshot.child("contratante/rating").getValue(String::class.java) ?: "★ 0"
                        val txtDesde = snapshot.child("contratante/desde").getValue(String::class.java) ?: ""
                        val txtLocal = snapshot.child("contratante/local").getValue(String::class.java) ?: ""

                        // 🔹 Preenche o layout
                        binding.eventType.text = tipoServico
                        binding.eventDateTime.text = "$data • $hora"
                        binding.eventLocation.text = local
                        binding.eventStatus.text = "Status: $status"

                        // 🔹 Clique no card → abrir TelaAgenda3 com Bundle
                        binding.cardServicoSelecionado.setOnClickListener {
                            val bundle = Bundle().apply {
                                putString("userId", userId)
                                putString("agendamentoId", agendamentoId)
                                putString("tipoServico", tipoServico)
                                putString("dataHora", "$data • $hora")
                                putString("local", local)
                                putString("nomeContratante", contratanteNome)
                                putString("avatarUrl", contratanteAvatar)
                                putString("statusOnline", statusOnline)
                                putString("rating", rating)
                                putString("txtDesde", txtDesde)
                                putString("txtLocal", txtLocal)
                            }
                            findNavController().navigate(R.id.action_telaAgenda2Fragment_to_telaAgenda3Fragment, bundle)
                        }

                    } else {
                        Toast.makeText(requireContext(), "Agendamento não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Erro ao carregar agendamento", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
