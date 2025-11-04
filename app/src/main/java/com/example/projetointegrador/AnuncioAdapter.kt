package com.projetointegrador.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.databinding.ItemAnuncioBinding


class AnuncioAdapter(private val lista: List<Anuncio>) :
    RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder>() {

    inner class AnuncioViewHolder(val binding: ItemAnuncioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnuncioViewHolder {
        val binding = ItemAnuncioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnuncioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnuncioViewHolder, position: Int) {
        val anuncio = lista[position]
        holder.binding.tvTitulo.text = anuncio.titulo
        holder.binding.tvPreco.text = anuncio.preco
        holder.binding.ivAnuncio.setImageResource(anuncio.imagem)
    }

    override fun getItemCount(): Int = lista.size
}
