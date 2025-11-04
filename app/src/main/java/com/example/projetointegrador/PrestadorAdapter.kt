package com.projetointegrador.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.databinding.ItemPrestadorBinding


class PrestadorAdapter(private val lista: List<Prestador>) :
    RecyclerView.Adapter<PrestadorAdapter.PrestadorViewHolder>() {

    inner class PrestadorViewHolder(val binding: ItemPrestadorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrestadorViewHolder {
        val binding = ItemPrestadorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PrestadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrestadorViewHolder, position: Int) {
        val prestador = lista[position]
        holder.binding.tvNomePrestador.text = prestador.nome
        holder.binding.tvLocalizacao.text = prestador.local
        holder.binding.tvNota.text = prestador.nota
        holder.binding.tvCategoria.text = prestador.categorias
    }

    override fun getItemCount() = lista.size
}
