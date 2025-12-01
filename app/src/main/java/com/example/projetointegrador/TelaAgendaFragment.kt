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
import com.example.projetointegrador.navigation.TopNavigationBarHelper
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

    // dados do usuário solicitante
    private var enderecoUsuario: String = ""
    private var nomeDoUsuario: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TopNavigationBarHelper.setupNavigationBar(binding.topBar.root, this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        prestadorUid = args.prestadorUid

        if (prestadorUid == auth.currentUser?.uid) {
            Toast.makeText(requireContext(), "Você não pode agendar consigo mesmo.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        carregarEnderecoDoUsuario()
        carregarDadosPrestador()
        setupListeners()

        binding.dropHorarios.isEnabled = false
        binding.btnConfirm.isEnabled = false
        binding.tilHorario.hint = "Selecione um serviço"
    }

    // =====================================================================
    // CARREGA NOME + ENDEREÇO DO USUÁRIO
    // =====================================================================
    private fun carregarEnderecoDoUsuario() {
        val uid = auth.currentUser?.uid ?: return

        database.child("usuarios").child(uid)
            .get()
            .addOnSuccessListener { snap ->

                nomeDoUsuario = snap.child("nome").getValue(String::class.java) ?: "Cliente"

                val logradouro = snap.child("logradouro").getValue(String::class.java) ?: ""
                val numero = snap.child("numero").getValue(String::class.java) ?: ""
                val bairro = snap.child("bairro").getValue(String::class.java) ?: ""
                val cidade = snap.child("cidade").getValue(String::class.java) ?: ""
                val estado = snap.child("estado").getValue(String::class.java) ?: ""

                enderecoUsuario = "$logradouro, $numero - $bairro, $cidade - $estado"

                // agora podemos validar o botão
                atualizarBotaoConfirmar()
            }
    }

    // =====================================================================
    // NOME DO PRESTADOR
    // =====================================================================
    private fun carregarDadosPrestador() {
        database.child("usuarios").child(prestadorUid)
            .get()
            .addOnSuccessListener { snap ->
                binding.txtNome.text = snap.child("nome").getValue(String::class.java) ?: "Prestador"
                binding.txtRating.text = "★ 0,0"
            }

        database.child("prestadores").child(prestadorUid)
            .get()
            .addOnSuccessListener { snap ->
                carregarServicosComPreco(snap.child("servicos_oferecidos").children.toList())
            }
    }

    // =====================================================================
    // SERVIÇOS
    // =====================================================================
    private fun carregarServicosComPreco(lista: List<DataSnapshot>) {
        val refTipos = database.child("tipos_de_servico")
        val buttons = listOf(binding.btnService1, binding.btnService2, binding.btnService3)

        buttons.forEach { it.text = "" }

        for (i in lista.indices.take(3)) {
            val snap = lista[i]
            val id = snap.key?.toIntOrNull() ?: continue
            val preco = snap.getValue(String::class.java)?.toIntOrNull() ?: continue

            refTipos.child(id.toString()).get()
                .addOnSuccessListener { tipo ->
                    val nome = tipo.child("dscr_servico").getValue(String::class.java) ?: "Serviço"
                    val btn = buttons[i]

                    btn.text = "$nome\nR$ $preco"

                    btn.setOnClickListener {
                        resetarBotoes(buttons)
                        btn.setBackgroundColor(Color.parseColor("#5A0275"))
                        btn.setTextColor(Color.WHITE)

                        selectedServiceId = id
                        selectedServiceName = nome
                        selectedServicePrice = preco

                        binding.tilHorario.hint = "Selecione uma data"

                        if (selectedDate != null) gerarHorariosDoDia()

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
    // HORÁRIOS
    // =====================================================================
    private fun gerarHorariosDoDia() {
        if (selectedDate == null || selectedServiceId == null) return

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate!!)!!
        val cal = Calendar.getInstance()
        cal.time = date

        val dia = listOf(
            "domingo", "segunda", "terça", "quarta", "quinta", "sexta", "sábado"
        )[cal.get(Calendar.DAY_OF_WEEK) - 1]

        val ref = database.child("prestadores")
            .child(prestadorUid)
            .child("disponibilidade")
            .child(dia)

        ref.get().addOnSuccessListener { snap ->
            val inicio = snap.child("inicio").getValue(String::class.java)
            val fim = snap.child("fim").getValue(String::class.java)

            if (inicio == null || fim == null) {
                horariosAtuais = emptyList()
                binding.tilHorario.hint = "Sem horários disponíveis"
                binding.dropHorarios.isEnabled = false
                return@addOnSuccessListener
            }

            horariosAtuais = gerarListaHorarios(inicio, fim)
            atualizarHorarios(horariosAtuais)
        }
    }

    private fun gerarListaHorarios(inicio: String, fim: String): List<String> {
        val result = mutableListOf<String>()
        var hora = inicio

        while (hora.substring(0, 2).toInt() < fim.substring(0, 2).toInt()) {
            result.add(hora)
            val (h, m) = hora.split(":").map { it.toInt() }
            hora = "%02d:%02d".format(h + 1, m)
        }
        return result
    }

    private fun atualizarHorarios(lista: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, lista)
        binding.dropHorarios.setAdapter(adapter)
        binding.dropHorarios.setText("", false)
        binding.dropHorarios.isEnabled = true

        binding.dropHorarios.setOnItemClickListener { _, _, pos, _ ->
            selectedTime = lista[pos]
            atualizarBotaoConfirmar()
        }
    }

    // =====================================================================
    // LISTENERS
    // =====================================================================
    private fun setupListeners() {

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            selectedDate = "%02d/%02d/%04d".format(day, month + 1, year)
            if (selectedServiceId != null) gerarHorariosDoDia()
            atualizarBotaoConfirmar()
        }

        binding.btnArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConfirm.setOnClickListener {
            confirmarAgendamento()
        }
    }

    // =====================================================================
    // HABILITAÇÃO DO BOTÃO
    // =====================================================================
    private fun atualizarBotaoConfirmar() {
        binding.btnConfirm.isEnabled =
            selectedDate != null &&
                    selectedTime != null &&
                    selectedServiceId != null &&
                    nomeDoUsuario.isNotBlank() &&
                    enderecoUsuario.isNotBlank()
    }

    // =====================================================================
    // FORMATAR DATA
    // =====================================================================
    private fun formatDate(date: String): String {
        val inFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outFmt = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return outFmt.format(inFmt.parse(date)!!)
    }

    // =====================================================================
    // CONFIRMAR AGENDAMENTO
    // =====================================================================
    private fun confirmarAgendamento() {
        val msg = """
            📅 Data: $selectedDate
            ⏰ Horário: $selectedTime
            🔧 Serviço: $selectedServiceName
            📍 Local: $enderecoUsuario
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar agendamento")
            .setMessage(msg)
            .setPositiveButton("Confirmar") { _, _ -> salvarNoFirebase() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // =====================================================================
    // SALVAR AGENDAMENTO + ENVIAR NOTIFICAÇÃO
    // =====================================================================
    private fun salvarNoFirebase() {
        val userUid = auth.currentUser?.uid ?: return
        val ref = database.child("prestacoes")
        val agendamentoId = ref.push().key ?: return

        val dados = mapOf(
            "data" to formatDate(selectedDate!!),
            "hora" to selectedTime!!,
            "tipo_servico" to selectedServiceName!!,
            "prestador" to prestadorUid,
            "usuario_id" to userUid,
            "status" to "aguardando_confirmacao",
            "local" to enderecoUsuario,
            "valor" to selectedServicePrice!!
        )

        ref.child(agendamentoId).setValue(dados)
            .addOnSuccessListener {
                enviarNotificacao(agendamentoId)

                findNavController().navigate(
                    TelaAgendaFragmentDirections.actionTelaAgendaFragmentToTelaAgenda2Fragment(
                        selectedDate!!, selectedTime!!, selectedServiceName!!, agendamentoId
                    )
                )
            }
    }

    // =====================================================================
    // ENVIAR NOTIFICAÇÃO COMPLETA
    // =====================================================================
    private fun enviarNotificacao(agendamentoId: String) {
        val notifRef = database.child("notificacoes").child(prestadorUid).push()

        val notif = mapOf(
            "usuario_id" to auth.currentUser!!.uid,
            "tipo" to "solicitacao_servico",
            "agendamento_id" to agendamentoId,
            "nome_usuario" to nomeDoUsuario,
            "endereco" to enderecoUsuario,
            "servico" to selectedServiceName,
            "data" to selectedDate,
            "hora" to selectedTime,
            "valor" to selectedServicePrice,

            "mensagem" to "Novo pedido de $selectedServiceName para $selectedDate às $selectedTime",
            "data_hora" to System.currentTimeMillis(),
            "lido" to false
        )

        notifRef.setValue(notif)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
