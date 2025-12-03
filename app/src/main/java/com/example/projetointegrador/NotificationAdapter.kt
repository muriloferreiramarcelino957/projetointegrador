package com.example.projetointegrador.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.databinding.ItemNotificacaoBinding
import com.example.projetointegrador.databinding.ItemNotificacaoAvaliacaoBinding
import com.example.projetointegrador.model.Notificacao
import com.google.firebase.database.FirebaseDatabase

class NotificacaoAdapter(
    private val onAceitar: (String) -> Unit,
    private val onRecusar: (String) -> Unit,
    private val onMarcarLido: (String) -> Unit,
    private val onAvaliar: (String, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_NORMAL = 0
        private const val VIEW_AVALIACAO = 1
    }

    private val db = FirebaseDatabase.getInstance().reference
    private var lista = listOf<Notificacao>()

    fun atualizar(novas: List<Notificacao>) {
        lista = novas.sortedByDescending { it.data_hora }
        notifyDataSetChanged()
    }

    override fun getItemCount() = lista.size

    override fun getItemViewType(position: Int): Int =
        if (lista[position].tipo == "avaliacao_servico") VIEW_AVALIACAO else VIEW_NORMAL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_NORMAL)
            VHNormal(ItemNotificacaoBinding.inflate(inflater, parent, false))
        else
            VHAvaliacao(ItemNotificacaoAvaliacaoBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notif = lista[position]

        if (holder is VHNormal) holder.bind(notif)
        if (holder is VHAvaliacao) holder.bind(notif)
    }

    inner class VHNormal(private val b: ItemNotificacaoBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(n: Notificacao) {

            b.txtTitulo.text = n.mensagem.ifBlank { "Notificação" }
            b.txtDescricao.text = n.mensagem

            b.layoutDetalhes.visibility = View.GONE
            b.layoutConfirmacao.visibility = View.GONE
            b.btnMarcarLido.visibility = View.GONE

            when (n.tipo) {

                "solicitacao_servico" -> {

                    b.layoutDetalhes.visibility = View.VISIBLE
                    b.layoutConfirmacao.visibility = View.VISIBLE

                    carregarDadosDinamicos(n)

                    val id = n.id
                    b.btnSim.setOnClickListener { onAceitar(id) }
                    b.btnNao.setOnClickListener { onRecusar(id) }
                }

                "confirmado" -> {
                    b.txtTitulo.text = "Seu agendamento foi confirmado!"
                    b.btnMarcarLido.visibility = View.VISIBLE
                }

                "recusado" -> {
                    b.txtTitulo.text = "Sua solicitação foi recusada"
                    b.btnMarcarLido.visibility = View.VISIBLE
                }
            }

            b.btnMarcarLido.setOnClickListener {
                onMarcarLido(n.id)
            }
        }

        private fun carregarDadosDinamicos(n: Notificacao) {

            db.child("prestacoes").child(n.agendamento_id)
                .get().addOnSuccessListener { prestacao ->

                    val servico = prestacao.child("tipo_servico").value?.toString() ?: "Indefinido"
                    val valor = prestacao.child("valor").value?.toString() ?: "0"
                    val data = prestacao.child("data").value?.toString() ?: ""
                    val hora = prestacao.child("hora").value?.toString() ?: ""

                    b.txtServico.text = "Serviço: $servico"
                    b.txtDataHora.text = "Data: $data • $hora"
                    b.txtValor.text = "R$ $valor"
                }

            db.child("usuarios").child(n.usuario_id)
                .get().addOnSuccessListener { usuario ->

                    val nome = usuario.child("nome").value?.toString() ?: "Cliente"
                    val log = usuario.child("logradouro").value?.toString() ?: ""
                    val num = usuario.child("numero").value?.toString() ?: ""
                    val bairro = usuario.child("bairro").value?.toString() ?: ""
                    val cidade = usuario.child("cidade").value?.toString() ?: ""
                    val estado = usuario.child("estado").value?.toString() ?: ""

                    b.txtNomeUsuario.text = nome
                    b.txtEndereco.text = "$log, $num - $bairro, $cidade - $estado"
                }
        }
    }

    inner class VHAvaliacao(private val b: ItemNotificacaoAvaliacaoBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(n: Notificacao) {

            b.txtTitulo.text = "Avalie o serviço realizado"
            b.txtDescricao.text = "Ajude outros usuários deixando uma avaliação."
            b.txtChipTipo.text = "Serviço contratado"

            carregarDadosAvaliacao(n)

            b.ratingBarAvaliacao.rating = 0f

            b.btnAvaliar.setOnClickListener {
                val estrelas = b.ratingBarAvaliacao.rating.toInt()

                if (estrelas == 0) {
                    Toast.makeText(
                        b.root.context,
                        "Selecione uma nota!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                onAvaliar(n.id, estrelas)
            }
        }

        private fun carregarDadosAvaliacao(n: Notificacao) {

            db.child("prestacoes").child(n.agendamento_id)
                .get().addOnSuccessListener { prest ->

                    val servico = prest.child("tipo_servico").value?.toString() ?: "Serviço"
                    val data = prest.child("data").value?.toString() ?: ""
                    val hora = prest.child("hora").value?.toString() ?: ""

                    b.txtServico.text = "Serviço: $servico"
                    b.txtDataHora.text = "Data: $data • $hora"
                }
        }
    }
}
