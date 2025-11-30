package com.projetointegrador.app.ui

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
        val binding = ItemPrestadorAvaliadoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PrestadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrestadorViewHolder, position: Int) {
        val p = listaPrestadores[position]

        with(holder.binding) {

            tvNome.text = p.user.nome.ifBlank { "Prestador" }

            val nota = p.prestador.notaMedia ?: 0.0
            tvNota.text = String.format(Locale("pt", "BR"), "%.1f", nota)

            tvDataInicio.text =
                if (p.dataCadastro.isNotBlank())
                    "Na AllService desde ${formatarDataCadastro(p.dataCadastro)}"
                else
                    "Data de cadastro desconhecida"

            tvLocalizacao.text = listOfNotNull(
                p.user.bairro.takeIf { it.isNotBlank() },
                p.user.cidade.takeIf { it.isNotBlank() },
                p.user.estado.takeIf { it.isNotBlank() }
            ).joinToString(", ")

            tvUltimoAcesso.text =
                "Último acesso ${calcularTempoDesdeUltimoAcesso(p.ultimoAcesso)}"

            root.setOnClickListener { onItemClick(p) }
        }
    }

    private fun formatarDataCadastro(data: String?): String {
        return runCatching {
            if (data.isNullOrBlank()) return "data desconhecida"

            val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outFmt = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))

            val dateObj = inFmt.parse(data) ?: return data

            return outFmt.format(dateObj).replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
        }.getOrElse { data ?: "data inválida" }
    }

    private fun calcularTempoDesdeUltimoAcesso(dataHora: String?): String {
        if (dataHora.isNullOrBlank()) return "indisponível"

        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val ultimoAcesso = format.parse(dataHora)
            val agora = Date()

            if (ultimoAcesso == null) return "indisponível"

            val diffMillis = agora.time - ultimoAcesso.time

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

            return when {
                minutes < 60 -> "há ${minutes} minuto(s)"
                hours < 24 -> "há ${hours} hora(s)"
                days <= 7 -> "há ${days} dia(s)"
                else -> {
                    val specificFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    "em ${specificFormat.format(ultimoAcesso)}"
                }
            }
        } catch (e: Exception) {
            "indisponível"
        }
    }

    override fun getItemCount() = listaPrestadores.size

    fun atualizarLista(novaLista: List<PrestadorDisplay>) {
        listaPrestadores = novaLista
        notifyDataSetChanged()
    }
}
