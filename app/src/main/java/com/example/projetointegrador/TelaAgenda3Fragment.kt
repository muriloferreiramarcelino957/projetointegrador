package com.example.projetointegrador

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TelaAgenda3Fragment : Fragment() {

    private var _binding: TelaDeAgenda3Binding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private val args by navArgs<TelaAgenda3FragmentArgs>()

    private var agendamentoId: String = ""

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
        super.onViewCreated(view, savedInstanceState)

        agendamentoId = args.agendamentoId

        binding.btnArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConcluir.setOnClickListener {
            concluirServico()
        }

        carregarDadosAgendamento()
    }

    // =====================================================================
    // CARREGA /prestacoes/{agendamentoId} + dados do usuário contratante
    // =====================================================================
    private fun carregarDadosAgendamento() {

        val ref = database.child("prestacoes").child(agendamentoId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "Agendamento não encontrado", Toast.LENGTH_SHORT).show()
                    return
                }

                val tipoServico = snapshot.child("tipoServico").getValue(String::class.java) ?: ""
                val data = snapshot.child("data").getValue(String::class.java) ?: ""
                val hora = snapshot.child("hora").getValue(String::class.java) ?: ""
                val usuarioId = snapshot.child("usuarioId").getValue(String::class.java) ?: ""
                val statusCode = snapshot.child("status").getValue(String::class.java) ?: "aguardando_confirmacao"

                // Preenche cabeçalho
                binding.tvTipoServico.text = tipoServico
                binding.tvDataHora.text = "$data • $hora"
                binding.tvLocalizacao.text = "Endereço do contratante"

                // Status / média, por enquanto placeholder
                val statusTexto = traduzirStatus(statusCode)
                binding.ratingMedia.text = "Status\n$statusTexto"

                if (usuarioId.isNotEmpty()) {
                    carregarDadosContratante(usuarioId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Busca nome + endereço do contratante em /usuarios/{usuarioId}
    private fun carregarDadosContratante(usuarioId: String) {

        database.child("usuarios").child(usuarioId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {

                    val nome = snap.child("nome").getValue(String::class.java) ?: "Usuário"
                    val bairro = snap.child("bairro").getValue(String::class.java) ?: ""
                    val cidade = snap.child("cidade").getValue(String::class.java) ?: ""
                    val estado = snap.child("estado").getValue(String::class.java) ?: ""
                    val logradouro = snap.child("logradouro").getValue(String::class.java) ?: ""
                    val numero = snap.child("numero").getValue(String::class.java) ?: ""

                    val localLinha1 = listOfNotNull(logradouro, numero.takeIf { it.isNotBlank() })
                        .joinToString(", ")

                    val localLinha2 = listOfNotNull(bairro, cidade, estado)
                        .joinToString(", ")

                    binding.txtNome.text = nome
                    binding.statusOnline.text = "● offline"   // futuramente você pode ligar com /status
                    binding.txtRating.text = "★ 0,0"

                    binding.tvLocalizacao.text =
                        if (localLinha1.isNotBlank() || localLinha2.isNotBlank())
                            "$localLinha1\n$localLinha2"
                        else
                            "Localização não informada"

                    // “Na AllService desde ...” → se você salvar data_cadastro em /usuarios
                    val dataCadastro = snap.child("data_cadastro").getValue(String::class.java)
                    binding.txtDesde.text =
                        if (!dataCadastro.isNullOrBlank())
                            "Na AllService desde ${formataMesAno(dataCadastro)}"
                        else
                            "Na AllService desde —"

                    binding.txtLocal.text = localLinha2
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =====================================================================
    // CONCLUIR SERVIÇO → status = "finalizado"
    // =====================================================================
    private fun concluirServico() {

        val ref = database.child("prestacoes").child(agendamentoId)

        ref.child("status").setValue("finalizado")
            .addOnSuccessListener {
                mostrarDialogoSucesso()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao concluir serviço", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoSucesso() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sucesso, null)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .setOnDismissListener {
                // volta para a agenda após concluir
                findNavController().navigateUp()
            }
            .show()
    }

    // =====================================================================
    // HELPERS
    // =====================================================================
    private fun traduzirStatus(code: String): String {
        return when (code) {
            "aguardando_confirmacao" -> "aguardando confirmação"
            "agendado" -> "agendado"
            "em_execucao" -> "em execução"
            "finalizado" -> "finalizado"
            "cancelado" -> "cancelado"
            "recusado" -> "recusado"
            else -> code.replace("_", " ")
        }
    }

    private fun formataMesAno(data: String): String {
        return try {
            val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outFmt = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
            outFmt.format(inFmt.parse(data)!!).replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
        } catch (e: Exception) {
            data
        }
    }
    private fun configurarMenuLateral() {
        val btnMenu = binding.topBar.root.findViewById<ImageView>(R.id.ic_menu)
        val drawerLayout = binding.root.findViewById<DrawerLayout>(R.id.drawerLayout)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
