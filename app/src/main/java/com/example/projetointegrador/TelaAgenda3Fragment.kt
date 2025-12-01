package com.example.projetointegrador

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.projetointegrador.databinding.TelaDeAgenda3Binding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TelaAgenda3Fragment : Fragment() {

    private var _binding: TelaDeAgenda3Binding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private val args by navArgs<TelaAgenda3FragmentArgs>()

    private lateinit var agendamentoId: String

    // ids da prestação (preenchidos ao carregar a prestação)
    private var usuarioIdPrestacao: String = ""
    private var prestadorIdPrestacao: String = ""

    private val uidLogado by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeAgenda3Binding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)

        agendamentoId = args.agendamentoId
        binding.btnArrowBack.setOnClickListener { findNavController().navigateUp() }

        configurarMenuLateral()
        carregarDadosAgendamento()

        // ----------------------------------------
        // BOTÃO CONCLUIR SERVIÇO
        // ----------------------------------------
        binding.btnConcluir.setOnClickListener {

            // segurança: só o CLIENTE pode concluir e gerar avaliação
            if (uidLogado != usuarioIdPrestacao) {
                toast("Somente o cliente pode concluir e avaliar este serviço.")
                return@setOnClickListener
            }

            if (usuarioIdPrestacao.isBlank() || prestadorIdPrestacao.isBlank()) {
                toast("Dados ainda não carregados. Tente novamente.")
                return@setOnClickListener
            }

            binding.btnConcluir.isEnabled = false

            criarNotificacaoDeAvaliacao(
                onSuccess = {
                    // vai direto para tela de notificações
                    irParaTelaDeNotificacoes()
                },
                onError = {
                    binding.btnConcluir.isEnabled = true
                    toast("Erro ao registrar notificação de avaliação.")
                }
            )
        }
    }

    // =======================================================
    // CRIA NOTIFICAÇÃO DE AVALIAÇÃO PARA O CLIENTE
    // =======================================================
    private fun criarNotificacaoDeAvaliacao(
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        // nó: notificacoes/{usuarioIdPrestacao}/{id_notificacao}
        val notifRef = database
            .child("notificacoes")
            .child(usuarioIdPrestacao)
            .push()

        val notifId = notifRef.key
        if (notifId == null) {
            onError()
            return
        }

        val dados = mapOf(
            "id" to notifId,
            "tipo" to "avaliacao_servico",
            "titulo" to "Avalie o serviço realizado",
            "descricao" to "Conte como foi a experiência com o prestador.",
            "chipTipo" to "Avaliação",
            "agendamentoId" to agendamentoId,
            "prestadorId" to prestadorIdPrestacao,
            "lido" to false,
            "timestamp" to ServerValue.TIMESTAMP
        )

        notifRef.setValue(dados)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }

    private fun irParaTelaDeNotificacoes() {
        // tela de notificações está dentro do mesmo graph (navigation)
        findNavController().navigate(R.id.telaNotificacaoFragment)
    }

    // =======================================================
    // BUSCA DADOS DO AGENDAMENTO
    // =======================================================
    private fun carregarDadosAgendamento() {

        val ref = database.child("prestacoes").child(agendamentoId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (!snapshot.exists()) {
                    toast("Agendamento não encontrado.")
                    return
                }

                val tipoServico = snapshot.child("tipo_servico").value?.toString() ?: ""
                val dataIso = snapshot.child("data").value?.toString() ?: ""
                val hora = snapshot.child("hora").value?.toString() ?: ""
                val usuarioId = snapshot.child("usuario_id").value?.toString() ?: ""
                val prestadorId = snapshot.child("prestador").value?.toString() ?: ""

                usuarioIdPrestacao = usuarioId
                prestadorIdPrestacao = prestadorId

                binding.tvTipoServico.text = tipoServico
                binding.tvDataHora.text = "${converterData(dataIso)} - $hora"

                val statusRaw = snapshot.child("status").value?.toString()?.trim() ?: ""

                when (statusRaw) {

                    "aguardando_confirmacao" -> {
                        binding.tvStatusServico.text = " • aguardando confirmação"
                        binding.tvStatusServico.setTextColor(Color.parseColor("#F39C12"))
                        binding.btnConcluir.visibility = View.GONE
                    }

                    "agendado" -> {
                        binding.tvStatusServico.text = " • agendado"
                        binding.tvStatusServico.setTextColor(Color.parseColor("#4CAF50"))

                        // botão só aparece para o CLIENTE
                        binding.btnConcluir.visibility =
                            if (uidLogado == usuarioIdPrestacao) View.VISIBLE else View.GONE
                    }

                    else -> {
                        binding.tvStatusServico.text = " • status desconhecido"
                        binding.tvStatusServico.setTextColor(Color.GRAY)
                        binding.btnConcluir.visibility = View.GONE
                    }
                }

                decidirVisao(usuarioId, prestadorId)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // =======================================================
    // LÓGICA PRINCIPAL → MOSTRAR DADOS DO CLIENTE OU PRESTADOR
    // =======================================================
    private fun decidirVisao(usuarioId: String, prestadorId: String) {
        when (uidLogado) {
            prestadorId -> carregarDadosContratante(usuarioId) // prestador logado
            else        -> carregarDadosPrestador(prestadorId) // cliente logado
        }
    }

    // =======================================================
    // EXIBIR DADOS DO CONTRATANTE (quando PRESTADOR está logado)
    // =======================================================
    private fun carregarDadosContratante(uid: String) {

        val usuarioRef = database.child("usuarios").child(uid)

        usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {

                val nome = snap.child("nome").value?.toString() ?: "Usuário"
                val telefone = snap.child("telefone").value?.toString() ?: "Não informado"
                val email = snap.child("email").value?.toString() ?: "Não informado"

                val logradouro = snap.child("logradouro").value?.toString() ?: ""
                val numero = snap.child("numero").value?.toString() ?: ""
                val bairro = snap.child("bairro").value?.toString() ?: ""
                val cidade = snap.child("cidade").value?.toString() ?: ""
                val estado = snap.child("estado").value?.toString() ?: ""

                binding.txtNome.text = nome

                // OCULTAR "usuário desde xxx"
                binding.txtDesde.visibility = View.GONE

                val linha1 = "$logradouro, $numero".trim().trim(',')
                val linha2 = "$bairro - $cidade, $estado".trim().trim(',')

                binding.tvEnderecoCompleto.text =
                    if (linha1.isBlank() && linha2.isBlank()) "Endereço não informado"
                    else "$linha1\n$linha2"

                binding.txtTelefone.text = "Telefone: ${formatarTelefone(telefone)}"
                binding.txtEmail.text = "E-mail: $email"
                binding.txtRating.text = "★ 5.0"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // =======================================================
    // EXIBIR DADOS DO PRESTADOR (quando CLIENTE está logado)
    // =======================================================
    private fun carregarDadosPrestador(uid: String) {

        val usuarioRef = database.child("usuarios").child(uid)
        val prestadorRef = database.child("prestadores").child(uid)

        usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {

                val nome = snap.child("nome").value?.toString() ?: "Prestador"
                val telefone = snap.child("telefone").value?.toString() ?: "Não informado"
                val email = snap.child("email").value?.toString() ?: "Não informado"

                val logradouro = snap.child("logradouro").value?.toString() ?: ""
                val numero = snap.child("numero").value?.toString() ?: ""
                val bairro = snap.child("bairro").value?.toString() ?: ""
                val cidade = snap.child("cidade").value?.toString() ?: ""
                val estado = snap.child("estado").value?.toString() ?: ""

                binding.txtNome.text = nome
                binding.txtDesde.text = "Prestador na AllService"

                val linha1 = "$logradouro, $numero".trim().trim(',')
                val linha2 = "$bairro - $cidade, $estado".trim().trim(',')

                binding.tvEnderecoCompleto.text =
                    if (linha1.isBlank() && linha2.isBlank()) "Endereço não informado"
                    else "$linha1\n$linha2"

                binding.txtTelefone.text = "Telefone: ${formatarTelefone(telefone)}"
                binding.txtEmail.text = "E-mail: $email"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        prestadorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {

                val nota = snap.child("info_prestador/notaMedia")
                    .getValue(Double::class.java) ?: 0.0

                binding.txtRating.text = "★ ${String.format("%.1f", nota)}"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // =======================================================
    // FORMATADORES
    // =======================================================
    private fun converterData(dataIso: String): String {
        return try {
            val input = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
            output.format(input.parse(dataIso)!!)
        } catch (e: Exception) {
            dataIso
        }
    }

    private fun formatarTelefone(tel: String): String {
        val digitos = tel.replace(Regex("\\D"), "")
        return if (digitos.length >= 10) {
            "(${digitos.substring(0, 2)}) ${digitos.substring(2, 7)}-${digitos.substring(7)}"
        } else tel
    }

    private fun configurarMenuLateral() {
        val btnMenu = binding.topBar.root.findViewById<ImageView>(R.id.ic_menu)
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout)
        btnMenu.setOnClickListener { drawer.openDrawer(GravityCompat.START) }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}