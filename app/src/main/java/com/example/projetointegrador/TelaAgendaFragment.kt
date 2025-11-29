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

    private val args by navArgs<TelaAgendaFragmentArgs>()
    private lateinit var prestadorUid: String

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedServiceId: Int? = null
    private var selectedServiceName: String? = null
    private var selectedServicePrice: Int? = null

    private var horariosAtuais = emptyList<String>()

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

        // 🚫 Impede agendamento consigo mesmo
        if (prestadorUid == auth.currentUser?.uid) {
            Toast.makeText(requireContext(), "Você não pode agendar consigo mesmo.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        binding.dropHorarios.isEnabled = false
        binding.tilHorario.hint = "Selecione um serviço"

        binding.btnConfirm.isEnabled = false

        carregarDadosPrestador()
        setupListeners()
    }

    // =====================================================================
    // CARREGA DADOS DO PRESTADOR
    // =====================================================================

    private fun carregarDadosPrestador() {
        val ref = database.child("prestadores").child(prestadorUid)

        ref.get().addOnSuccessListener { snap ->

            if (!snap.exists()) {
                Toast.makeText(requireContext(), "Prestador não encontrado.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            carregarNomeDoUsuario()

            val servicosNode = snap.child("servicos_oferecidos")
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
        database.child("usuarios").child(prestadorUid)
            .get()
            .addOnSuccessListener { snap ->
                binding.txtNome.text = snap.child("nome").getValue(String::class.java) ?: "Prestador"
                binding.txtRating.text = "★ 0,0"
            }
    }

    // =====================================================================
    // CARREGA SERVIÇOS + PREÇO
    // =====================================================================

    private fun carregarServicosComDescricoes(listaServicos: List<DataSnapshot>) {
        val refTipos = database.child("tipos_de_servico")
        val buttons = listOf(binding.btnService1, binding.btnService2, binding.btnService3)

        buttons.forEach { it.text = "" }

        for (i in listaServicos.indices.take(3)) {

            val snap = listaServicos[i]
            val idServico = snap.key?.toIntOrNull() ?: continue
            val preco = snap.getValue(String::class.java)?.toIntOrNull() ?: continue

            refTipos.child(idServico.toString()).get()
                .addOnSuccessListener { tipoSnap ->

                    val nomeServico =
                        tipoSnap.child("dscr_servico").getValue(String::class.java)
                            ?: "Serviço"

                    val btn = buttons[i]
                    btn.text = "$nomeServico\nR$ $preco"

                    btn.setOnClickListener {
                        resetarBotoes(buttons)

                        btn.setBackgroundColor(Color.parseColor("#5A0275"))
                        btn.setTextColor(Color.WHITE)

                        selectedServiceId = idServico
                        selectedServiceName = nomeServico
                        selectedServicePrice = preco

                        // Caixa de horário volta a exigir seleção de data
                        binding.tilHorario.hint = "Selecione uma data"

                        if (selectedDate != null) gerarHorariosDoDia()
                        else mostrarEstado("Selecione uma data para ver horários.")

                        atualizarBotaoConfirmar()
                    }
                }
        }
    }

    private fun resetarBotoes(buttons: List<TextView>) {
        buttons.forEach {
            it.setBackgroundResource(R.drawable.border_calendar)
            it.setTextColor(Color.parseColor("#5A0275"))
        }
    }

    // =====================================================================
    // DISPONIBILIDADE + HORÁRIOS
    // =====================================================================

    private fun gerarHorariosDoDia() {
        if (selectedDate == null || selectedServiceId == null) return

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate!!)!!
        val cal = Calendar.getInstance()
        cal.time = date

        val diaSemana = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "segunda"
            Calendar.TUESDAY -> "terça"
            Calendar.WEDNESDAY -> "quarta"
            Calendar.THURSDAY -> "quinta"
            Calendar.FRIDAY -> "sexta"
            Calendar.SATURDAY -> "sábado"
            else -> "domingo"
        }

        val ref = database.child("prestadores")
            .child(prestadorUid)
            .child("disponibilidade")
            .child(diaSemana)

        ref.get().addOnSuccessListener { snap ->

            val inicio = snap.child("inicio").getValue(String::class.java)
            val fim = snap.child("fim").getValue(String::class.java)

            if (inicio == null || fim == null) {

                horariosAtuais = emptyList()
                atualizarDropdownSemHorarios()

                mostrarEstado("Sem disponibilidade neste dia.", isError = true)

                // Caixa indica necessidade de outro dia
                binding.tilHorario.hint = "Sem horários disponíveis"

            } else {

                horariosAtuais = gerarListaHorarios(inicio, fim)

                atualizarDropdownComHorarios(horariosAtuais)
                mostrarEstado("Selecione um horário disponível.")

                binding.tilHorario.hint = "Selecione um horário"
            }

            atualizarBotaoConfirmar()
        }
    }

    private fun gerarListaHorarios(inicio: String, fim: String): List<String> {
        val horarios = mutableListOf<String>()
        var horaAtual = inicio

        while (horaAtual < fim) {
            horarios.add(horaAtual)

            val (h, m) = horaAtual.split(":").map { it.toInt() }
            horaAtual = String.format("%02d:%02d", h + 1, m)
        }

        return horarios
    }

    private fun atualizarDropdownComHorarios(lista: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, lista)

        binding.dropHorarios.setAdapter(adapter)
        binding.dropHorarios.setText("", false)
        binding.dropHorarios.isEnabled = true

        selectedTime = null

        binding.dropHorarios.setOnItemClickListener { _, _, position, _ ->
            selectedTime = lista[position]
            atualizarBotaoConfirmar()
        }
    }

    private fun atualizarDropdownSemHorarios() {
        binding.dropHorarios.setText("", false)
        binding.dropHorarios.isEnabled = false
        selectedTime = null
    }

    private fun mostrarEstado(msg: String, isError: Boolean = false) {
        binding.tvEstadoDia.text = msg
        binding.tvEstadoDia.setTextColor(
            if (isError) Color.parseColor("#B00020") else Color.DKGRAY
        )
    }

    // =====================================================================
    // LISTENERS
    // =====================================================================

    private fun setupListeners() {

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->

            selectedDate = "%02d/%02d/%04d".format(day, month + 1, year)

            if (selectedServiceId != null) {
                gerarHorariosDoDia()
            } else {
                mostrarEstado("Selecione um serviço para ver horários.")
                binding.tilHorario.hint = "Selecione um serviço"
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

    // =====================================================================
    // SALVAR NO FIREBASE
    // =====================================================================

    private fun confirmarAgendamento() {

        val msg = """
            Confirme seu agendamento:
            
            📅 Data: $selectedDate
            ⏰ Horário: $selectedTime
            🧰 Serviço: $selectedServiceName
            💵 Preço: R$ $selectedServicePrice
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar agendamento")
            .setMessage(msg)
            .setPositiveButton("Confirmar") { _, _ -> salvarNoFirebase() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarNoFirebase() {
        val userUid = auth.currentUser?.uid ?: return
        val ref = database.child("prestacoes")
        val pushId = ref.push().key ?: return

        // 🔹 1. Criar registro de agendamento
        val dados = mapOf(
            "data" to selectedDate!!,
            "hora" to selectedTime!!,
            "tipo_servico" to selectedServiceName!!,
            "prestador" to prestadorUid,
            "usuario_id" to userUid,
            "status" to "aguardando_confirmacao",
            "local" to "Local do usuário futuramente",
            "valor" to selectedServicePrice!!
        )

        ref.child(pushId).setValue(dados)
            .addOnSuccessListener {

                // 🔹 NOTIFICAÇÃO PARA O PRESTADOR
                val notifRef = database.child("notificacoes")
                    .child(prestadorUid)
                    .push()

                val notifData = mapOf(
                    "tipo" to "solicitacao_servico",
                    "agendamento_id" to pushId,
                    "mensagem" to "Novo pedido de $selectedServiceName às $selectedTime",
                    "data_hora" to System.currentTimeMillis(),
                    "lido" to false
                )

                notifRef.setValue(notifData)

                // 🔹 NAVEGAÇÃO CORRIGIDA
                findNavController().navigate(
                    TelaAgendaFragmentDirections
                        .actionTelaAgendaFragmentToTelaAgenda2Fragment(
                            selectedDate!!,
                            selectedTime!!,
                            selectedServiceName!!,
                            pushId
                        )
                )
            }
    }


        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
