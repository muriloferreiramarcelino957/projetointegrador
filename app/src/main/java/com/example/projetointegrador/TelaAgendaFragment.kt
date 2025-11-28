package com.example.projetointegrador

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.projetointegrador.databinding.TelaDeAgendaBinding
import com.example.projetointegrador.model.Agendamento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TelaAgendaFragment : Fragment() {

    private var _binding: TelaDeAgendaBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val args: TelaAgendaFragmentArgs by navArgs()
    private lateinit var prestadorUid: String

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedServiceId: Int? = null
    private var selectedServiceName: String? = null
    private var selectedServicePrice: Int? = null

    private var horariosAtuais: List<String> = emptyList()

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
        prestadorUid = args.prestadorUid

        // estado inicial
        binding.dropHorarios.isEnabled = false
        binding.btnConfirm.isEnabled = false

        carregarDadosPrestador()
        setupListeners()
    }

    // -------------------------------------------------------------------------
    // CARREGAR DADOS DO PRESTADOR
    // -------------------------------------------------------------------------

    private fun carregarDadosPrestador() {
        val ref = database.child("prestadores").child(prestadorUid)

        ref.get().addOnSuccessListener { snap ->

            if (!snap.exists()) {
                Toast.makeText(requireContext(), "Prestador não encontrado.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            carregarNomeDoUsuario()

            val servicosNode = snap.child("servicos-oferecidos")
            val servicosList = servicosNode.children.toList()

            if (servicosList.isEmpty()) {
                binding.tvEstadoDia.text = "Este prestador ainda não cadastrou serviços."
                return@addOnSuccessListener
            }

            carregarServicosComDescricoes(servicosList)

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar prestador.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarNomeDoUsuario() {
        FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .child(prestadorUid)
            .get()
            .addOnSuccessListener {
                binding.txtNome.text = it.child("nomeUsuario").getValue(String::class.java) ?: "Prestador"
                binding.txtRating.text = "★ 5" // placeholder por enquanto
            }
    }

    // -------------------------------------------------------------------------
    // SERVIÇOS (NOME + PREÇO)
    // -------------------------------------------------------------------------

    private fun carregarServicosComDescricoes(listaServicos: List<DataSnapshot>) {
        val refTipos = database.child("tipos_de_servico")
        val serviceButtons = listOf(binding.btnService1, binding.btnService2, binding.btnService3)

        // limpa texto inicial
        serviceButtons.forEach { it.text = "" }

        for (i in listaServicos.indices.take(3)) {
            val snap = listaServicos[i]
            val serviceKey = snap.key?.toIntOrNull() ?: continue
            val servicePrice = snap.getValue(Int::class.java) ?: continue

            refTipos.child(serviceKey.toString()).get()
                .addOnSuccessListener { tipoSnap ->
                    val nomeServico = tipoSnap.child("dscr_servico").value?.toString() ?: "Serviço"

                    val btn = serviceButtons[i]
                    btn.text = "$nomeServico\nR$ $servicePrice"

                    btn.setOnClickListener {
                        resetServiceButtons(serviceButtons)

                        btn.setBackgroundColor(Color.parseColor("#5A0275"))
                        btn.setTextColor(Color.WHITE)

                        selectedServiceId = serviceKey
                        selectedServiceName = nomeServico
                        selectedServicePrice = servicePrice

                        if (selectedDate != null) {
                            gerarHorariosDoDia()
                        } else {
                            mostrarEstado("Selecione uma data no calendário para ver os horários.")
                        }

                        atualizarBotaoConfirmar()
                    }
                }
        }
    }

    private fun resetServiceButtons(buttons: List<TextView>) {
        buttons.forEach {
            it.setBackgroundResource(R.drawable.border_calendar)
            it.setTextColor(Color.parseColor("#5A0275"))
        }
    }

    // -------------------------------------------------------------------------
    // GERAÇÃO AUTOMÁTICA DE HORÁRIOS + ESTADO DO DIA
    // -------------------------------------------------------------------------

    private fun gerarHorariosDoDia() {
        if (selectedDate == null || selectedServiceId == null) return

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate!!)!!
        val cal = Calendar.getInstance().apply { time = date }

        val diaSemana = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "segunda"
            Calendar.TUESDAY -> "terça"
            Calendar.WEDNESDAY -> "quarta"
            Calendar.THURSDAY -> "quinta"
            Calendar.FRIDAY -> "sexta"
            Calendar.SATURDAY -> "sábado"
            else -> "domingo"
        }

        val ref = database.child("prestadores").child(prestadorUid)
            .child("disponibilidade").child(diaSemana)

        ref.get().addOnSuccessListener { snap ->
            val inicio = snap.child("inicio").value?.toString()
            val fim = snap.child("fim").value?.toString()

            if (inicio == null || fim == null) {
                // 🔒 dia sem turno
                horariosAtuais = emptyList()
                atualizarDropdownSemHorarios()
                mostrarEstado("Sem disponibilidade neste dia.", isError = true)
            } else {
                val lista = gerarListaHorarios(inicio, fim)
                horariosAtuais = lista
                atualizarDropdownComHorarios(lista)
                mostrarEstado("Selecione um horário disponível.")
            }
            atualizarBotaoConfirmar()
        }
    }

    private fun gerarListaHorarios(inicio: String, fim: String): List<String> {
        val lista = mutableListOf<String>()

        var horaAtual = inicio
        while (horaAtual < fim) {
            lista.add(horaAtual)

            val (h, m) = horaAtual.split(":").map { it.toInt() }
            horaAtual = String.format("%02d:%02d", h + 1, m)
        }

        return lista
    }

    private fun atualizarDropdownComHorarios(lista: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            lista
        )
        binding.dropHorarios.setAdapter(adapter)
        binding.dropHorarios.setText("", false)
        binding.dropHorarios.isEnabled = true
        binding.tilHorario.hint = "Selecione um horário"

        selectedTime = null

        binding.dropHorarios.setOnItemClickListener { _, _, position, _ ->
            selectedTime = horariosAtuais.getOrNull(position)
            atualizarBotaoConfirmar()
        }
    }

    private fun atualizarDropdownSemHorarios() {
        binding.tilHorario.hint = "Sem horários para este dia"   // <- CORRETO
        binding.dropHorarios.setAdapter(null)
        binding.dropHorarios.setText("", false)
        binding.dropHorarios.clearFocus()
        binding.dropHorarios.isEnabled = false
        selectedTime = null
    }


    private fun mostrarEstado(msg: String, isError: Boolean = false) {
        binding.tvEstadoDia.text = msg
        binding.tvEstadoDia.setTextColor(
            if (isError) Color.parseColor("#B00020")
            else Color.DKGRAY
        )
    }

    // -------------------------------------------------------------------------
    // LISTENERS GERAIS
    // -------------------------------------------------------------------------

    private fun setupListeners() {

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"

            if (selectedServiceId != null) {
                gerarHorariosDoDia()
            } else {
                mostrarEstado("Selecione um tipo de serviço para ver os horários.")
                atualizarDropdownSemHorarios()
            }

            atualizarBotaoConfirmar()
        }

        binding.btnArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConfirm.setOnClickListener {
            confirmarAgendamento()
        }
    }

    private fun atualizarBotaoConfirmar() {
        binding.btnConfirm.isEnabled =
            selectedDate != null &&
                    selectedTime != null &&
                    selectedServiceId != null
    }

    // -------------------------------------------------------------------------
    // CONFIRMAÇÃO E SALVAMENTO
    // -------------------------------------------------------------------------

    private fun confirmarAgendamento() {
        val mensagem = """
            Confirme seu agendamento:
            
            📅 Data: $selectedDate
            ⏰ Horário: $selectedTime
            🧰 Serviço: $selectedServiceName
            💲 Preço: R$ $selectedServicePrice
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Agendamento")
            .setMessage(mensagem)
            .setPositiveButton("Confirmar") { _, _ -> salvarNoFirebase() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarNoFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.child("prestacoes")
        val id = ref.push().key ?: return

        val ag = Agendamento(
            data = selectedDate!!,
            hora = selectedTime!!,
            tipoServico = selectedServiceName!!,
            prestador = prestadorUid,
            usuarioId = uid,
            status = "aguardando_confirmacao"
        )

        ref.child(id).setValue(ag)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Agendamento confirmado!", Toast.LENGTH_SHORT).show()

                val action = TelaAgendaFragmentDirections
                    .actionTelaAgendaFragmentToTelaAgenda2Fragment(
                        selectedDate!!,
                        selectedTime!!,
                        selectedServiceName!!
                    )
                findNavController().navigate(action)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
