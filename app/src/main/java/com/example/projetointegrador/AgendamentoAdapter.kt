package com.example.projetointegrador.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.TelaAgenda2Fragment
import com.example.projetointegrador.databinding.ItemAgendamentoBinding

class AgendamentoAdapter(
    private val lista: List<TelaAgenda2Fragment.Agendamento>,
    private val onClick: (TelaAgenda2Fragment.Agendamento) -> Unit
) : RecyclerView.Adapter<AgendamentoAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemAgendamentoBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAgendamentoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ag = lista[position]

        holder.b.txtDataHora.text = "${ag.data} • ${ag.hora}"

        holder.b.txtServico.text = ag.tipoServico

        holder.b.txtLocal.text =
            ag.local.ifBlank { "Local informado no momento da contratação" }

        holder.b.root.setOnClickListener { onClick(ag) }
    }
}
