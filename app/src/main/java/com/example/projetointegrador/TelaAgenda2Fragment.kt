package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.adapters.AgendamentoAdapter
import com.example.projetointegrador.databinding.TelaDeAgenda2Binding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale

class TelaAgenda2Fragment : Fragment() {

    private var _binding: TelaDeAgenda2Binding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    data class Agendamento(
        val id: String = "",
        val data: String = "",
        val hora: String = "",
        val tipoServico: String = "",
        val statusCode: String = "",
        val local: String = "",
        val valor: Int = 0
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgenda2Binding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TopNavigationBarHelper.setupNavigationBar(binding.root, this)

        binding.recyclerAgenda.layoutManager = LinearLayoutManager(requireContext())

        carregarTodosAgendamentosDoUsuario()
    }

    private fun carregarTodosAgendamentosDoUsuario() {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.child("prestacoes")

        val lista = mutableListOf<Agendamento>()
        var pendentes = 2

        fun finalizarConsulta() {
            pendentes--

            if (pendentes > 0) return

            if (lista.isEmpty()) {
                binding.txtSemAgendamentos.visibility = View.VISIBLE
                binding.recyclerAgenda.visibility = View.GONE
                return
            }

            binding.txtSemAgendamentos.visibility = View.GONE
            binding.recyclerAgenda.visibility = View.VISIBLE

            val listaOrdenada = lista.sortedBy { ag ->
                juntarDataHora(ag.data, ag.hora)
            }

            binding.recyclerAgenda.adapter =
                AgendamentoAdapter(listaOrdenada) { ag ->
                    val action =
                        TelaAgenda2FragmentDirections
                            .actionTelaAgenda2FragmentToTelaAgenda3Fragment(ag.id)

                    findNavController().navigate(action)
                }

        }

        ref.orderByChild("usuario_id").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        parseAgendamentoSeguro(child)?.let { lista.add(it) }
                    }
                    finalizarConsulta()
                }

                override fun onCancelled(error: DatabaseError) {
                    finalizarConsulta()
                }
            })

        ref.orderByChild("prestador").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        parseAgendamentoSeguro(child)?.let { lista.add(it) }
                    }
                    finalizarConsulta()
                }

                override fun onCancelled(error: DatabaseError) {
                    finalizarConsulta()
                }
            })
    }

    private fun parseAgendamentoSeguro(s: DataSnapshot): Agendamento? {
        val id = s.key ?: return null

        val dataRaw = s.child("data").value?.toString() ?: return null
        val hora = s.child("hora").value?.toString() ?: return null
        val tipo = s.child("tipo_servico").value?.toString() ?: "Serviço"
        val status = s.child("status").value?.toString() ?: "aguardando_confirmacao"
        val local = s.child("local").value?.toString() ?: "Local não informado"
        val valor = s.child("valor").value?.toString()?.toIntOrNull() ?: 0

        val dataBR = converterDataParaBR(dataRaw)

        return Agendamento(
            id = id,
            data = dataBR,
            hora = hora,
            tipoServico = tipo,
            statusCode = status,
            local = local,
            valor = valor
        )
    }

    private fun converterDataParaBR(data: String): String {
        val p = data.split("/")
        return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else data
    }

    private fun juntarDataHora(dataBR: String, hora: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.parse("$dataBR $hora")?.time ?: Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
