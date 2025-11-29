package com.example.projetointegrador.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.databinding.ItemNotificacaoBinding
import com.example.projetointegrador.model.Notificacao
import com.google.firebase.database.DataSnapshot
import java.text.SimpleDateFormat
import java.util.*

class NotificacaoAdapter(
    private val lista: List<DataSnapshot>,
    private val onAceitar: (String) -> Unit,
    private val onRecusar: (String) -> Unit
) : RecyclerView.Adapter<NotificacaoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNotificacaoBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificacaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val snap = lista[position]
        val n = snap.getValue(Notificacao::class.java) ?: return

        with(holder.binding) {

            tvMensagem.text = n.mensagem

            tvData.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                .format(Date(n.data_hora))

            btnAceitar.setOnClickListener { onAceitar(snap.key!!) }
            btnRecusar.setOnClickListener { onRecusar(snap.key!!) }
        }
    }

    override fun getItemCount() = lista.size
}
