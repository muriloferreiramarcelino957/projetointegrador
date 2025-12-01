package com.example.projetointegrador.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.databinding.ItemNotificacaoBinding
import com.example.projetointegrador.databinding.ItemNotificacaoAvaliacaoBinding
import com.example.projetointegrador.model.Notificacao

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

    private var lista = listOf<Notificacao>()

    fun atualizar(novas: List<Notificacao>) {
        lista = novas.sortedByDescending { it.data_hora ?: 0 }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = lista.size

    override fun getItemViewType(position: Int): Int {
        return if (lista[position].tipo == "avaliacao_servico") VIEW_AVALIACAO else VIEW_NORMAL
    }

    // ============================================================
    // VIEW HOLDERS
    // ============================================================

    inner class NormalVH(val b: ItemNotificacaoBinding) :
        RecyclerView.ViewHolder(b.root)

    inner class AvaliacaoVH(val b: ItemNotificacaoAvaliacaoBinding) :
        RecyclerView.ViewHolder(b.root)

    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_NORMAL) {

            val b = ItemNotificacaoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            NormalVH(b)

        } else {

            val b = ItemNotificacaoAvaliacaoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AvaliacaoVH(b)
        }
    }

    // ============================================================
    // BIND VIEW HOLDER
    // ============================================================

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val n = lista[position]

        // ⭐ VIEW NORMAL
        if (holder is NormalVH) {
            val b = holder.b

            b.txtTitulo.text = n.mensagem ?: "Notificação"
            b.txtDescricao.text = n.mensagem ?: ""

            // reset
            b.layoutDetalhes.visibility = View.GONE
            b.layoutConfirmacao.visibility = View.GONE

            when (n.tipo) {

                "solicitacao_servico" -> {
                    b.txtTitulo.text = "Novo pedido de serviço"

                    b.layoutDetalhes.visibility = View.VISIBLE
                    b.txtNomeUsuario.text = "Cliente: ${n.nome_usuario}"
                    b.txtEndereco.text = "Endereço: ${n.endereco}"
                    b.txtServico.text = "Serviço: ${n.servico}"
                    b.txtDataHora.text = "Data: ${n.data ?: "-"} • ${n.hora ?: "-"}"
                    b.txtValor.text = "R$ ${n.valor ?: "0"}"

                    b.layoutConfirmacao.visibility = View.VISIBLE

                    val id = n.id ?: ""
                    b.btnSim.setOnClickListener { onAceitar(id) }
                    b.btnNao.setOnClickListener { onRecusar(id) }
                }

                "confirmado" -> {
                    b.txtTitulo.text = "Seu agendamento foi confirmado!"
                }

                "recusado" -> {
                    b.txtTitulo.text = "Seu pedido de serviço foi recusado"
                }
            }

            // Marcar como lido → remove
            b.btnMarcarLido.setOnClickListener {
                if (!n.id.isNullOrEmpty()) onMarcarLido(n.id!!)
            }
        }

        // ⭐ VIEW AVALIAÇÃO
        if (holder is AvaliacaoVH) {
            val b = holder.b

            b.txtTitulo.text = "Avalie o serviço realizado"
            b.txtDescricao.text = "Ajude outros usuários deixando uma avaliação."

            b.txtServico.text = "Serviço: ${n.servico ?: "-"}"
            b.txtDataHora.text = "Data: ${n.data ?: "-"} • ${n.hora ?: "-"}"

            val id = n.id ?: ""

            b.btnAvaliar.setOnClickListener {
                val estrelas = b.ratingBar.rating.toInt()

                if (estrelas == 0) {
                    android.widget.Toast.makeText(
                        b.root.context,
                        "Selecione uma nota!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                onAvaliar(id, estrelas)
            }
        }
    }
}
