package com.example.projetointegrador

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeAgendaBinding
import com.example.projetointegrador.model.Agendamento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TelaAgendaFragment : Fragment() {

    private var _binding: TelaDeAgendaBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedService: String? = null

    // UID fixo do prestador (pode ser dinamizado depois)
    private val prestadorUid = "6kn4YRqQXiSE3CxFUy3Xt2GbKJb2"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        carregarDadosDoPrestador()
        initListeners()
    }

    /** 🔹 Carrega dados do prestador e serviços do Firebase */
    private fun carregarDadosDoPrestador() {
        val prestadorRef = database.child("prestadores").child(prestadorUid)
        prestadorRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(requireContext(), "Prestador não encontrado.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Nome do prestador
            val nome = snapshot.child("info_prestador/nomeUsuario").value?.toString() ?: "Prestador"

            // Serviços
            val tipo1 = snapshot.child("info_serviços/tipoServico1").value?.toString() ?: ""
            val tipo2 = snapshot.child("info_serviços/tipoServico2").value?.toString() ?: ""
            val tipo3 = snapshot.child("info_serviços/tipoServico3").value?.toString() ?: ""

            // Horários do serviço 1
            val h1 = snapshot.child("info_serviços/horarioServico1_1").value?.toString() ?: "--:--"
            val h2 = snapshot.child("info_serviços/horarioServico1_2").value?.toString() ?: "--:--"
            val h3 = snapshot.child("info_serviços/horarioServico1_3").value?.toString() ?: "--:--"

            // Atualiza UI
            binding.txtNome.text = nome
            binding.servicosTags.text = listOf(tipo1, tipo2, tipo3).filter { it.isNotEmpty() }.joinToString(" | ")

            binding.btnFaxina.text = tipo1
            binding.btnHidraulica.text = tipo2
            binding.btnEletrica.text = tipo3

            binding.btnTime1.text = h1
            binding.btnTime2.text = h2
            binding.btnTime3.text = h3

            selectedService = tipo1
            selectedTime = null
            binding.tvSelectedTime.text = "--:--*"

            setupServiceButtons(snapshot)
            setupTimeButtons()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar dados do Firebase.", Toast.LENGTH_SHORT).show()
        }
    }

    /** 🔹 Inicializa listeners de calendário, voltar e confirmar */
    private fun initListeners() {
        binding.cardCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            Toast.makeText(requireContext(), "Data selecionada: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        binding.btnArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConfirm.setOnClickListener {
            if (selectedDate != null && selectedTime != null && selectedService != null) {
                mostrarPopupConfirmacao()
            } else {
                Toast.makeText(requireContext(), "Selecione data, horário e serviço.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 🔹 Pop-up de confirmação do agendamento */
    private fun mostrarPopupConfirmacao() {
        val mensagem = """
            Confirme seu agendamento:
            
            📅 Data: $selectedDate
            ⏰ Horário: $selectedTime
            🧰 Serviço: $selectedService
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Agendamento")
            .setMessage(mensagem)
            .setPositiveButton("Confirmar") { _, _ -> salvarAgendamentoNoFirebase() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** 🔹 Configura os botões de serviço e troca horários conforme serviço selecionado */
    private fun setupServiceButtons(snapshot: DataSnapshot) {
        val serviceButtons = listOf(binding.btnFaxina, binding.btnHidraulica, binding.btnEletrica)
        serviceButtons.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                resetServiceButtons(serviceButtons)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                selectedService = btn.text.toString()
                val i = index + 1
                binding.btnTime1.text = snapshot.child("info_serviços/horarioServico${i}_1").value?.toString() ?: "--:--"
                binding.btnTime2.text = snapshot.child("info_serviços/horarioServico${i}_2").value?.toString() ?: "--:--"
                binding.btnTime3.text = snapshot.child("info_serviços/horarioServico${i}_3").value?.toString() ?: "--:--"

                selectedTime = null
                binding.tvSelectedTime.text = "--:--*"
            }
        }
    }

    private fun resetServiceButtons(buttons: List<TextView>) {
        buttons.forEach {
            it.setBackgroundResource(R.drawable.border_calendar)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        }
    }

    /** 🔹 Configura os botões de horários */
    private fun setupTimeButtons() {
        val timeButtons = listOf(binding.btnTime1, binding.btnTime2, binding.btnTime3)
        timeButtons.forEach { btn ->
            btn.setOnClickListener {
                resetTimeButtons(timeButtons)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                selectedTime = btn.text.toString()
                binding.tvSelectedTime.text = "$selectedTime*"
            }
        }
    }

    private fun resetTimeButtons(buttons: List<TextView>) {
        buttons.forEach {
            it.setBackgroundResource(R.drawable.border_calendar)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        }
    }

    /** 🔹 Salva o agendamento no Firebase */
    private fun salvarAgendamentoNoFirebase() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val agendamentoRef = database.child("agendamentos")
        val agendamento = Agendamento(
            data = selectedDate!!,
            hora = selectedTime!!,
            tipoServico = selectedService!!,
            prestador = prestadorUid,
            usuarioId = uid,
            status = "aguardando_confirmacao"
        )

        val id = agendamentoRef.push().key
        if (id == null) {
            Toast.makeText(requireContext(), "Erro ao gerar ID do agendamento.", Toast.LENGTH_SHORT).show()
            return
        }

        agendamentoRef.child(id).setValue(agendamento)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Agendamento confirmado!", Toast.LENGTH_SHORT).show()
                val bundle = Bundle().apply {
                    putString("data", selectedDate)
                    putString("hora", selectedTime)
                    putString("servico", selectedService)
                }
                findNavController().navigate(R.id.action_telaAgendaFragment_to_telaAgenda2Fragment, bundle)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao salvar agendamento: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
