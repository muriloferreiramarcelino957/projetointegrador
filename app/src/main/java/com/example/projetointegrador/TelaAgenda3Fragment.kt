package com.example.projetointegrador

import android.graphics.Color
import android.os.Bundle
import android.view.*
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

    private lateinit var db: DatabaseReference
    private val args by navArgs<TelaAgenda3FragmentArgs>()

    private lateinit var agendamentoId: String
    private var usuarioIdPrestacao = ""   // cliente
    private var prestadorIdPrestacao = "" // prestador

    private val uid by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TelaDeAgenda3Binding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance().reference
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
        configurarMenuLateral()

        agendamentoId = args.agendamentoId

        binding.btnArrowBack.setOnClickListener { findNavController().navigateUp() }

        carregarPrestacao()

        // =================================================
        // BOTÃO DE CONCLUIR SERVIÇO (SOMENTE CLIENTE)
        // =================================================
        binding.btnConcluir.setOnClickListener {

            if (uid != usuarioIdPrestacao) {
                toast("Somente o cliente pode concluir este serviço.")
                return@setOnClickListener
            }

            if (usuarioIdPrestacao.isBlank() || prestadorIdPrestacao.isBlank()) {
                toast("Dados ainda não carregados.")
                return@setOnClickListener
            }

            binding.btnConcluir.isEnabled = false

            criarNotificacaoDeAvaliacao(
                onSuccess = {
                    db.child("prestacoes").child(agendamentoId).child("status").setValue("concluido")
                    toast("Serviço concluído! Avalie o prestador.")
                    irParaNotificacoes()
                },
                onError = {
                    binding.btnConcluir.isEnabled = true
                    toast("Erro ao enviar notificação.")
                }
            )
        }
    }

    // =======================================================
    // LER PRESTAÇÃO DO FIREBASE
    // =======================================================
    private fun carregarPrestacao() {

        db.child("prestacoes").child(agendamentoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snap: DataSnapshot) {

                    if (!snap.exists()) {
                        toast("Agendamento não encontrado.")
                        return
                    }

                    val tipo = snap.child("tipo_servico").value?.toString() ?: ""
                    val dataIso = snap.child("data").value?.toString() ?: ""
                    val hora = snap.child("hora").value?.toString() ?: ""

                    usuarioIdPrestacao = snap.child("usuario_id").value?.toString() ?: ""
                    prestadorIdPrestacao =
                        snap.child("prestador_id").value?.toString()
                            ?: snap.child("prestador").value?.toString()
                                    ?: ""

                    binding.tvTipoServico.text = tipo
                    binding.tvDataHora.text = "${formatarData(dataIso)} - $hora"

                    val status = snap.child("status").value?.toString()?.lowercase() ?: ""

                    atualizarStatus(status)

                    decidirVisao(usuarioIdPrestacao, prestadorIdPrestacao)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun atualizarStatus(status: String) {

        when (status) {

            "aguardando_confirmacao" -> {
                binding.tvStatusServico.text = " • aguardando confirmação"
                binding.tvStatusServico.setTextColor(Color.parseColor("#F39C12"))
                binding.btnConcluir.visibility = View.GONE
            }

            "agendado" -> {
                binding.tvStatusServico.text = " • agendado"
                binding.tvStatusServico.setTextColor(Color.parseColor("#4CAF50"))
                binding.btnConcluir.visibility =
                    if (uid == usuarioIdPrestacao) View.VISIBLE else View.GONE
            }

            "concluido" -> {
                binding.tvStatusServico.text = " • concluído"
                binding.tvStatusServico.setTextColor(Color.parseColor("#5A0275"))
                binding.btnConcluir.visibility = View.GONE
            }

            else -> {
                binding.tvStatusServico.text = " • status desconhecido"
                binding.tvStatusServico.setTextColor(Color.GRAY)
                binding.btnConcluir.visibility = View.GONE
            }
        }
    }

    // =======================================================
    // MOSTRAR DADOS DO CLIENTE OU PRESTADOR
    // =======================================================
    private fun decidirVisao(uidCliente: String, uidPrestador: String) {
        if (uid == uidPrestador)
            carregarDadosCliente(uidCliente)
        else
            carregarDadosPrestador(uidPrestador)
    }

    private fun carregarDadosCliente(uidCliente: String) {

        val ref = db.child("usuarios").child(uidCliente)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {

                binding.txtNome.text = snap.child("nome").value?.toString() ?: "Usuário"
                binding.txtDesde.visibility = View.GONE

                val endereco = construirEndereco(snap)
                binding.tvEnderecoCompleto.text = endereco

                val tel = snap.child("telefone").value?.toString() ?: "Não informado"
                binding.txtTelefone.text = "Telefone: $tel"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun carregarDadosPrestador(uidPrestador: String) {

        val refUser = db.child("usuarios").child(uidPrestador)
        val refPrest = db.child("prestadores").child(uidPrestador)

        refUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {

                binding.txtNome.text = snap.child("nome").value?.toString() ?: "Prestador"
                binding.txtDesde.text = "Prestador na AllService"

                binding.tvEnderecoCompleto.text = construirEndereco(snap)

                val tel = snap.child("telefone").value?.toString() ?: "Não informado"
                binding.txtTelefone.text = "Telefone: $tel"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        refPrest.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {

                val nota = snap.child("info_prestador/notaMedia")
                    .getValue(Double::class.java) ?: 0.0

                binding.txtRating.text = "★ ${String.format("%.1f", nota)}"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // =======================================================
    // CRIAR NOTIFICAÇÃO DE AVALIAÇÃO
    // =======================================================
    private fun criarNotificacaoDeAvaliacao(
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val ref = db.child("notificacoes").child(usuarioIdPrestacao).push()
        val id = ref.key ?: return onError()

        val agora = System.currentTimeMillis()
        val data = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(agora))
        val hora = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(agora))

        val dados = mapOf(
            "id" to id,
            "tipo" to "avaliacao_servico",
            "mensagem" to "Avalie o serviço realizado",
            "servico" to binding.tvTipoServico.text.toString(),
            "usuario_id" to usuarioIdPrestacao,
            "prestador_id" to prestadorIdPrestacao,
            "agendamento_id" to agendamentoId,
            "data" to data,
            "hora" to hora,
            "data_hora" to agora,
            "lido" to false
        )

        ref.setValue(dados)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }

    private fun irParaNotificacoes() {
        findNavController().navigate(R.id.telaNotificacaoFragment)
    }

    // =======================================================
    // 🔧 UTILITÁRIOS
    // =======================================================
    private fun construirEndereco(snap: DataSnapshot): String {
        val log = snap.child("logradouro").value?.toString() ?: ""
        val num = snap.child("numero").value?.toString() ?: ""
        val bairro = snap.child("bairro").value?.toString() ?: ""
        val cidade = snap.child("cidade").value?.toString() ?: ""
        val estado = snap.child("estado").value?.toString() ?: ""

        val l1 = "$log, $num".trim().trim(',')
        val l2 = "$bairro - $cidade, $estado".trim().trim(',')

        return if (l1.isBlank() && l2.isBlank()) "Endereço não informado"
        else "$l1\n$l2"
    }

    private fun formatarData(dataIso: String): String {
        return try {
            val i = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val o = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            o.format(i.parse(dataIso)!!)
        } catch (_: Exception) {
            dataIso
        }
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
