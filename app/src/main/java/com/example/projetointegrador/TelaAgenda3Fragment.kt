package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeAgenda3Binding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*

class TelaAgenda3Fragment : Fragment() {

    private var _binding: TelaDeAgenda3Binding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgenda3Binding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        carregarDadosFirebase()
    }

    private fun initListeners() {
        // Botão de voltar
        binding.btnArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Botão de concluir
        binding.btnConcluir.setOnClickListener {
            mostrarDialogoConcluido()
        }
    }

    private fun mostrarDialogoConcluido() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sucesso, null)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .show()
    }

    private fun carregarDadosFirebase() {
        val userId = arguments?.getString("userId") ?: ""
        val agendamentoId = arguments?.getString("agendamentoId") ?: ""

        if (userId.isEmpty() || agendamentoId.isEmpty()) {
            Toast.makeText(requireContext(), "Dados do agendamento inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = database.child("agendamentos").child(userId).child(agendamentoId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "Agendamento não encontrado", Toast.LENGTH_SHORT).show()
                    return
                }

                // 🔹 Ler todos os campos do Firebase
                val tipoServico = snapshot.child("tipoServico").getValue(String::class.java) ?: ""
                val data = snapshot.child("data").getValue(String::class.java) ?: ""
                val hora = snapshot.child("hora").getValue(String::class.java) ?: ""
                val local = snapshot.child("local").getValue(String::class.java) ?: ""
                val nome = snapshot.child("nome").getValue(String::class.java) ?: ""
                val statusOnline = snapshot.child("statusOnline").getValue(String::class.java) ?: ""
                val rating = snapshot.child("rating").getValue(String::class.java) ?: ""
                val desde = snapshot.child("desde").getValue(String::class.java) ?: ""
                val txtLocal = snapshot.child("txtLocal").getValue(String::class.java) ?: ""

                // 🔹 Preencher todos os campos da TelaAgenda3
                binding.tvTipoServico.text = tipoServico
                binding.tvDataHora.text = "$data • $hora"
                binding.tvLocalizacao.text = local
                binding.txtNome.text = nome
                binding.statusOnline.text = statusOnline
                binding.txtRating.text = rating
                binding.txtDesde.text = desde
                binding.txtLocal.text = txtLocal
                binding.ratingMedia.text = "Média\n$rating"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erro ao carregar dados: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
