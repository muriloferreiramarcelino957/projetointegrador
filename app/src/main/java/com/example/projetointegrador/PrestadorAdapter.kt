package com.example.projetointegrador.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.databinding.ItemPrestadorAvaliadoBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PrestadorAdapter(
    private val onItemClick: (PrestadorDisplay) -> Unit = {}
) : RecyclerView.Adapter<PrestadorAdapter.PrestadorViewHolder>() {

    private var listaPrestadores = listOf<PrestadorDisplay>()

    inner class PrestadorViewHolder(val binding: ItemPrestadorAvaliadoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrestadorViewHolder {
        return PrestadorViewHolder(
            ItemPrestadorAvaliadoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PrestadorViewHolder, position: Int) {
        val p = listaPrestadores[position]

        with(holder.binding) {
            tvNome.text = p.nome
            tvNota.text = String.format("%.1f", p.info_prestador.notaMedia)
            tvLocalizacao.text = p.cidade

            tvDataInicio.text =
                if (p.data_cadastro.isNotBlank())
                    "Desde ${p.data_cadastro}"
                else
                    "Data não informada"

            tvUltimoAcesso.text =
                "Último acesso ${calcularTempoDesde(p.ultimo_acesso)}"

            root.setOnClickListener { onItemClick(p) }
        }
    }

    private fun calcularTempoDesde(data: String): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dataAcesso = formatter.parse(data) ?: return "indisponível"

            val diff = Date().time - dataAcesso.time
            val horas = TimeUnit.MILLISECONDS.toHours(diff)
            val dias = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                horas < 24 -> "há $horas hora(s)"
                dias < 7 -> "há $dias dia(s)"
                else -> SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(dataAcesso)
            }
        } catch (e: Exception) {
            "indisponível"
        }
    }

    override fun getItemCount(): Int = listaPrestadores.size

    fun atualizarLista(novaLista: List<PrestadorDisplay>) {
        listaPrestadores = novaLista
        notifyDataSetChanged()
    }
}
