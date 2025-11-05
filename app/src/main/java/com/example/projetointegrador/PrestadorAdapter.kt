package com.projetointegrador.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetointegrador.PrestadorDisplay
import com.example.projetointegrador.databinding.ItemPrestadorAvaliadoBinding
import java.text.SimpleDateFormat
import java.util.*

class PrestadorAdapter : RecyclerView.Adapter<PrestadorAdapter.PrestadorViewHolder>() {

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
        val prestadorDisplay = listaPrestadores[position]

        with(holder.binding) {
            tvNome.text = prestadorDisplay.user.nomeUsuario

            val notaFormatada = String.format(Locale.getDefault(), "%.1f", prestadorDisplay.prestador.notaMedia)
            rbNota.rating = prestadorDisplay.prestador.notaMedia.toFloat()
            tvNota.text = notaFormatada

            tvDataInicio.text = "Na AllService desde ${formatarData(prestadorDisplay.prestador.dataDeInicio)}"
            tvLocalizacao.text = "${prestadorDisplay.user.bairro}, ${prestadorDisplay.user.cidade} - ${prestadorDisplay.user.estado}"

            val servicosText = buildString {
                val servicos = prestadorDisplay.tiposServico?.let { tipos ->
                    listOfNotNull(
                        if (tipos.tipoServico1.isNotEmpty()) "${tipos.tipoServico1} (R$ ${tipos.valorServico1})" else null,
                        if (tipos.tipoServico2.isNotEmpty()) "${tipos.tipoServico2} (R$ ${tipos.valorServico2})" else null,
                        if (tipos.tipoServico3.isNotEmpty()) "${tipos.tipoServico3} (R$ ${tipos.valorServico3})" else null
                    )
                } ?: emptyList()
                
                servicos.take(3).forEachIndexed { index, servico ->
                    if (index > 0) append(", ")
                    append(servico)
                }
                if (servicos.size > 3) {
                    append(" +${servicos.size - 3} mais")
                }
            }
            tvServicos.text = servicosText

            tvUltimoAcesso.text = "Último acesso: há ${calcularTempoDesde(prestadorDisplay.prestador.dataDeInicio)}"
        }
    }

    private fun formatarData(data: String): String {
        return try {
            val inputFormat = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(data)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            data
        }
    }

    private fun calcularTempoDesde(data: String): String {
        return try {
            val format = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            val inicio = format.parse(data)
            val agora = Date()
            val diff = agora.time - inicio.time

            val segundos = diff / 1000
            val minutos = segundos / 60
            val horas = minutos / 60
            val dias = horas / 24
            val meses = dias / 30
            val anos = dias / 365

            when {
                anos > 0 -> "$anos ano(s)"
                meses > 0 -> "$meses mês(es)"
                dias > 0 -> "$dias dia(s)"
                horas > 0 -> "$horas hora(s)"
                else -> "pouco tempo"
            }
        } catch (e: Exception) {
            "algum tempo"
        }
    }

    override fun getItemCount(): Int = listaPrestadores.size

    fun atualizarLista(novaLista: List<PrestadorDisplay>) {
        listaPrestadores = novaLista
        notifyDataSetChanged()
    }
}