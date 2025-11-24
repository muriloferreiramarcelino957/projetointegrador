package com.example.projetointegrador.registro.cadastro

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaTipoDeServico2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class TipoDeServico2 : Fragment() {

    private var _binding: TelaTipoDeServico2Binding? = null
    private val binding get() = _binding!!

    private val args by navArgs<TipoDeServico2Args>()
    private lateinit var auth: FirebaseAuth
    private lateinit var prestadoresRef: DatabaseReference

    /** Horários permitidos **/
    private val horariosInicio = listOf(
        "06:00","06:30","07:00","07:30",
        "08:00","08:30","09:00","09:30",
        "10:00","10:30","11:00","11:30",
        "12:00","12:30","13:00","13:30",
        "14:00","14:30","15:00"
    )

    private val horariosFim = listOf(
        "09:00","09:30",
        "10:00","10:30","11:00","11:30",
        "12:00","12:30","13:00","13:30",
        "14:00","14:30","15:00","15:30",
        "16:00","16:30","17:00","17:30",
        "18:00"
    )

    /** Para cada dia: campos de início e fim */
    private val camposPorDia = mutableMapOf<String, Pair<AutoCompleteTextView, AutoCompleteTextView>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaTipoDeServico2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        prestadoresRef = FirebaseDatabase.getInstance().reference.child("prestadores")

        val diasSelecionados = normalizeArray(args.diasSelecionados)
        gerarCamposParaDias(diasSelecionados)

        configurarIndicadores()
        configurarBotoes()
    }

    /** Normaliza dias vindos do SafeArgs */
    private fun normalizeArray(value: Any?): List<String> =
        when (value) {
            null -> emptyList()
            is Array<*> -> value.filterIsInstance<String>()
            is List<*> -> value.filterIsInstance<String>()
            is String -> listOf(value)
            else -> listOf(value.toString())
        }

    /** Cria dinamicamente os campos para cada dia selecionado */
    private fun gerarCamposParaDias(dias: List<String>) {
        val ctx = requireContext()
        val container = binding.containerDias

        val adapterInicio = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, horariosInicio)
        val adapterFim = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, horariosFim)

        camposPorDia.clear()

        dias.forEach { diaLabel ->

            container.addView(
                TextView(ctx).apply {
                    text = "Horários de $diaLabel"
                    setTextColor(Color.WHITE)
                    textSize = 17f
                    setPadding(10, 25, 10, 10)
                    typeface = Typeface.DEFAULT_BOLD
                }
            )

            val inicio = criarCampoHorario("Início do turno", adapterInicio)
            val fim = criarCampoHorario("Fim do turno", adapterFim)

            container.addView(inicio)
            container.addView(fim)

            camposPorDia[diaLabel] = inicio to fim
        }
    }

    /** Cria AutoCompleteTextView estilizado */
    private fun criarCampoHorario(
        hint: String,
        adapter: ArrayAdapter<String>
    ): AutoCompleteTextView = AutoCompleteTextView(requireContext()).apply {

        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 120
        ).apply { topMargin = 10 }

        background = GradientDrawable().apply {
            setStroke(3, Color.WHITE)
            cornerRadius = 18f
            setColor(Color.TRANSPARENT)
        }

        this.hint = hint
        setHintTextColor(Color.WHITE)
        setTextColor(Color.WHITE)
        textSize = 15f
        setPadding(35, 20, 20, 20)

        /** Impede digitação */
        inputType = InputType.TYPE_NULL
        keyListener = null
        isFocusable = true
        isFocusableInTouchMode = true

        /** Adapter + abrir dropdown automaticamente */
        setAdapter(adapter)
        threshold = 0
        setOnClickListener { showDropDown() }
        setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
    }

    private fun configurarIndicadores() {
        binding.ball1.setImageResource(R.drawable.ic_ball_not_pressed)
        binding.ball2.setImageResource(R.drawable.ic_ball_pressed)
        binding.ball3.setImageResource(R.drawable.ic_ball_not_pressed)
    }

    private fun configurarBotoes() {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.btnProximo.setOnClickListener {
            if (!validarCampos()) return@setOnClickListener

            salvarDisponibilidade()
            findNavController().navigate(R.id.tipoDeServico3Fragment)
        }
    }

    private fun validarCampos(): Boolean {
        camposPorDia.forEach { (dia, campos) ->
            val inicio = campos.first.text.toString()
            val fim = campos.second.text.toString()

            if (inicio.isEmpty() || fim.isEmpty()) {
                Toast.makeText(requireContext(), "Defina início e fim para $dia.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun salvarDisponibilidade() {
        val uid = auth.currentUser?.uid ?: return

        val (dataCadastro, ultimoAcesso) = datasBrasil()

        val disponibilidade = camposPorDia.mapValues { (_, campos) ->
            mapOf(
                "inicio" to campos.first.text.toString(),
                "fim" to campos.second.text.toString()
            )
        }.mapKeys { (k, _) -> mapDiaParaKey(k) }

        prestadoresRef.child(uid).updateChildren(
            mapOf(
                "disponibilidade" to disponibilidade,
                "nivel_cadastro" to "bronze",
                "data_cadastro" to dataCadastro,
                "ultimo_acesso" to ultimoAcesso
            )
        ).addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao salvar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Mapeia: Segunda → segunda */
    private fun mapDiaParaKey(nome: String): String =
        when (nome.lowercase(Locale.getDefault())) {
            "segunda", "segunda-feira" -> "segunda"
            "terça", "terça-feira", "terca", "terca-feira" -> "terca"
            "quarta", "quarta-feira" -> "quarta"
            "quinta", "quinta-feira" -> "quinta"
            "sexta", "sexta-feira" -> "sexta"
            "sábado", "sabado" -> "sabado"
            "domingo" -> "domingo"
            else -> nome.lowercase(Locale.getDefault())
        }

    /** Retorna datas corretas no fuso de Brasília */
    private fun datasBrasil(): Pair<String, String> {
        val tz = TimeZone.getTimeZone("America/Sao_Paulo")
        val agora = Date()

        val data = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = tz }
        val dataHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply { timeZone = tz }

        return data.format(agora) to dataHora.format(agora)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
