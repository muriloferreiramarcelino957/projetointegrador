package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class TelaAgendaFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var btn_arrow_back: ImageView
    private lateinit var btnTime1: TextView
    private lateinit var btnTime2: TextView
    private lateinit var btnTime3: TextView
    private lateinit var btnFaxina: TextView
    private lateinit var btnHidraulica: TextView
    private lateinit var btnEletrica: TextView
    private lateinit var tvSelectedTime: TextView
    private lateinit var btnConfirm: Button

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedService: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tela_agenda, container, false)

        // Referências
        calendarView = view.findViewById(R.id.card_calendar)
        btn_arrow_back = view.findViewById(R.id.btn_arrow_back)
        btnTime1 = view.findViewById(R.id.btn_time1)
        btnTime2 = view.findViewById(R.id.btn_time2)
        btnTime3 = view.findViewById(R.id.btn_time3)
        btnFaxina = view.findViewById(R.id.btn_faxina)
        btnHidraulica = view.findViewById(R.id.btn_hidraulica)
        btnEletrica = view.findViewById(R.id.btn_eletrica)
        tvSelectedTime = view.findViewById(R.id.tv_selected_time)
        btnConfirm = view.findViewById(R.id.btn_confirm)

        // Seleção de data
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            Toast.makeText(requireContext(), "Data escolhida: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        // Seleção de horários
        setupTimeButtons()

        // Seleção de serviços
        setupServiceButtons()

        // Botão de voltar
        btn_arrow_back.setOnClickListener {
            findNavController().navigateUp()
        }

        // Botão confirmar
        btnConfirm.setOnClickListener {
            if (selectedDate != null && selectedTime != null && selectedService != null) {
                Toast.makeText(
                    requireContext(),
                    "Agendado: $selectedDate às $selectedTime ($selectedService)",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                findNavController().navigate(R.id.action_telaAgendaFragment_to_telaAgenda2Fragment)
            }
        }

        return view
    }

    // Função para lidar com a seleção de horário
    private fun setupTimeButtons() {
        val timeButtons = listOf(btnTime1, btnTime2, btnTime3)

        for (btn in timeButtons) {
            btn.setOnClickListener {
                resetTimeButtons(timeButtons)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                selectedTime = btn.text.toString()
                tvSelectedTime.text = "$selectedTime*"
            }
        }
    }

    private fun resetTimeButtons(buttons: List<TextView>) {
        for (btn in buttons) {
            btn.setBackgroundResource(R.drawable.border_calendar)
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        }
    }

    // Função para lidar com a seleção de serviço
    private fun setupServiceButtons() {
        val serviceButtons = listOf(btnFaxina, btnHidraulica, btnEletrica)

        for (btn in serviceButtons) {
            btn.setOnClickListener {
                resetServiceButtons(serviceButtons)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                selectedService = btn.text.toString()
            }
        }
    }

    private fun resetServiceButtons(buttons: List<TextView>) {
        for (btn in buttons) {
            btn.setBackgroundResource(R.drawable.border_calendar)
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        }
    }
}
