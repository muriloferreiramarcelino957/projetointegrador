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

            // Nome
            tvNome.text = p.user.nomeUsuario

            // Nota
            val nota = p.prestador.notaMedia ?: 0.0
            tvNota.text = String.format(Locale.getDefault(), "%.1f", nota)

            // 🌟 "Na AllService desde ..." (Usando o novo campo dataCadastro)
            // Assumindo que dataCadastro é no formato "YYYY-MM-DD"
            tvDataInicio.text =
                "Na AllService desde ${formatarDataCadastro(p.dataCadastro)}"

            // Localização
            tvLocalizacao.text =
                "${p.user.bairro}, ${p.user.cidade} - ${p.user.estado}"

            // 🌟 Último acesso (Usando o novo campo ultimoAcesso)
            // Assumindo que ultimoAcesso é no formato "YYYY-MM-DD HH:MM:SS"
            tvUltimoAcesso.text =
                "Último acesso ${calcularTempoDesdeUltimoAcesso(p.ultimoAcesso)}"

            root.setOnClickListener { onItemClick(p) }
        }
    }

    // ============================================
    // FORMATAÇÃO DE DATAS
    // ============================================

    /**
     * Formata a string de "YYYY-MM-DD" para "Mês AAAA".
     */
    private fun formatarDataCadastro(data: String?): String {
        return runCatching {
            if (data.isNullOrBlank()) return "data desconhecida"

            // O formato de entrada é "YYYY-MM-DD"
            val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            // O formato de saída é "Mês de YYYY" (Ex: "novembro de 2025")
            val outFmt = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))

            val dateObj = inFmt.parse(data) ?: return data

            // Capitaliza a primeira letra do mês (Ex: "novembro" -> "Novembro")
            return outFmt.format(dateObj).replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
        }.getOrElse { data ?: "data inválida" }
    }

    /**
     * Calcula o tempo decorrido do último acesso no formato "YYYY-MM-DD HH:MM:SS".
     */
    private fun calcularTempoDesdeUltimoAcesso(dataHora: String?): String {
        if (dataHora.isNullOrBlank()) return "indisponível"

        return try {
            // Formato de entrada: "YYYY-MM-DD HH:MM:SS"
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
                    // Se for mais de 7 dias, mostra a data e hora formatada
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