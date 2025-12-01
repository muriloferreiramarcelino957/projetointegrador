package com.example.projetointegrador.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.databinding.ItemNotificacaoBinding
import com.example.projetointegrador.databinding.ItemNotificacaoAvaliacaoBinding
import com.example.projetointegrador.model.Notificacao

class NotificacaoAdapter2(
    private val onAceitar: (String) -> Unit,
    private val onRecusar: (String) -> Unit,
    private val onMarcarLido: (String) -> Unit,
    private val onAvaliar: (String, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_NORMAL = 0
        private const val TIPO_AVALIACAO = 1
    }

    private var lista = listOf<Notificacao>()

    fun atualizar(novos: List<Notificacao>) {
        lista = novos
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (lista[position].tipo == "avaliacao_servico") TIPO_AVALIACAO else TIPO_NORMAL
    }

    override fun getItemCount(): Int = lista.size

    // ========================================================
    //  ON CREATE VIEW HOLDER
    // ========================================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TIPO_AVALIACAO) {

            val binding = ItemNotificacaoAvaliacaoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ViewHolderAvaliacao(binding)

        } else {

            val binding = ItemNotificacaoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ViewHolderNormal(binding)
        }
    }

    // ========================================================
    //  ON BIND VIEW HOLDER
    // ========================================================
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notif = lista[position]

        when (holder) {
            is ViewHolderNormal -> holder.bind(notif)
            is ViewHolderAvaliacao -> holder.bind(notif)
        }
    }

    // ========================================================
    //  VIEW HOLDER NORMAL (item_notificacao.xml)
    // ========================================================
    inner class ViewHolderNormal(val b: ItemNotificacaoBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(n: Notificacao) {

            b.txtTitulo.text = "Notificação"
            b.txtDescricao.text = n.mensagem

            val id = n.id ?: ""

            // Solicitação de serviço
            if (n.tipo == "solicitacao_servico") {

                b.txtTitulo.text = "Novo pedido de serviço"
                b.layoutDetalhes.visibility = View.VISIBLE

                b.txtNomeUsuario.text = "Cliente: ${n.nome_usuario}"
                b.txtEndereco.text = "Endereço: ${n.endereco}"
                b.txtServico.text = "Serviço: ${n.servico}"
                b.txtDataHora.text = "Data: ${n.data} • ${n.hora}"
                b.txtValor.text = "Valor: R$ ${n.valor}"

                b.layoutConfirmacao.visibility = View.VISIBLE

                b.btnSim.setOnClickListener { onAceitar(id) }
                b.btnNao.setOnClickListener { onRecusar(id) }

            } else {
                b.layoutDetalhes.visibility = View.GONE
                b.layoutConfirmacao.visibility = View.GONE
            }

            b.btnMarcarLido.setOnClickListener {
                if (id.isNotEmpty()) onMarcarLido(id)
            }
        }
    }

    // ========================================================
    //  VIEW HOLDER AVALIACAO (item_notificacao_avaliacao.xml)
    // ========================================================
    inner class ViewHolderAvaliacao(val b: ItemNotificacaoAvaliacaoBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(n: Notificacao) {

            val id = n.id ?: ""

            b.txtTitulo.text = "Avalie o serviço realizado"
            b.txtDescricao.text = "Ajude outros usuários deixando uma avaliação."

            // Dados básicos
            b.txtServico.text = "Serviço: ${n.servico}"
            b.txtDataHora.text = "Data: ${n.data} • ${n.hora}"

            // Botão enviar avaliação
            b.btnAvaliar.setOnClickListener {
                val estrelas = b.ratingBar.rating.toInt()

                if (estrelas == 0) {
                    android.widget.Toast.makeText(
                        b.root.context,
                        "Escolha uma nota.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                onAvaliar(id, estrelas)
            }
        }
    }

}
